package org.kathrynhuxtable.radiofreelawrence.game;

import java.util.List;

public interface GamePlace extends GameFlag {
	String getName();
	List<String> getVerbs();
	List<String> getActions();

	String getBriefDescription();
	String getLongDescription();

//	String getSourceLocationText();
}
