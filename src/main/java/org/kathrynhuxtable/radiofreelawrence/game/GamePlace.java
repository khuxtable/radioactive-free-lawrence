package org.kathrynhuxtable.radiofreelawrence.game;

import java.util.List;

public interface GamePlace {
	String getName();
	List<String> getVerbs();

	String getBriefDescription();
	String getLongDescription();
	boolean isCommand(String verb);
	boolean isProc(String name);

	String getSourceLocationText();
}
