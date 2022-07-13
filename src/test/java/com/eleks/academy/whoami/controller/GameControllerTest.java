package com.eleks.academy.whoami.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.eleks.academy.whoami.configuration.GameControllerAdvice;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.LeaveModel;
import com.eleks.academy.whoami.model.response.PlayerSuggestion;
import com.eleks.academy.whoami.model.response.QuickGame;
import com.eleks.academy.whoami.service.impl.GameServiceImpl;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

	private final GameServiceImpl gameService = mock(GameServiceImpl.class);
	private final GameController gameController = new GameController(gameService);
	private final NewGameRequest gameRequest = new NewGameRequest();
	private MockMvc mockMvc;

	@BeforeEach
	public void setMockMvc() {
		mockMvc = MockMvcBuilders.standaloneSetup(gameController)
				.setControllerAdvice(new GameControllerAdvice()).build();
		gameRequest.setMaxPlayers(3);
	}

	@Test
	void findAvailableGames() throws Exception {
		this.mockMvc.perform(
						MockMvcRequestBuilders.get("/games")
								.header("X-Player", "player"))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotHaveJsonPath());
	}

	@Test
	void createGame() throws Exception {
		GameDetails gameDetails = new GameDetails();
		gameDetails.setId("12613126");
		gameDetails.setStatus("WaitingForPlayers");
		when(gameService.createGame(eq("player"), any(NewGameRequest.class))).thenReturn(Optional.of(gameDetails));
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games")
								.header("X-Player", "player")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "maxPlayers": 2
										}"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("id").value("12613126"))
				.andExpect(jsonPath("status").value("WaitingForPlayers"));
	}

	@Test
	void createGameFailedWithException() throws Exception {
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games")
								.header("X-Player", "player")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "maxPlayers": null
										}"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("{\"message\":\"Validation failed!\"," +
						"\"details\":[\"maxPlayers must not be null\"]}"));
	}
	
	@Test
	void suggestCharacter() throws Exception {
		
		final String header = "Test-Player";

		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games/1234/characters")
								.header("X-Player", header)
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "nickname": " Usop",
										    "character": " char"
										}"""))
				.andExpect(status().isOk());
	}
	
	@Test
	void findQuickGameSuccessful() throws Exception {
		String playerId = "Test-Player";

		Optional<QuickGame> availableGame = Optional.of(new QuickGame("1111", "WaitingForPlayers", true, "1", null));
		
		when(gameService.findQuickGame(playerId)).thenReturn(availableGame);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/games/quick")
				.header("X-Player", playerId)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
		
		assertThat(availableGame).isNotEmpty().isNotNull();
		
		verify(gameService, times(1)).findQuickGame(eq(playerId));
		
	}
	
	@Test
	void findQuickGameFailed_WithBadRequest() throws Exception {
		String playerId = "Test-Player";

		Optional<QuickGame> availableGame = Optional.empty();
		
		when(gameService.findQuickGame(playerId)).thenReturn(availableGame);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/games/quick")
				.header("X-Player", playerId)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isNotFound());
		
		assertThat(availableGame).isEmpty().isNotNull();
		
		verify(gameService, times(1)).findQuickGame(eq(playerId));
		
	}
	
	@Test
	void leaveGameTest() throws Exception {
		final String id = "686863";
		
		Optional<LeaveModel> response = Optional.of(new LeaveModel("Test-Player", id));
		
		when(gameService.leaveGame(id, "Test-Player")).thenReturn(response);
		
		this.mockMvc.perform(MockMvcRequestBuilders.delete("/games/{id}/leave", id)
					.header("X-Player", "Test-Player")
					.content("""
							{
							    "username": " Test-Player",
							    "gameId": " {id}"
							}"""))
					.andExpect(status().isOk())
					.andExpect(jsonPath("username").value("Test-Player"))
					.andExpect(jsonPath("gameId").value(id));
		
		verify(gameService, times(1)).leaveGame(eq(id), eq("Test-Player"));
	}
  
}
