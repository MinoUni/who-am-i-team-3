package com.eleks.academy.whoami.api;

import com.eleks.academy.whoami.handler.ApiClient;
import com.eleks.academy.whoami.model.*;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class GameApiTest {

    private final String header = "X-Player";

    private final String contentType = "Content-Type";

    private final String json = "application/json";

    @RegisterExtension
    private static final WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8090)).build();

    @BeforeEach
    void prepare() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void shutDown() {
        wireMockServer.shutdownServer();
    }

    @Test
    void findAvailableGamesTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/get-available-games-response.json")) {
            String expectedResponse = new String(Objects.requireNonNull(in).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.get(WireMock.urlMatching("/api/v1/games"))
                            .withHeader(header, equalTo("Example"))
                            .willReturn(WireMock.aResponse().withBody(expectedResponse)
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(contentType, json)));

            GameApi gameApi = new GameApi(new ApiClient());

            GameShortInfo shortInfo = new GameShortInfo();
            shortInfo.setId("1234-UUID");
            shortInfo.setStatus("WaitingForPlayers");
            shortInfo.setPlayersInGame("3/4");

            assertThat(gameApi.findAvailableGames("Example")).isEqualTo(singletonList(shortInfo));
        }
    }

    @Test
    void createGameTest() throws IOException {
        try (InputStream in1 = getClass().getResourceAsStream("/models/game/post-create-game-request.json");
             InputStream in2 = getClass().getResourceAsStream("/models/game/post-create-game-response.json")) {
            String expectedRequest = new String(Objects.requireNonNull(in1).readAllBytes());
            String expectedResponse = new String(Objects.requireNonNull(in2).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.post(WireMock.urlMatching("/api/v1/games"))
                            .withHeader(header, equalTo("Example"))
                            .withHeader(contentType, equalTo(json))
                            .withRequestBody(equalToJson(expectedRequest))
                            .willReturn(WireMock.aResponse().withBody(expectedResponse)
                                    .withStatus(HttpStatus.CREATED.value())
                                    .withHeader(contentType, json)));

            GameApi gameApi = new GameApi(new ApiClient());

            NewGameSize newGameSize = new NewGameSize();
            newGameSize.maxPlayers(4);

            SynchronousPlayer synchronousPlayer = new SynchronousPlayer();
            synchronousPlayer.name("Example");
            synchronousPlayer.character("Batman");

            PlayerWithState playerWithState = new PlayerWithState();
            playerWithState.player(synchronousPlayer);
            playerWithState.answer(QuestionAnswer.NO);
            playerWithState.state(PlayerState.NOT_READY);

            GameDetails gameDetails = new GameDetails();
            gameDetails.id("1234-Uid");
            gameDetails.status("WaitingForPlayers");
            gameDetails.players(singletonList(playerWithState));

            assertThat(gameApi.createGame(newGameSize, "Example")).isEqualTo(gameDetails);
        }
    }

    @Test
    void findByIdTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/get-game-by-id-response.json")) {
            String expectedResponse = new String(Objects.requireNonNull(in).readAllBytes());
            wireMockServer
                    .stubFor(WireMock.get(WireMock.urlMatching("/api/v1/games/1234-Uid"))
                            .withHeader(header, equalTo("Example"))
                            .willReturn(WireMock.aResponse().withBody(expectedResponse)
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(contentType, json)));

            GameApi gameApi = new GameApi(new ApiClient());

            SynchronousPlayer synchronousPlayer = new SynchronousPlayer();
            synchronousPlayer.name("Example");
            synchronousPlayer.character("Batman");
            synchronousPlayer.id("PlayerId");

            PlayerWithState playerWithState = new PlayerWithState();
            playerWithState.player(synchronousPlayer);
            playerWithState.answer(QuestionAnswer.NO);
            playerWithState.state(PlayerState.NOT_READY);

            GameDetails gameDetails = new GameDetails();
            gameDetails.id("1234-Uid");
            gameDetails.status("WaitingForPlayers");
            gameDetails.players(singletonList(playerWithState));

            assertThat(gameApi.findById("Example", "1234-Uid")).isEqualTo(gameDetails);
        }
    }

    @Test
    void startGameTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/post-start-game-response.json")) {
            String expectedResponse = new String(Objects.requireNonNull(in).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.post(WireMock.urlMatching("/api/v1/games/1234-Uid"))
                            .withHeader(header, equalTo("Example"))
                            .willReturn(WireMock.aResponse().withBody(expectedResponse)
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(contentType, json)));

            GameApi gameApi = new GameApi(new ApiClient());

            SynchronousPlayer synchronousPlayer = new SynchronousPlayer();
            synchronousPlayer.name("Example");
            synchronousPlayer.character("Batman");
            synchronousPlayer.id("playerId");

            PlayerWithState playerWithState = new PlayerWithState();
            playerWithState.player(synchronousPlayer);
            playerWithState.answer(QuestionAnswer.NO);
            playerWithState.state(PlayerState.NOT_READY);

            GameDetails gameDetails = new GameDetails();
            gameDetails.id("1234-Uid");
            gameDetails.status("WaitingForPlayers");
            gameDetails.players(singletonList(playerWithState));

            assertThat(gameApi.startGame("Example", "1234-Uid")).isEqualTo(gameDetails);
        }
    }

    @Test
    void enrollToGameTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/post-enroll-player-response.json")) {
            String expectedResponse = new String(Objects.requireNonNull(in).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.post(WireMock.urlMatching("/api/v1/games/1234-Uid/players"))
                            .withHeader(header, equalTo("Example"))
                            .willReturn(WireMock.aResponse().withBody(expectedResponse)
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(contentType, json)));

            GameApi gameApi = new GameApi(new ApiClient());

            SynchronousPlayer synchronousPlayer = new SynchronousPlayer();
            synchronousPlayer.name("Example");
            synchronousPlayer.character("Batman");
            synchronousPlayer.id("playerId");

            assertThat(gameApi.enrollToGame("Example", "1234-Uid")).isEqualTo(synchronousPlayer);
        }
    }

    @Test
    void suggestCharacterTest() throws IOException {
        try (InputStream in1 = getClass().getResourceAsStream("/models/game/post-suggest-character-request.json")) {
            String expectedRequest = new String(Objects.requireNonNull(in1).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.post(WireMock.urlMatching("/api/v1/games/1234-Uid/characters"))
                            .withHeader(header, equalTo("Example"))
                            .withRequestBody(equalToJson(expectedRequest))
                            .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

            GameApi gameApi = new GameApi(new ApiClient());

            CharacterSuggestion characterSuggestion = new CharacterSuggestion();
            characterSuggestion.name("John");
            characterSuggestion.character("Batman");

            gameApi.suggestCharacter(characterSuggestion, "Example", "1234-Uid");
        }
    }

    @Test
    void findTurnInfoTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/get-current-turn-response.json")) {
            String expectedResponse = new String(Objects.requireNonNull(in).readAllBytes());
            wireMockServer
                    .stubFor(WireMock.get(WireMock.urlMatching("/api/v1/games/1234-Uid/turn"))
                            .withHeader(header, equalTo("Example"))
                            .willReturn(WireMock.aResponse().withBody(expectedResponse)
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(contentType, json)));

            GameApi gameApi = new GameApi(new ApiClient());

            SynchronousPlayer synchronousPlayer = new SynchronousPlayer();
            synchronousPlayer.name("Example");
            synchronousPlayer.character("Batman");
            synchronousPlayer.id("playerId");

            PlayerWithState playerWithState = new PlayerWithState();
            playerWithState.player(synchronousPlayer);
            playerWithState.answer(QuestionAnswer.NO);
            playerWithState.state(PlayerState.NOT_READY);

            TurnDetails turnDetails = new TurnDetails();
            turnDetails.players(singletonList(playerWithState));
            turnDetails.currentPlayer(synchronousPlayer);

            assertThat(gameApi.findTurnInfo("Example", "1234-Uid")).isEqualTo(turnDetails);
        }
    }

    @Test
    void askQuestionTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/post-ask-question-request.json")) {
            String expectedRequest = new String(Objects.requireNonNull(in).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.post(WireMock.urlMatching("/api/v1/games/1234-Uid/question"))
                            .withHeader(header, equalTo("Example"))
                            .withRequestBody(equalToJson(expectedRequest))
                            .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

            GameApi gameApi = new GameApi(new ApiClient());

            Message message = new Message();
            message.message("Question");

            gameApi.askQuestion(message, "Example", "1234-Uid");
        }
    }

    @Test
    void answerQuestionTest() {
        wireMockServer
                .stubFor(WireMock.post(WireMock.urlMatching("/api/v1/games/1234-Uid/question/answer\\?answer=YES"))
                        .withHeader(header, equalTo("Example"))
                        .withQueryParam("answer", equalTo("YES"))
                        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

        GameApi gameApi = new GameApi(new ApiClient());

        QuestionAnswer questionAnswer = QuestionAnswer.YES;

        gameApi.answerQuestion("Example", "1234-Uid", questionAnswer);
    }

    @Test
    void submitGuessTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/post-submit-guess-request.json")) {
            String expectedRequest = new String(Objects.requireNonNull(in).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.post(WireMock.urlMatching("/api/v1/games/1234-Uid/guess"))
                            .withHeader(header, equalTo("Example"))
                            .withRequestBody(equalToJson(expectedRequest))
                            .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

            GameApi gameApi = new GameApi(new ApiClient());

            Message message = new Message();
            message.message("Guess");

            gameApi.submitGuess(message, "Example", "1234-Uid");
        }
    }

    @Test
    void answerGuessTest() {
        wireMockServer
                .stubFor(WireMock.post(WireMock.urlMatching("/api/v1/games/1234-Uid/guess/answer\\?answer=YES"))
                        .withHeader(header, equalTo("Example"))
                        .withQueryParam("answer", equalTo("YES"))
                        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

        GameApi gameApi = new GameApi(new ApiClient());

        GuessAnswer guessAnswer = GuessAnswer.YES;

        gameApi.answerGuess("Example", "1234-Uid", guessAnswer);
    }

    @Test
    void getGameHistoryTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/get-game-history-response.json")) {
            String expectedResponse = new String(Objects.requireNonNull(in).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.get(WireMock.urlMatching("/api/v1/games/1234-Uid/history"))
                            .willReturn(WireMock.aResponse().withBody(expectedResponse)
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(contentType, json)));

            GameApi gameApi = new GameApi(new ApiClient());

            AnswersHistory answersHistory = new AnswersHistory();
            answersHistory.player("John");
            answersHistory.answer("YES");

            QuestionsHistory questionsHistory = new QuestionsHistory();
            questionsHistory.player("Max");
            questionsHistory.type("question");
            questionsHistory.question("question text");
            questionsHistory.answers(singletonList(answersHistory));

            GameHistory gameHistory = new GameHistory();
            gameHistory.questions(singletonList(questionsHistory));
            gameHistory.currentQuestion(questionsHistory);

            assertThat(gameApi.getGameHistory("1234-Uid")).isEqualTo(gameHistory);
        }
    }

    @Test
    void leaveGameTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/delete-leave-game-response.json")) {
            String expectedResponse = new String(Objects.requireNonNull(in).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.delete(WireMock.urlMatching("/api/v1/games/1234-Uid/leave"))
                            .withHeader(header, equalTo("Example"))
                            .willReturn(WireMock.aResponse().withBody(expectedResponse)
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(contentType, json)));

            GameApi gameApi = new GameApi(new ApiClient());

            LeaveDetails leaveDetails = new LeaveDetails();
            leaveDetails.id("1234-Uid");
            leaveDetails.name("Example");

            assertThat(gameApi.leaveGame("Example", "1234-Uid")).isEqualTo(leaveDetails);
        }
    }

    @Test
    void getPlayerCountTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/models/game/get-all-players-count-response.json")) {
            String expectedResponse = new String(Objects.requireNonNull(in).readAllBytes());

            wireMockServer
                    .stubFor(WireMock.get(WireMock.urlMatching("/api/v1/games/all-players-count"))
                            .withHeader(header, equalTo("Example"))
                            .willReturn(WireMock.aResponse().withBody(expectedResponse)
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(contentType, json)));

            GameApi gameApi = new GameApi(new ApiClient());

            Integer playersCount = 4;

            assertThat(gameApi.getAllPlayersCount("Example")).isEqualTo(playersCount);
        }
    }
}
