package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.configuration.GameControllerAdvice;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.request.NewGameSize;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameHistory;
import com.eleks.academy.whoami.model.response.LeaveDetails;
import com.eleks.academy.whoami.model.response.TurnDetails;
import com.eleks.academy.whoami.service.impl.GameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    private final GameServiceImpl gameService = mock(GameServiceImpl.class);
    private final GameController gameController = new GameController(gameService);
    private final NewGameSize gameRequest = new NewGameSize();
    private MockMvc mockMvc;

    private final String PLAYER = "X-Player";

    @BeforeEach
    public void setMockMvc() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(gameController)
                .setControllerAdvice(new GameControllerAdvice())
                .build();
        gameRequest.setMaxPlayers(3);
    }

    @Test
    void findAvailableGames() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/games")
                                .header(PLAYER, "player"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotHaveJsonPath());
    }

    @Test
    void findAllGamesInfo() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.get("/games/info")
                                .header(PLAYER, "player"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotHaveJsonPath());
    }

    @Test
    void createGameSuccessful() throws Exception {
        GameDetails gameDetails = new GameDetails();
        gameDetails.setId("12613126");
        gameDetails.setStatus("WaitingForPlayers");

        when(gameService.createGame(eq("player"), any(NewGameSize.class)))
                .thenReturn(Optional.of(gameDetails));

        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games")
                                .header(PLAYER, "player")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "maxPlayers": 2
                                        }
                                        """
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value("12613126"))
                .andExpect(jsonPath("status").value("WaitingForPlayers"));

        verify(gameService, times(1)).createGame(anyString(), any(NewGameSize.class));
    }

    @Test
    void createGameFailedWithBadRequestException() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games")
                                .header(PLAYER, "player")
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
    void findByIdSuccessful() throws Exception {
        GameDetails gameDetails = new GameDetails("151515", "WaitingForPlayers", List.of());

        when(this.gameService.findByIdAndPlayer(eq(gameDetails.getId()), eq("playerId")))
                .thenReturn(Optional.of(gameDetails));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/games/{id}", gameDetails.getId())
                        .header(PLAYER, "playerId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(gameDetails.getId()))
                .andExpect(jsonPath("status").value(gameDetails.getStatus()));

        verify(gameService, times(1)).findByIdAndPlayer(eq(gameDetails.getId()), eq("playerId"));
    }

    @Test
    void findByIdNotFound() throws Exception {
        when(this.gameService.findByIdAndPlayer(anyString(), anyString())).thenReturn(Optional.empty());

        this.mockMvc.perform(MockMvcRequestBuilders.get("/games/1516")
                        .header(PLAYER, "playerId"))
                .andExpect(status().isNotFound());

        verify(gameService, times(1)).findByIdAndPlayer(anyString(), anyString());
    }

    @Test
    void enrollToGameSuccessful() throws Exception {
        var player = new PersistentPlayer("1", "2");

        when(this.gameService.enrollToGame(anyString(), anyString())).thenReturn(player);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/games/{id}/players", "9999")
                        .header(PLAYER, "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(player.getId()))
                .andExpect(jsonPath("name").value(player.getName()));

        verify(gameService, times(1)).enrollToGame(anyString(), anyString());
    }

    @Test
    void suggestCharacter() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/{id}}/characters", "1234")
                                .header(PLAYER, "playerId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": " Usop",
                                            "character": " char"
                                        }"""))
                .andExpect(status().isOk());
    }

    @Test
    void suggestCharacterFailedNameValidationWithBadRequest() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/{id}}/characters", "1234")
                                .header(PLAYER, "playerId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "s",
                                            "character": "char"
                                        }"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"Validation failed!\"," +
                        "\"details\":[\"Name must be between 2 and 50 characters long\"]}"));
    }

    @Test
    void suggestCharacterFailedCharacterValidationWithBadRequest() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/{id}}/characters", "1234")
                                .header(PLAYER, "playerId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Scorp",
                                            "character": "c@!"
                                        }"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"Validation failed!\"," +
                        "\"details\":[\"Character contains special symbols.\"]}"));
    }

    @Test
    void findTurnInfoSuccessful() throws Exception {
        var turn = new TurnDetails();

        when(this.gameService.findTurnInfo(eq("12345"), eq("playerId"))).thenReturn(Optional.of(turn));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/games/{id}/turn", "12345")
                        .header(PLAYER, "playerId"))
                .andExpect(status().isOk());

        verify(this.gameService, times(1)).findTurnInfo(eq("12345"), eq("playerId"));
    }

    @Test
    void findTurnInfoNotFound() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/games/{id}/turn", "12345")
                        .header(PLAYER, "playerId"))
                .andExpect(status().isNotFound());
    }

    @Test
    void askQuestionSuccessful() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/{id}}/question", "1234")
                                .header(PLAYER, "playerId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "message": "Am i a human?"
                                        }
                                        """))
                .andExpect(status().isOk());
    }

    @Test
    void askQuestionQuestionValidationFailed() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/{id}}/question", "1234")
                                .header(PLAYER, "playerId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "message": "a"
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"Validation failed!\"," +
                        "\"details\":[\"Message must be between 2 and 256 characters long.\"]}"));
    }

    @Test
    void answerQuestionSuccessful() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/{id}}/question/answer", "1234")
                                .header(PLAYER, "playerId")
                                .param("answer", "YES"))
                .andExpect(status().isOk());
    }

    @Test
    void answerQuestionQuestionValidationFailed() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/{id}}/question/answer", "1234")
                                .header(PLAYER, "playerId")
                                .param("answer", "maybe"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitGuessSuccessful() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/{id}}/guess", "1234")
                                .header(PLAYER, "playerId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "message": "Am i a Java?"
                                        }
                                        """))
                .andExpect(status().isOk());
    }

    @Test
    void submitGuessValidationFailed() throws Exception {
        this.mockMvc.perform(
                        MockMvcRequestBuilders.post("/games/{id}}/guess", "1234")
                                .header(PLAYER, "playerId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "message": "a"
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"Validation failed!\"," +
                        "\"details\":[\"Message must be between 2 and 256 characters long.\"]}"));
    }

    @Test
    void answerGuessSuccessful() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/games/{id}/guess/answer", "123")
                        .header(PLAYER, "playerId")
                        .param("answer", "NO"))
                .andExpect(status().isOk());
    }

    @Test
    void answerGuessValidationFailed() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/games/{id}/guess/answer", "123")
                        .header(PLAYER, "playerId")
                        .param("answer", "don't know"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllPlayersCountTest() throws Exception {
        Integer response = 0;
        when(this.gameService.getAllPlayersCount()).thenReturn(response);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/games/all-players-count")
                        .header(PLAYER, "playerId"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(this.gameService, times(1)).getAllPlayersCount();
    }

    @Test
    void getGameHistorySuccessful() throws Exception {
        var response = new GameHistory();

        when(this.gameService.findGameHistory(anyString()))
                .thenReturn(Optional.of(response));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/games/{id}/history", "12345"))
                .andExpect(status().isOk());

        verify(this.gameService, times(1)).findGameHistory(anyString());
    }

    @Test
    void getGameHistoryNotFound() throws Exception {
        when(this.gameService.findGameHistory(anyString()))
                .thenReturn(Optional.empty());

        this.mockMvc.perform(MockMvcRequestBuilders.get("/games/{id}/history", "123"))
                .andExpect(status().isNotFound());

        verify(this.gameService, times(1)).findGameHistory(anyString());
    }

    @Test
    void leaveGameSuccessful() throws Exception {
        var response = new LeaveDetails("686863", "Test-Player");

        when(this.gameService.leaveGame(response.getId(), response.getName()))
                .thenReturn(Optional.of(response));

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/games/{id}/leave", response.getId())
                        .header(PLAYER, response.getName())
                        .content("""
                                {
                                    "id": " {id}"
                                    "name": {player}",
                                }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(response.getId()))
                .andExpect(jsonPath("name").value(response.getName()));

        verify(this.gameService, times(1))
                .leaveGame(eq(response.getId()), eq(response.getName()));
    }

    @Test
    void leaveGameNotFoundGameToLeaveFrom() throws Exception {
        when(this.gameService.leaveGame(anyString(), anyString()))
                .thenReturn(Optional.empty());

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/games/{id}/leave", "12345")
                        .header(PLAYER, "playerId"))
                .andExpect(status().isNotFound());

        verify(this.gameService, times(1)).leaveGame(anyString(), anyString());
    }

    @Test
    void startGameSuccessful() throws Exception {
        GameDetails gameDetails = new GameDetails("gameId", "gameStatus", List.of());

        when(this.gameService.startGame(anyString(), anyString()))
                .thenReturn(Optional.of(gameDetails));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/games/{id}", gameDetails.getId())
                        .header(PLAYER, "playerId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(gameDetails.getId()))
                .andExpect(jsonPath("status").value(gameDetails.getStatus()));

        verify(this.gameService, times(1)).startGame(anyString(), anyString());
    }

    @Test
    void startGameNotFound() throws Exception {
        when(this.gameService.startGame(anyString(), anyString()))
                .thenReturn(Optional.empty());

        this.mockMvc.perform(MockMvcRequestBuilders.post("/games/{id}", "12345")
                        .header(PLAYER, "playerId"))
                .andExpect(status().isNotFound());

        verify(this.gameService, times(1)).startGame(anyString(), anyString());
    }

}
