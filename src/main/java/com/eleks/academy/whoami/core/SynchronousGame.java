package com.eleks.academy.whoami.core;

import java.util.List;
import java.util.Optional;

import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.model.response.PlayerWithState;

public interface SynchronousGame {

	Optional<SynchronousPlayer> findPlayer(String player);

	String getId();

	SynchronousPlayer enrollToGame(String player);

	String getPlayersInGame();

	String getStatus();

	boolean isAvailable();

	SynchronousGame start();

	Optional<SynchronousPlayer> leaveGame(String player);

	List<PlayerWithState> getPlayersList();

	GameState getState();

}
