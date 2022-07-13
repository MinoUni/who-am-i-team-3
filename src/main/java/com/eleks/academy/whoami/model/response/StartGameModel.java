package com.eleks.academy.whoami.model.response;

import java.util.List;

import com.eleks.academy.whoami.core.SynchronousGame;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartGameModel {
	
	private String gameId;
	
	private String gameState;
	
	private String playersInGame;
	
	private List<PlayerWithState> players;
	
	public static StartGameModel of(SynchronousGame game) {
		return StartGameModel.builder()
				.gameId(game.getId())
				.gameState(game.getStatus())
				.playersInGame(game.getPlayersInGame())
				.players(game.getPlayersList())
				.build();
	}
}
