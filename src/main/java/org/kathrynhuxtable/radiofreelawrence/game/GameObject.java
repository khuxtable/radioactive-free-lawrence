package org.kathrynhuxtable.radiofreelawrence.game;

import java.util.List;

public interface GameObject {
	String getName();
	List<String> getActions();
	boolean isInVocabulary();

	String getInventoryDescription();
	String getBriefDescription();
	String getLongDescription();
	boolean isCommand(String verb);
	boolean isProc(String name);

	GamePlace getLocation();
	void  setLocation(GamePlace location);

	String getSourceLocationText();
}
