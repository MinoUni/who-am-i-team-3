package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.exception.PlayerNotFoundException;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import com.eleks.academy.whoami.model.response.TurnDetails;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ProcessingQuestion implements GameState {

    private static final int DURATION = 1;

    private static final TimeUnit UNIT = TimeUnit.MINUTES;
    private final String currentPlayer;

    private final Map<String, PlayerWithState> players;

    public ProcessingQuestion(Map<String, PlayerWithState> players) {
        this.players = players;
        this.currentPlayer = players.keySet()
                .stream()
                .findFirst()
                .orElse(null);

//        this.players.forEach((k, v) -> {
//            if (k.equals(currentPlayer)) {
//                v.setState(PlayerState.ASKING);
//            } else v.setState(PlayerState.ANSWERING);
//        });
    }

    @Override
    public GameState next() {
        throw new GameException("Not implemented");
    }
    //TODO: Implement cycle of Q-A

    public void gameCycle(String player, String message) {

        if (player.equals(currentPlayer)) {
            askQuestion(player, message);
        } else {
            answerQuestion(player, message);
        }

    }

    public void askQuestion(String player, String message) {

    }

    public void submitGuess(String player, String guess) {

    }

    public void answerQuestion(String player, String answer) {

    }

    public TurnDetails getTurnInfo() {
        return new TurnDetails(this.players.get(currentPlayer).getPlayer(),
                new ArrayList<>(this.players.values()));
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
