# WHO AM I GAME BACKEND

## Eleks Engineering Academy (Spring 2022)

Heroku deploy

```https://who-am-i-t3f.herokuapp.com/```

Frontend part

```https://github.com/MinoUni/who-am-i-game-frontend-t3```

Path to implemented endpoints

```src/main/resources/openapi.yml```

## Base guide

1. Play quick game, when 4 players connected game move to the next state.
   If player want to leave game - use leave game button or endpoint.

2. At ```Suggest-Character``` state, players can provide a suggestion.
   Suggestion include name & character fields.
   Game move to the next state when all 4 players provide their suggestions.
   If times out all players will be kicked from game.

3. At ```Question-Answer``` state 1 of the players become ```Asker```.
   He can provide a question or guess, other players need to vote.
   For questions players can vote [Yes/No/Not sure] & for guesses only [Yes/No].
   If yes + not sure > no, player can ask again, for guess -> player become winner
   and turn pass to next player, if not turn also pass to next player.
   Game stops when 3 player guessed their characters and last one become loser.
