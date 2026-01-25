package org.kathrynhuxtable.radiofreelawrence.game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.GameGenerator;

@RequiredArgsConstructor
@SpringBootApplication
@ComponentScan(basePackages = "org.kathrynhuxtable.radiofreelawrence")
@Slf4j
public class RadioactiveFreeLawrenceApplication {

	private final GameGenerator gameGenerator;
	private final GameData gameData;

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

			try {
				gameGenerator.generate();

				gameData.callInits();

				for (; ; ) {
					try {
						gameData.callRepeats();
					} catch (GameRuntimeException e) {
						e.printStackTrace(System.out);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		};
	}

}
