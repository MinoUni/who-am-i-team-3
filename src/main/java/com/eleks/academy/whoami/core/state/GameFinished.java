package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;

import java.util.Optional;
import java.util.stream.Stream;

public final class GameFinished implements GameState {

	private final int playersInGame;

	private final int maxPlayers;

	public GameFinished(int playersInGame, int maxPlayers) {
		this.playersInGame = playersInGame;
		this.maxPlayers = maxPlayers;
	}

	@Override
	public GameState next() {
		return null;
	}

	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		return Optional.empty();
	}

	@Override
	public GameState getCurrentState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatus() {
		return null;
	}

	@Override
	public boolean isReadyToNextState() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<SynchronousPlayer> remove(String player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<SynchronousPlayer> getPlayersList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPlayersInGame() {
		return this.playersInGame;
	}
}
