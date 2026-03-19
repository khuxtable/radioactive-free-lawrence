package org.kathrynhuxtable.radiofreelawrence.game;

import java.util.List;

public interface GameObject {
	String getName();
	List<String> getVerbs();
	List<String> getActions();

	String getInventoryDescription();
	String getBriefDescription();
	String getLongDescription();

	GamePlace getLocation();
	void  setLocation(GamePlace location);

//	String getSourceLocationText();
}
