## Eleks Engineering Academy (Spring 2022)

# WHO AM I GAME BACKEND PART 

## 1. Where to find front-end part 
GitHub front-end part: https://github.com/MinoUni/who-am-i-game-frontend-t3

Front-end branch ```develop``` = back-end ```develop```
## 2. Where to find deploy demo of the product
Heroku deploy demo: https://who-am-i-t3f.herokuapp.com/
## 3. REST API Specification
Path to openapi: ```src/main/resources/openapi.yml```.
## 4. Base guide
Currently working only ```Quick game```(MVP). Quick game - game session with 4 players.

1. Wait until 4 players connect to quick game. Simply can open demo in 4 tabs.
2. All players need to provide a ```Character suggestion``` before time outs. In suggestion form you also can change default name. Players that provide suggestion marks with ```✓(READY)``` status. If times out game will be disbanded and all players return to main screen.
3. If all players succeed with suggestion - game starts. Randomly 1 of the players become ```ASKER```, his character label marks green and he can provide a question. When asker provided question other players can answer on it with ```YES|NO|NOT_SURE```. If ```YES + NOT_SURE > NO``` player can ask once more else other player become ```ASKER```.
4. ```ASKER``` besides question can try to guess his character. Other players can answer only ```YES|NO```. If ```YES > NO``` player become ```Winner``` and leaves a game.
5. The last player who didn't guessed his character become ```Loser```.
## 5. How to run locally if demo dies
1. Clone back & front parts of the project.
   For front part follow guide that described in ```README```.
   
2.
