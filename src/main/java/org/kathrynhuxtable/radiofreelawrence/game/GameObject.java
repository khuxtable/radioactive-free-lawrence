package org.kathrynhuxtable.radiofreelawrence.game;

import java.util.List;

public interface GameObject {
	String getName();
	List<String> getVerbs();
	boolean isInVocabulary();

	String getInventoryDescription();
	String getBriefDescription();
	String getLongDescription();
	boolean isCommand(String verb);
	boolean isProc(String name);

	String getSourceLocationText();
}
