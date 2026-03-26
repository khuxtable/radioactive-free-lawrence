package org.kathrynhuxtable.radiofreelawrence.game;

import java.io.FileOutputStream;
import java.io.IOException;

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
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.ObjectNode;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.PlaceNode;

@RequiredArgsConstructor
@SpringBootApplication
@ComponentScan(basePackages = "org.kathrynhuxtable.radiofreelawrence")
@Slf4j
public class RadioactiveFreeLawrenceApplication {

	private static final String MAIN_GDESC_FILE = "main.gdesc";

	private final GameContext gameContext = new GameContext();

	public static void main(String[] args) {
		SpringApplication.run(RadioactiveFreeLawrenceApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			gameContext.gameNode.createDefaultElements();

			GameVisitor visitor = new GameVisitor(gameContext.gameNode, gameContext.errorReporter);
			visitor.readFile(MAIN_GDESC_FILE, false);

			gameContext.internalFunctions.validateGrammar();

			// Since places exist once, create variables for them.
			for (PlaceNode placeNode : gameContext.gameNode.getPlaces()) {
				gameContext.variableStore.addVariable(placeNode.getName(), VariableType.PLACE);
			}

			// Generate main Game class
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			gameContext.gameNode.generate(cw, gameContext);
			generateClassFile(cw, null);

			// Generate object inner classes
			for (ObjectNode objectNode : gameContext.gameNode.getObjects()) {
				ClassWriter innerCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
				objectNode.generate(innerCw, gameContext);
				generateClassFile(innerCw, objectNode.getName());
			}

			// Generate place inner classes
			for (PlaceNode placeNode : gameContext.gameNode.getPlaces()) {
				ClassWriter innerCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
				placeNode.generate(innerCw, gameContext);
				generateClassFile(innerCw, placeNode.getName());
			}

			new GameRunner().run(gameContext);

			System.exit(0);
		};
	}

	private void generateClassFile(ClassWriter classWriter, String innerClassName) throws IOException {
		StringBuilder filename = new StringBuilder("target/classes/");
		filename.append(GameContext.GAME_CLASS_NAME);
		if (innerClassName != null) {
			filename.append("$");
			filename.append(innerClassName);
		}
		filename.append(".class");

		byte[] bytes = classWriter.toByteArray();

		FileOutputStream innerOut = new FileOutputStream(filename.toString());
		innerOut.write(bytes);
		innerOut.close();
	}
}
