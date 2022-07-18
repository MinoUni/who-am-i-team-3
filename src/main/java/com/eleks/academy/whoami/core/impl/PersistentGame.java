package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameNotFoundException;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.core.state.ProcessingQuestion;
import com.eleks.academy.whoami.core.state.SuggestingCharacters;
import com.eleks.academy.whoami.core.state.WaitingForPlayers;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import com.eleks.academy.whoami.model.response.TurnDetails;

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
    public String getCurrentTurn() {
        return this.gameState.peek() instanceof ProcessingQuestion ?
                ((ProcessingQuestion) this.gameState.peek()).getCurrentTurn() : null;
    }

    @Override
    public void askQuestion(String player, String message) {
        if (findPlayer(player).isPresent()) {
            assert this.gameState.peek() != null;
            ((ProcessingQuestion) this.gameState.peek()).askQuestion(player, message);
        }
    }

    @Override
    public void submitGuess(String player, String guess) {
        if (findPlayer(player).isPresent()) {
            assert this.gameState.peek() != null;
            ((ProcessingQuestion) this.gameState.peek()).submitGuess(player, guess);
        }
    }

    @Override
    public void answerQuestion(String player, String answer) {
        if (findPlayer(player).isPresent()) {
            assert this.gameState.peek() != null;
            ((ProcessingQuestion) this.gameState.peek()).answerQuestion(player, answer);
        }
    }

    @Override
    public TurnDetails findTurnInfo(String player) {
        if (findPlayer(player).isPresent()) {
            assert this.gameState.peek() != null;
           return ((ProcessingQuestion) this.gameState.peek()).getTurnInfo();
        } else
            throw new GameNotFoundException("Game [" + this.getId() + "] not found [" + player + "].");
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
        return this.applyIfPresent(this.gameState.peek(), GameState::getPlayersList)
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
            assert gameState.peek() != null;
            ((WaitingForPlayers) gameState.peek()).add(newPlayer);
            assert gameState.peek() != null;
            if (gameState.peek().getPlayersInGame().equals("4")) {
                this.gameState.add(Objects.requireNonNull(this.gameState.poll()).next());
            }
            return newPlayer;
        } else
            throw new GameNotFoundException("Game [" + this.getId() + "] already at " + this.getStatus() + " state.");
    }

    @Override
    public void suggestCharacter(String player, CharacterSuggestion suggestion) {
        if (findPlayer(player).isPresent()) {
            assert gameState.peek() != null;
            ((SuggestingCharacters) gameState.peek()).suggestCharacter(player, suggestion);
            assert gameState.peek() != null;
            if (gameState.peek().isReadyToNextState()) {
                this.gameState.add(Objects.requireNonNull(this.gameState.poll()).next());
            }
        }
    }

    @Override
    public Optional<SynchronousPlayer> leaveGame(String player) {
        return this.applyIfPresent(this.gameState.peek(), gameState -> gameState.leave(player));
    }

    @Override
    public SynchronousGame start() {
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

    private String getToken() {
        return String.format("Player %d", Double.valueOf(Math.random() * 999).intValue());
    }

}
