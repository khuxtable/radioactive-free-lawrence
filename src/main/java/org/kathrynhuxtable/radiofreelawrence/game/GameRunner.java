package org.kathrynhuxtable.radiofreelawrence.game;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.kathrynhuxtable.radiofreelawrence.game.exception.BreakException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.ContinueException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.ControlType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.InitialNode;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.RepeatNode;

public class GameRunner {

	public void run(GameContext gameContext) throws Exception {
		Class<?> myClass = Class.forName(GameContext.GAME_CLASS_NAME.replaceAll("/", "."));
		Constructor<?> constructor = myClass.getConstructor(InternalFunctions.class);
		Object instance = constructor.newInstance(gameContext.getInternalFunctions());

		callInits(gameContext.gameNode.getInits(), myClass, instance);

		if (!gameContext.gameNode.getRepeats().isEmpty()) {
			for (; ; ) {
				try {
					callRepeats(gameContext.gameNode.getRepeats(), myClass, instance);
				} catch (GameRuntimeException e) {
					e.printStackTrace(System.out);
				}
			}
		}
	}

	private void callInits(List<InitialNode> inits, Class<?> myClass, Object instance) {
		for (InitialNode init : inits) {
			String name = "initialProc" + init.getIndex();
			try {
				Method method = myClass.getDeclaredMethod(name);
				method.invoke(instance);
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof BreakException) {
					break;
				} else if (e.getCause() instanceof ContinueException) {
					// Implicit continue
				} else {
					throw new RuntimeException(e);
				}
			} catch (NoSuchMethodException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void callRepeats(List<RepeatNode> repeats, Class<?> myClass, Object instance) {
		for (; ; ) {
			for (RepeatNode repeat : repeats) {
				String name = "repeatProc" + repeat.getIndex();
				try {
					Method method = myClass.getDeclaredMethod(name);
					method.invoke(instance);
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof BreakException breakException) {
						if (breakException.getControlType() == ControlType.REPEAT) {
							break;
						}
					} else if (e.getCause() instanceof ContinueException) {
						// Restart from the beginning
						break;
					}  else {
						throw new RuntimeException(e);
					}
				} catch (NoSuchMethodException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
