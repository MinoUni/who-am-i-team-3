package com.eleks.academy.whoami.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.QuickGame;
import com.eleks.academy.whoami.repository.impl.GameInMemoryRepository;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

	private final GameInMemoryRepository mockGameRepository = mock(GameInMemoryRepository.class);	
	private final GameServiceImpl gameService = new GameServiceImpl(mockGameRepository);
	private final NewGameRequest gameRequest = new NewGameRequest();

	@BeforeEach
	void setup() {
		gameRequest.setMaxPlayers(2);
	}

	@Test
	void findAvailableGamesSuccessfulTest() throws Exception {
		String player = "player";
		Stream<SynchronousGame> games = Stream.empty();

		when(mockGameRepository.findAllAvailable(player)).thenReturn(games);

		List<GameLight> listOfGames = gameService.findAvailableGames(player);

		assertThat(listOfGames).isNotNull();
		assertThat(listOfGames).isEmpty();

		verify(mockGameRepository, times(1)).findAllAvailable(player);
	}

	@Test
	void createGameSuccessfulTest() {
		final String player = "player";
		final SynchronousGame game = new PersistentGame(player, gameRequest.getMaxPlayers());

		when(mockGameRepository.save(any(SynchronousGame.class))).thenReturn(game);

		Optional<GameDetails> createdGame = gameService.createGame(player, gameRequest);

		assertThat(createdGame).isNotNull();
		assertThat(createdGame.get().getId()).isEqualTo(game.getId());

		verify(mockGameRepository, times(1)).save(any(SynchronousGame.class));
	}
	
	@Test
	void findQuickGameSuccessfulTest() {
		
		final String playerId = "Test-Player";
		
		SynchronousGame createdGame = new PersistentGame(4);
		Map<String, SynchronousGame> availableGames = Map.of(createdGame.getId(), createdGame);
		Optional<SynchronousGame> game = Optional.of(createdGame);
		
		when(mockGameRepository.findAvailableQuickGames()).thenReturn(availableGames);
		when(mockGameRepository.findById(createdGame.getId())).thenReturn(game);
		
		Optional<QuickGame> quickGame = gameService.findQuickGame(playerId);
		
		assertThat(quickGame).isNotEmpty().isNotNull();
		assertThat(quickGame.get().getId()).isEqualTo(createdGame.getId());
		
		verify(mockGameRepository, times(2)).findById(eq(createdGame.getId()));
		verify(mockGameRepository, times(1)).findAvailableQuickGames();
	}
	
	@Test
	void findQuickGameSuccessful_CreateQuickGameBranchTest() {
		
		final String playerId = "Test-Player";
		
		SynchronousGame createdGame = new PersistentGame(4);
		Optional<SynchronousGame> game = Optional.of(createdGame);

		when(mockGameRepository.findAvailableQuickGames()).thenReturn(Collections.emptyMap());
		when(mockGameRepository.save(any(SynchronousGame.class))).thenReturn(createdGame);
		when(mockGameRepository.findById(createdGame.getId())).thenReturn(game);
		
		Optional<QuickGame> quickGame = gameService.findQuickGame(playerId);
		
		assertThat(quickGame).isNotEmpty().isNotNull();
		assertThat(quickGame.get().getId()).isEqualTo(createdGame.getId());
		
		verify(mockGameRepository, times(1)).findAvailableQuickGames();
		verify(mockGameRepository, times(1)).save(any(SynchronousGame.class));
		verify(mockGameRepository, times(2)).findById(eq(createdGame.getId()));
	}
	
  @Test
	void leaveGameSuccessfulTest() {
		SynchronousGame game = new PersistentGame(4);
		Optional<SynchronousGame> gameById = Optional.of(game);

		when(mockGameRepository.findById(game.getId())).thenReturn(gameById);
		
		Assertions.assertDoesNotThrow(() -> gameService.leaveGame(game.getId(), "player"));
		
		verify(mockGameRepository, times(1)).findById(game.getId());
	}
	
  @Test
	void leaveGameFailedWithNotFoundTest() {
		final String id = "542332";
		Optional<SynchronousGame> gameById = Optional.empty();
		
		when(mockGameRepository.findById(eq(id))).thenReturn(gameById);
		
		Assertions.assertThrows(ResponseStatusException.class, () -> gameService.leaveGame(id, "player"));
		
		verify(mockGameRepository, times(1)).findById(eq(id));
	}
  
}
