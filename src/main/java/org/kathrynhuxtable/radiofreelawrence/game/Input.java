package org.kathrynhuxtable.radiofreelawrence.game;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Input {

	private final GameContext gameContext;

	private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

	public void input() {
			System.out.print("? ");
			String text = scanner.nextLine();

			parseInput(text);
	}

	private void parseInput(String input) {
		int arg1 = 0;
		int arg2 = 0;
		int status = 0;

//		String[] words = input.split("\\s+");
//		int index = 1;
//		for (String word : words) {
//			if (!gameContext.gameNode.getNoise().contains(word)) {
//				int arg;
//				if (gameContext.gameNode.getVerbs().containsKey(word)) {
//					HasRefno wordNode = (HasRefno) gameContext.gameNode.getVerbs().get(word);
//					arg = wordNode.getRefno();
//					if (status >= 0) {
//						status = index;
//					}
//				} else {
//					List<String> possibleKeys = gameContext.gameNode.getVerbs().keySet().stream()
//							.filter(v -> v.startsWith(word))
//							.toList();
//					if (possibleKeys.isEmpty()) {
//						arg = gameContext.getIntIdentifierValue("badword");
//						status = gameContext.getIntIdentifierValue("badsyntax");
//					} else if (possibleKeys.size() > 1) {
//						arg = gameContext.getIntIdentifierValue("ambigword");
//						status = gameContext.getIntIdentifierValue("badsyntax");
//					} else {
//						HasRefno wordNode = (HasRefno) gameContext.gameNode.getVerbs().get(possibleKeys.get(0));
//						arg = wordNode.getRefno();
//						if (status >= 0) {
//							status = index;
//						}
//					}
//				}
//				if (index == 1) {
//					arg1 = arg;
//				} else {
//					arg2 = arg;
//				}
//				index++;
//			}
//			if (index > 2) {
//				break;
//			}
//		}
//
//		gameContext.setIntIdentifierValue("arg1", arg1);
//		gameContext.setIntIdentifierValue("arg2", arg2);
//		gameContext.setIntIdentifierValue("status", status);
	}

}
