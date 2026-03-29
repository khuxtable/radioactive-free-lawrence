package org.kathrynhuxtable.radiofreelawrence.game;

import java.util.Iterator;

public interface GameAction {

	void doAction(String verb);
	Iterator<Object> iterator();

	int doMessage(String verb, int arg);
}
