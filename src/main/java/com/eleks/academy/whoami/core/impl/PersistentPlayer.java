package com.eleks.academy.whoami.core.impl;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;

public class PersistentPlayer implements SynchronousPlayer {

	private String name;

	private final String id;

	private String characterSuggestion;
	
	private boolean isSuggested = Boolean.FALSE;
	
	private String gameCharacter;
	
	private boolean isCharacterAssigned = Boolean.FALSE;

	public PersistentPlayer(String id) {
		this.id = Objects.requireNonNull(id);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isSuggest() {
		return isSuggested;
	}
	
	@Override
	public boolean isCharacterAssigned() {
		return isCharacterAssigned;
	}
	
	private void setCharacter(String character) {
		this.characterSuggestion = character;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getCharacterSuggestion() {
		return this.characterSuggestion;
	}
	
	@Override
	public String getGameCharacter() {
		return gameCharacter;
	}
	
	@Override
	public void setGameCharacter(String gameCharacter) {
		if (!this.isCharacterAssigned) {
			this.isCharacterAssigned = Boolean.TRUE;
			this.gameCharacter = gameCharacter;
		}
	}
	
	@Override
	public void suggest(CharacterSuggestion suggestion) {
		if (!this.isSuggested) {
			this.isSuggested = Boolean.TRUE;
			setCharacter(suggestion.getCharacter());
			setName(suggestion.getName());
		}
		else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suggestion has already been submitted!");
		}
		
	}
	
}
