package org.kathrynhuxtable.radiofreelawrence.game;

import java.io.FileOutputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassWriter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.GameGenerator;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.GameVisitor;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.ObjectNode;

@RequiredArgsConstructor
@SpringBootApplication
@ComponentScan(basePackages = "org.kathrynhuxtable.radiofreelawrence")
@Slf4j
public class RadioactiveFreeLawrenceApplication {

	private final GameGenerator gameGenerator;
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

			GameVisitor visitor = new GameVisitor(gameContext.gameNode, gameContext.errorReporter);
			visitor.readFile("foo.gdesc", false);

			// Generate main Game class
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			gameContext.gameNode.generate(cw, gameContext);

			byte[] bytes = cw.toByteArray();
			FileOutputStream out = new FileOutputStream("target/classes/" + GameContext.GAME_CLASS_NAME + ".class");
			out.write(bytes);
			out.close();

			// Generate object inner classes
			for (ObjectNode objectNode : gameContext.gameNode.getObjects()) {
				ClassWriter innerCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
				objectNode.generate(innerCw, gameContext);
				byte[] innerBytes = innerCw.toByteArray();
				FileOutputStream innerOut = new FileOutputStream(
						"target/classes/" + GameContext.GAME_CLASS_NAME + "$" + objectNode.getName() + ".class");
				innerOut.write(innerBytes);
				innerOut.close();
			}

			new GameRunner().run(gameContext);

			System.exit(0);
		};
	}

}
