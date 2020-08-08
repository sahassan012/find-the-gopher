# Find The Gopher
## About

The gopher hides in any of the holes in the field's ground. The goal of this game is to find the hole that contains the gopher. 
As a user, you can choose two game modes: guess-by-guess or continuous.In guess-by-guess game mode, you get to choose when the players will make a move. In the continuous mode, the players make moves one after another. 

## How it works
There are two worker threads that play the game against each other and try to find the gopher using two different strategies.
These threads use a job queue, a looper, and a handler. The main UI thread manages the game. The worker threads communicate with each other and the main thread using handlers.  

Whenever a thread takes a turn, there are one of the five responses shown:
1. Success - gopher is found and the thread that guessed wins the game
2. Near miss - the gopher is in one of the 8 holes adjacent to the gopher's hole
3. Close guess - a thread guessed a hole that is 2 holes away from where the gopher is in any direction.
4. Complete miss - in all other cases 
5. Disaster - a thread guesses a hole that was already guessed before


## How to run(in Android Studio):

Open project in Android Studio, create a virtual device using AVD (using Pixel 3 XL API 28) and click 'run app'
