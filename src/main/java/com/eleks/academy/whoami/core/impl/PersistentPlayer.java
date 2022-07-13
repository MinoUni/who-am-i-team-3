package com.eleks.academy.whoami.core.impl;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;

public class PersistentPlayer implements SynchronousPlayer {

	private final String username;
	
	private String nickname;
	
	private String characterSuggestion;
	
	private boolean isSuggested = Boolean.FALSE;
	
	private String gameCharacter;
	
	private boolean isCharacterAssigned = Boolean.FALSE;
	
	public PersistentPlayer(String username) {
		this.username = Objects.requireNonNull(username);
		this.nickname = Objects.requireNonNull(nickname);
	}
	
	@Override
	public boolean isSuggest() {
		return isSuggested;
	}
	
	@Override
	public boolean isCharacterAssigned() {
		return isCharacterAssigned;
	}
	
	private void setNickName(String nickname) {
		this.nickname = nickname;
	}

	private void setCharacter(String character) {
		this.characterSuggestion = character;
	}
	
	@Override
	public String getUserName() {
		return this.username;
	}
	
	@Override
	public String getNickName() {
		return this.nickname;
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
//			setNickName(suggestion.getNickname());
			setCharacter(suggestion.getCharacter());
		}
		else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suggestion has already been submitted!");
		}
		
	}
	
}
