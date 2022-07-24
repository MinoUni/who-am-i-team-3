package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.exception.GameNotFoundException;
import com.eleks.academy.whoami.core.exception.PlayerAlreadyInGameException;
import com.eleks.academy.whoami.core.exception.PlayerNotFoundException;
import com.eleks.academy.whoami.model.request.GuessAnswer;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.GameHistory;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.state.impl.ProcessingQuestion;
import com.eleks.academy.whoami.core.state.impl.SuggestingCharacters;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameSize;
import com.eleks.academy.whoami.model.response.*;
import com.eleks.academy.whoami.repository.GameRepository;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

	private final GameRepository gameRepository;

	@Override
	public Integer getAllPlayersCount() {
		return Math.toIntExact(this.gameRepository.getAllPlayers().count());
	}

	@Override
	public List<GameShortInfo> findAvailableGames(String player) {
		return this.gameRepository.findAllAvailable(player)
				.map(GameShortInfo::of)
				.toList();
	}

	@Override
	public Optional<GameDetails> findByIdAndPlayer(String id, String player) {
		return this.gameRepository.findById(id)
				.filter(game -> game.findPlayer(player).isPresent())
				.map(GameDetails::of);
	}

	@Override
	public Optional<TurnDetails> findTurnInfo(String id, String player) {
		var game = this.gameRepository.findById(id);
		if (game.isPresent() && game.get().getState() instanceof ProcessingQuestion) {
			return Optional.ofNullable(game.get().findTurnInfo(player));
		} else throw new GameNotFoundException("PROCESSING-QUESTION: Game with id[" + id + "] not found.");
	}

	@Override
	public Optional<GameHistory> findGameHistory(String id) {
		var game = this.gameRepository.findById(id);
		if (game.isPresent()) {
			return Optional.ofNullable(game.get().getGameHistory());
		} else throw new GameNotFoundException("findGameHistory: Game with id[" + id + "] not found.");
	}

	@Override
	public List<AllFields> findAllGamesInfo(String player) {
		return this.gameRepository.findAllGames(player).map(AllFields::of).toList();
	}

	@Override
	public Optional<GameDetails> createGame(String player, NewGameSize gameRequest) {

		Map<String, SynchronousGame> games = gameRepository.findAvailableQuickGames();

		if (games.isEmpty()) {

			final SynchronousGame game = gameRepository.save(new PersistentGame(gameRequest.getMaxPlayers()));
			enrollToGame(game.getId(), player);

			return gameRepository.findById(game.getId()).map(GameDetails::of);
		}

		var FirstGame = games.keySet().stream().findFirst().get();
		enrollToGame(games.get(FirstGame).getId(), player);

		return gameRepository.findById(games.get(FirstGame).getId()).map(GameDetails::of);
	}

	@Override
	public SynchronousPlayer enrollToGame(String id, String player) {
		if (this.gameRepository.findPlayerByHeader(player).isPresent()) {
			throw new PlayerAlreadyInGameException("ENROLL-TO-GAME: [" + player + "] already in other game.");
		} else this.gameRepository.savePlayer(player);

		return this.gameRepository.findById(id).get().enrollToGame(player);
	}

	@Override
	public Optional<LeaveDetails> leaveGame(String id, String player) {

		if (this.gameRepository.findPlayerByHeader(player).isPresent()) {

			var game = this.gameRepository.findById(id);

			if (game.isPresent()) {

				this.gameRepository.deletePlayerByHeader(player);
				var plToLeave = game.get().leaveGame(player).get();

				if (String.valueOf(game.get().getPlayersList().size()).equals("0")) {
					this.gameRepository.disbandGame(id);
				}

				return Optional.of(LeaveDetails.of(plToLeave, id));

			} else throw new GameNotFoundException("Game with id[" + id + "] not found.");

		} else throw new PlayerNotFoundException("[" + player + "] in game with id[" + id + "] not found.");

	}

	@Override
	public Optional<GameDetails> startGame(String id, String player) {
		throw new GameException("Not implemented yet.");
		/*
		*	return this.gameRepository.findById(id)
		* 			.filter(g -> g.getState().isReadyToNextState() &&
		* 					g.getState() instanceof SuggestingCharacters
		* 			)
		* 			.map(SynchronousGame::start)
		* 			.map(GameDetails::of);
		* */
	}

	@Override
	public void suggestCharacter(String id, String player, CharacterSuggestion suggestion) {
		this.gameRepository.findById(id)
				.filter(game -> game.getState() instanceof SuggestingCharacters)
				.ifPresentOrElse(game -> game.suggestCharacter(player, suggestion),
						() -> {
							throw new GameNotFoundException("SUGGESTING-CHARACTERS: Game with id[" + id + "] not found.");
						}
				);
	}

	@Override
	public void askQuestion(String id, String player, String message) {
		this.gameRepository.findById(id)
				.filter(game -> game.getState() instanceof ProcessingQuestion)
				.ifPresentOrElse(game -> game.askQuestion(player, message),
						() -> {
							throw new GameNotFoundException("PROCESSING-QUESTION: Game with id[" + id + "] not found.");
						}
				);
	}

	@Override
	public void answerQuestion(String id, String player, QuestionAnswer answer) {
		this.gameRepository.findById(id)
				.filter(game -> game.getState() instanceof ProcessingQuestion)
				.ifPresentOrElse(game -> game.answerQuestion(player, answer),
						() -> {
							throw new GameNotFoundException("PROCESSING-QUESTION: Game with id[" + id + "] not found.");
						}
				);
	}

	@Override
	public void submitGuess(String id, String player, String guess) {
		this.gameRepository.findById(id)
				.filter(game -> game.getState() instanceof ProcessingQuestion)
				.ifPresentOrElse(game -> game.submitGuess(player, guess),
						() -> {
							throw new GameNotFoundException("PROCESSING-QUESTION: Game with id[" + id + "] not found.");
						}
				);
	}

	@Override
	public void answerGuess(String id, String player, GuessAnswer answer) {
		this.gameRepository.findById(id)
				.filter(game -> game.getState() instanceof ProcessingQuestion)
				.ifPresentOrElse(game -> game.answerGuess(player, answer),
						() -> {
							throw new GameNotFoundException("PROCESSING-QUESTION: Game with id[" + id + "] not found.");
						}
				);
	}

}
