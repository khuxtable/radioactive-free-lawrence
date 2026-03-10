package org.kathrynhuxtable.radiofreelawrence.game;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassWriter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.GameVisitor;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.*;

@RequiredArgsConstructor
@SpringBootApplication
@ComponentScan(basePackages = "org.kathrynhuxtable.radiofreelawrence")
@Slf4j
public class RadioactiveFreeLawrenceApplication {

	private final GameContext gameContext = new GameContext();

	public static void main(String[] args) {
		SpringApplication.run(RadioactiveFreeLawrenceApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

//			System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//			String[] beanNames = ctx.getBeanDefinitionNames();
//			Arrays.sort(beanNames);
//			for (String beanName : beanNames) {
//				System.out.println(beanName);
//			}

			createDefaultElements();

			GameVisitor visitor = new GameVisitor(gameContext.gameNode, gameContext.errorReporter);
			visitor.readFile("foo.gdesc", false);

			// Since places exist once, create variables for them.
			for (PlaceNode placeNode : gameContext.gameNode.getPlaces()) {
				gameContext.variableStore.addVariable(placeNode.getName(), VariableType.PLACE);
			}

			// Generate main Game class
			MyClassVisitor cw = new MyClassVisitor(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			gameContext.gameNode.generate(cw, gameContext);

			byte[] bytes = cw.toByteArray();
			FileOutputStream out = new FileOutputStream("target/classes/" + GameContext.GAME_CLASS_NAME + ".class");
			out.write(bytes);
			out.close();

			// Generate object inner classes
			for (ObjectNode objectNode : gameContext.gameNode.getObjects()) {
				MyClassVisitor innerCw = new MyClassVisitor(ClassWriter.COMPUTE_FRAMES);
				objectNode.generate(innerCw, gameContext);
				byte[] innerBytes = innerCw.toByteArray();
				FileOutputStream innerOut = new FileOutputStream(
						"target/classes/" + GameContext.GAME_CLASS_NAME + "$" + objectNode.getName() + ".class");
				innerOut.write(innerBytes);
				innerOut.close();
			}

			// Generate place inner classes
			for (PlaceNode placeNode : gameContext.gameNode.getPlaces()) {
				MyClassVisitor innerCw = new MyClassVisitor(ClassWriter.COMPUTE_FRAMES);
				placeNode.generate(innerCw, gameContext);
				byte[] innerBytes = innerCw.toByteArray();
				FileOutputStream innerOut = new FileOutputStream(
						"target/classes/" + GameContext.GAME_CLASS_NAME + "$" + placeNode.getName() + ".class");
				innerOut.write(innerBytes);
				innerOut.close();
			}

			new GameRunner().run(gameContext);

			System.exit(0);
		};
	}

	private void createDefaultElements() {
		createState("badword", -2);
		createState("ambigword", -3);
		createState("badsyntax", -1);

		createVariable("arg1");
		createVariable("arg2");
		createVariable("status");
		createVariable("here");
		createVariable("there");

		createPlace("inhand", "inventory", "Inventory");
		createPlace("ylem", "ylem", "Ylem");
	}

	private void createState(String name, int value) {
		StateClauseNode node = new StateClauseNode(name, NumberLiteralNode.builder()
				.number(value)
				.sourceLocation(new SourceLocation(null, 0, 0))
				.build(),
				new SourceLocation(null, 0, 0));
		gameContext.gameNode.getStates().put(name, node);
		gameContext.gameNode.getIdentifiers().put(name, node);
	}

	private void createVariable(String name) {
		VariableNode node = VariableNode.builder()
				.variable(name)
				.sourceLocation(new SourceLocation(null, 0, 0))
				.build();
		gameContext.gameNode.getVariables().add(node);
		gameContext.gameNode.getIdentifiers().put(name, node);
	}

	private void createPlace(String name, String briefDescription, String longDescription) {
		PlaceNode node = PlaceNode.builder()
				.name(name)
				.verbs(new HashSet<>())
				.briefDescription(briefDescription)
				.longDescription(longDescription)
				.variables(new ArrayList<>())
				.commands(new LinkedHashMap<>())
				.procs(new LinkedHashMap<>())
				.sourceLocation(new SourceLocation(null, 0, 0))
				.build();
		// Add name to vocabulary.
		gameContext.gameNode.getVerbs().put(node.getName(), node);
		gameContext.gameNode.getIdentifiers().put(node.getName(), node);
		gameContext.gameNode.getPlaces().add(node);
	}
}
