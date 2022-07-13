package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameNotFoundException;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.core.state.WaitingForPlayers;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistentGame implements SynchronousGame {

    private final String id;

    private final int maxPlayers;

    private final Queue<GameState> gameState = new LinkedBlockingQueue<>();

    /*
     * Creates a new custom game (game room) and makes a first enrollment turn by a current
     * player so that he won't have to enroll to the game he created
     *
     * @param hostPlayer player to initiate a new game
     */
    public PersistentGame(String hostPlayer, Integer maxPlayers) {
        this.id = String.format("%d-%d", Instant.now().toEpochMilli(),
                Double.valueOf(Math.random() * 999).intValue());

        this.maxPlayers = maxPlayers;
        this.gameState.add(new WaitingForPlayers(this.maxPlayers));
    }

    /*
     * Creates a new quick game (game room)
     *
     * @param maxPlayers to initiate a new quick game
     */
    public PersistentGame(Integer maxPlayers) {
        this.id = String.format("%d-%d", Instant.now().toEpochMilli(),
                Double.valueOf(Math.random() * 999).intValue());

        this.maxPlayers = maxPlayers;
        this.gameState.add(new WaitingForPlayers(this.maxPlayers));
    }

    /*
     * Creates a new quick game (game room)
     *
     * @return {@code String} game unique identifier
     */
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public GameState getState() {
        return this.applyIfPresent(this.gameState.peek(), GameState::getCurrentState);
    }

    @Override
    public String getStatus() {
        return this.applyIfPresent(this.gameState.peek(), GameState::getStatus);
    }

    @Override
    public String getPlayersInGame() {
        return this.applyIfPresent(this.gameState.peek(), GameState::getPlayersInGame);
    }

    @Override
    public List<PlayerWithState> getPlayersList() {
        return applyIfPresent(this.gameState.peek(), GameState::getPlayersList)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SynchronousPlayer> findPlayer(String player) {
        return this.applyIfPresent(this.gameState.peek(), gameState -> gameState.findPlayer(player));
    }

    @Override
    public SynchronousPlayer enrollToGame(String player) {
        if (isAvailable()) {
            var newPlayer = new PersistentPlayer(player);
            ((WaitingForPlayers) gameState.peek()).add(newPlayer);
            if (gameState.peek().getPlayersInGame().equals("4")) {
                this.gameState.add(Objects.requireNonNull(this.gameState.poll()).next());
            }
            return newPlayer;
        } else
            throw new GameNotFoundException("Game [" + this.getId() + "] already at " + this.getStatus() + " state.");
    }

    private String getToken() {
        return String.format("Player %d", Double.valueOf(Math.random() * 999).intValue());
    }

    /*
     * TODO: refactor method
     * @return {@code true} if player were removed or {@code false} if player not in game
     * (?)@throws some custom gameException?
     *
     */
    @Override
    public Optional<SynchronousPlayer> leaveGame(String player) {
        return this.applyIfPresent(this.gameState.peek(), gameState -> gameState.leave(player));
    }

    @Override
    public SynchronousGame start() {
//		this.currentState.peek().next();
        this.gameState.add(Objects.requireNonNull(this.gameState.poll()).next());
        return this;
    }

    @Override
    public boolean isAvailable() {
        return this.gameState.peek() instanceof WaitingForPlayers;
    }

    private <T, R> R applyIfPresent(T source, Function<T, R> mapper) {
        return this.applyIfPresent(source, mapper, null);
    }

    private <T, R> R applyIfPresent(T source, Function<T, R> mapper, R fallback) {
        return Optional.ofNullable(source).map(mapper).orElse(fallback);
    }

}
