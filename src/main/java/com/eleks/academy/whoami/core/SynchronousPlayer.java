package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.model.request.CharacterSuggestion;

public interface SynchronousPlayer {

	String getName();

	String getCharacterSuggestion();
	
	void suggest(CharacterSuggestion suggestion);

	boolean isSuggest();

	String getGameCharacter();

	void setGameCharacter(String gameCharacter);

	boolean isCharacterAssigned();

}
