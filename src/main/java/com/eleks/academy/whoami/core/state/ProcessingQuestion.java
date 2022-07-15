package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.exception.PlayerNotFoundException;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

// TODO: Implement makeTurn(...) and next() methods, pass a turn to next player
public final class ProcessingQuestion implements GameState {

	private final String currentPlayer;
	
	private final Map<String, PlayerWithState> players;
	
	public ProcessingQuestion(Map<String, PlayerWithState> players) {
		this.players = players;
		this.currentPlayer = players.keySet()
				.stream()
				.findAny()
				.orElse(null);
	}

	@Override
	public GameState next() {
		throw new GameException("Not implemented");
	}

	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		var result = Optional.ofNullable(this.players.get(player));
		if (result.isEmpty()) {
			throw new PlayerNotFoundException("PROCESSING-QUESTION: [" + player + "] not found.");
		}
		return Optional.ofNullable(result.get().getPlayer());
	}

	public String getCurrentTurn() {
		return this.currentPlayer;
	}

	@Override
	public GameState getCurrentState() {
		return this;
	}

	@Override
	public String getStatus() {
		return this.getClass().getName();
	}

	@Override
	public boolean isReadyToNextState() {
		return false;
	}

	@Override
	public Optional<SynchronousPlayer> leave(String player) {
		if (findPlayer(player).isPresent()) {
			return Optional.of(this.players.remove(player).getPlayer());
		} else throw new PlayerNotFoundException("[" + player + "] not found.");
	}

	@Override
	public Stream<PlayerWithState> getPlayersList() {
		return this.players.values().stream();
	}

	@Override
	public String getPlayersInGame() {
		return Integer.toString(this.players.size());
	}

}
