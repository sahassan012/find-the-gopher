package edu.uic.cs478.find_the_gopher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameplayActivity extends AppCompatActivity {

    private static String MODE = "";
    private static int GOPHERS_POSITION;
    private ArrayList<Integer> movesTaken = new ArrayList<>();

    private Button player_one_turn;
    private Button player_two_turn;
    private Boolean player_one_turn_bool;
    private Boolean player_two_turn_bool;
    private Button switch_to_guess_by_guess;
    private Button switch_to_continuous;
    private Button play_again;

    private int player_twos_next_turn = -1;

    private TextView gameplay_mode_tv;

    private ArrayList<Integer> image_ids = new ArrayList<Integer>(100);

    private MyThreadHandler handler = new MyThreadHandler(this);

    private boolean started = false;

    GridView gameboard_gridview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);

        // Get buttons for players' moves from layout
        player_one_turn = (Button)findViewById(R.id.player1_button);
        player_two_turn = (Button)findViewById(R.id.player2_button);

        // Get buttons for switching game modes from layout
        switch_to_guess_by_guess = (Button) findViewById(R.id.button_switch_guess_by_guess);
        switch_to_continuous = (Button) findViewById(R.id.button_switch_continuous);

        // Get textview for displaying game mode from layout
        gameplay_mode_tv = (TextView) findViewById(R.id.gameplay_textView);

        // Get button to allow user to restart game when finished, initialize to INVISIBLE
        play_again = (Button) findViewById(R.id.button_play_again);
        play_again.setVisibility(View.INVISIBLE);

        // Generate random position for gopher to hide
        Random random_number = new Random();
        GOPHERS_POSITION = random_number.nextInt(100);

        // Add images of holes to ArrayList 100 times
        int i = 0;
        while(i < 100){
            // If it isn't the gopher's position
            if (i != GOPHERS_POSITION){
                image_ids.add(R.drawable.empty);
            }
            else { // Use gopher's image at its position
                image_ids.add(R.drawable.gophers_position);
            }
            i++;
        }

        // Get gridview from layout and set the adapter with ArrayList
        gameboard_gridview = (GridView) findViewById(R.id.gameboard);
        gameboard_gridview.setAdapter(new MyAdapter(this, image_ids));

        // If this is the first time starting a game mode, get game mode chosen from intent
        if (!started){
            MODE = getIntent().getStringExtra("gameplay mode");
            started = true;
        }

        // Handle switch to guess-by-guess mode button
        switch_to_guess_by_guess.setOnClickListener(view -> {

            // Update game mode string
            MODE = new String("");
            MODE = "guess-by-guess";

            // Set textview
            gameplay_mode_tv.setText("Guess-by-guess Mode");

            // If boolean for player one's turn is null,
            // meaning user hit this mode before first move, set to true
            if (player_one_turn_bool == null){
                player_one_turn_bool = true;
            }

            // Update buttons depending on whose turn it is
            if (player_one_turn_bool){
                player_one_turn.setEnabled(true);
                player_two_turn.setEnabled(false);
            }
            else {
                player_one_turn.setEnabled(false);
                player_two_turn.setEnabled(true);
            }

            // Listen to clicks for players' turns
            player_one_turn.setOnClickListener(view2 -> { this.playerOneMakesTurn(); });
            player_two_turn.setOnClickListener(view2 -> { this.playerTwoMakesTurn(); });

        });

        // Handle switch to continuous mode button
        switch_to_continuous.setOnClickListener(view -> {
            MODE = new String("");
            MODE = "continuous";

            player_one_turn.setEnabled(false);
            player_two_turn.setEnabled(false);
            gameplay_mode_tv.setText("Continuous Mode");

            Thread thread = new Thread(new Player1());
            thread.start();
        });

        // If user chose continuous mode, disable buttons for turns, update textview, and start game with player 1
        if (MODE.equals("continuous")){

            player_one_turn.setEnabled(false);
            player_two_turn.setEnabled(false);
            gameplay_mode_tv.setText("Continuous Mode");

            Thread thread = new Thread(new Player1());
            thread.start();

        }
        else if (MODE.equals("guess-by-guess")) {   // If user chose guess-by-guess mode, enable player 1's button and update textview

            player_one_turn.setEnabled(true);
            player_two_turn.setEnabled(false);
            gameplay_mode_tv.setText("Guess-by-guess Mode");

            player_one_turn.setOnClickListener(view -> { this.playerOneMakesTurn(); });
            player_two_turn.setOnClickListener(view -> { this.playerTwoMakesTurn(); });
        }
        else{ // Otherwise, something is wrong

            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();

        }
        play_again.setOnClickListener(view3->{finish();});
    }

    // Player1:
    //   Class that handles player one and its strategy to find the gopher.
    //   Worker thread # 1
    //     Strategy : To simply choose random positions between 0-99
    public class Player1 implements Runnable{
        @Override
        public void run() {

            // Generate random number for player 2 to make move
            Random random_number = new Random();
            int n = random_number.nextInt(100);

            // If game mode is continuous, sleep for a second
            if (MODE.equals("continuous")){
                try {
                    Thread.sleep(900);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Store random number as argument 1 of message and send it
            Message message = handler.obtainMessage(1);
            message.arg1 = n;
            handler.sendMessage(message);
        }
    }

    // Player2:
    //   Class that handles player one and its strategy to find the gopher.
    //   Worker thread # 2
    //     Strategy : To start but choosing 0 and increment the position for the next turn until 99
    public class Player2 implements Runnable{
        @Override
        public void run() {

            // Increment position using player two's strategy
            player_twos_next_turn++;
            int n = player_twos_next_turn;

            // If game mode is continuous, sleep for a second
            if (MODE.equals("continuous")){
                try {
                    Thread.sleep(900);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Store player two's next position as argument 1 of message and send it
            Message message2 = handler.obtainMessage(2);
            message2.arg1 = n;
            handler.sendMessage(message2);
        }
    }

    // playerOneMakesTurn:
    //   Creates and starts thread for player one to make a move
    private void playerOneMakesTurn(){
        // Create thread using player1 class
        Thread thread = new Thread(new Player1());
        thread.start();
    }

    // playerTwoMakesTurn:
    //   Creates and starts thread for player two to make a move using a handler
    private void playerTwoMakesTurn(){
        // Create handler that utilizes runnable and creates thread using player2 class
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Thread thread = new Thread(new Player2());
                thread.start();
            }
        });
    }

    // updateGameboard:
    //    Update's images on gridview depending on the players' moves
    public void updateGameboard(int i){

        // Get view at position i and convert to imageview
        View v = gameboard_gridview.getChildAt(i);
        ImageView image = (ImageView) v;

        // If it was player 2's turn, player 1 goes
        if (player_two_turn_bool) {
            image.setImageResource(R.drawable.player_1);
            image.setTag(R.drawable.player_1);
        }
        else{ // If it was player 1's turn, player 2 goes
            image.setImageResource(R.drawable.player_2);
            image.setTag(R.drawable.player_2);
        }

    }

    //  MyAdapter:
    //   Custom adapter class that takes list of image ids and handles displaying images based on their positions
    public class MyAdapter extends BaseAdapter{

        private List<Integer> ids;
        private Context context;

        // Constructor
        public MyAdapter(Context context, List<Integer> image_ids){
            this.ids = image_ids;
            this.context = context;
        }

        @Override
        public int getCount() {
            return ids.size();
        }

        @Override
        public Object getItem(int i) {
            return ids.get(i);
        }

        @Override
        public long getItemId(int i) {
            return ids.get(i);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            // Convert view to imageview object
            ImageView image = (ImageView) view;

            // If imageview is null, initialize
            if (image == null) {
                image = new ImageView(context);
                image.setLayoutParams(new GridView.LayoutParams(150, 150));
            }
            // Update tag and image
            image.setTag(ids.get(i));
            image.setImageResource(ids.get(i));
            return image;

        }
    }

    // MyThreadHandler:
    //   Main thread that handles the gameplay
    public class MyThreadHandler extends Handler {

        // Instance of GameplayActivity class
        GameplayActivity gamePlayActivity;

        // Constructor that stores activity passed as GameplayActivity instance
        public MyThreadHandler(GameplayActivity activity){
            gamePlayActivity = activity;
        }

        @Override
        public void handleMessage(@NonNull Message message) {
            super.handleMessage(message);

            // Store move that was sent in message
            int move = message.arg1;

            // Local gameplay activity
            GameplayActivity gameplay = gamePlayActivity;

            Toast.makeText(GameplayActivity.this, "Player " + message.what + " made a move at " + message.arg1, Toast.LENGTH_SHORT).show();

            // If it is an invalid move (exists in the list of moves that have been made)
            if (!checkIfValidMove(message.arg1)){

                // Make a disaster toast
                Toast.makeText(GameplayActivity.this, "P" + message.what +": Disaster! - Go again", Toast.LENGTH_SHORT).show();

                // If it was while game mode was continuous, make appropriate player take a turn
                if (message.what == 1 && MODE.equals("continuous")){
                    gameplay.playerOneMakesTurn();
                }
                else if (message.what == 2 && MODE.equals("continuous")){
                    gameplay.playerTwoMakesTurn();
                }
                return;
            }

            // If player one sent the message
            if (message.what == 1){

                // Disable player one's and enable player two's button
                player_one_turn_bool = false;
                player_two_turn_bool = true;

                // Update gameboard and the list of moves taken with the move
                gameplay.updateGameboard(message.arg1);
                gameplay.movesTaken.add(message.arg1);

                // If game mode is guess-by-guess
                if (MODE.equals("guess-by-guess")){

                    // If gopher has not found
                    if (!checkIfGopherWasFound(move)) {

                        // Enable buttons depending on whose turn it is
                        if (player_one_turn_bool == true) {
                            player_one_turn.setEnabled(true);
                            player_two_turn.setEnabled(false);
                        }
                        else {
                            player_one_turn.setEnabled(false);
                            player_two_turn.setEnabled(true);
                        }
                    }
                    else{ // If gopher has been found, disable all buttons, set textview, and show toast

                        gameplay_mode_tv.setText("Success - Player " + message.what + " found the gopher at " + message.arg1 + "!");
                        Toast.makeText(GameplayActivity.this, "Success - Player " + message.what + " found the gopher!!! ", Toast.LENGTH_SHORT).show();

                        player_one_turn.setEnabled(false);
                        player_two_turn.setEnabled(false);
                        switch_to_continuous.setEnabled(false);
                        switch_to_guess_by_guess.setEnabled(false);
                        play_again.setVisibility(View.VISIBLE);

                        return;
                    }

                }
                else if (MODE.equals("continuous")){ // If game mode is continuous
                    // If gopher is not found, player two makes move
                    if (!checkIfGopherWasFound(move)){
                        gameplay.playerTwoMakesTurn();
                    }
                    else{ // If gopher is found, disable all buttons, set textview and show toast

                        gameplay_mode_tv.setText("Success - Player " + message.what + " found the gopher at " + message.arg1 + "!");
                        Toast.makeText(GameplayActivity.this, "Success - Player " + message.what + " found the gopher!!! ", Toast.LENGTH_SHORT).show();

                        player_one_turn.setEnabled(false);
                        player_two_turn.setEnabled(false);
                        switch_to_continuous.setEnabled(false);
                        switch_to_guess_by_guess.setEnabled(false);
                        play_again.setVisibility(View.VISIBLE);

                        return;
                    }
                }
            } // If player one sent the message
            else if (message.what == 2){

                // Enable player one's and disable player two's button
                player_one_turn_bool = true;
                player_two_turn_bool = false;

                // Update gameboard and the list of moves taken with the move
                gameplay.updateGameboard(message.arg1);
                gameplay.movesTaken.add(message.arg1);

                //If game mode is guess-by-guess
                if (MODE.equals("guess-by-guess")) {

                    //If gopher wasn't found at the move made
                    if (!checkIfGopherWasFound(move)) {
                        //Enable button allowing other player to make move
                        if (player_one_turn_bool == true) {
                            player_one_turn.setEnabled(true);
                            player_two_turn.setEnabled(false);
                        }
                        else {
                            player_one_turn.setEnabled(false);
                            player_two_turn.setEnabled(true);
                        }
                    }
                    else{ // If gopher is found, disable all buttons, , allow user to play again, set textview and show toast

                        gameplay_mode_tv.setText("Success - Player " + message.what + " found the gopher at " + message.arg1 + "!");
                        Toast.makeText(GameplayActivity.this, "Success - Player " + message.what + " found the gopher!!! ", Toast.LENGTH_SHORT).show();

                        player_one_turn.setEnabled(false);
                        player_two_turn.setEnabled(false);
                        switch_to_continuous.setEnabled(false);
                        switch_to_guess_by_guess.setEnabled(false);
                        play_again.setVisibility(View.VISIBLE);

                        return;
                    }
                }
                else if (MODE.equals("continuous")){ // If game mode is continuous

                    //If gopher wasn't found at the move made, player one makes move
                    if (!checkIfGopherWasFound(move)){
                        gameplay.playerOneMakesTurn();
                    }
                    else{ // If gopher is found, disable all buttons, allow user to play again, set textview and show toast

                        gameplay_mode_tv.setText("Success - Player " + message.what + " found the gopher at " + message.arg1 + "!");
                        Toast.makeText(GameplayActivity.this, "Success - Player " + message.what + " found the gopher at " + message.arg1 + "!!", Toast.LENGTH_SHORT).show();

                        player_one_turn.setEnabled(false);
                        player_two_turn.setEnabled(false);
                        switch_to_continuous.setEnabled(false);
                        switch_to_guess_by_guess.setEnabled(false);
                        play_again.setVisibility(View.VISIBLE);

                        return;
                    }
                }
            }

            // Call showMessage and determine whether it was a near miss, close guess, or complete miss
            if (showMessage(message.arg1).equals("Near miss")){
                Toast.makeText(GameplayActivity.this, "P" + message.what + ": Near Miss!", Toast.LENGTH_SHORT).show();
            }
            else if (showMessage(message.arg1).equals("Close guess")){
                Toast.makeText(GameplayActivity.this, "P" + message.what +": Close Guess!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(GameplayActivity.this, "P" + message.what +": Complete Miss!", Toast.LENGTH_SHORT).show();
            }
        }

        //
        // showMessage:
        //   Takes integer for move and determines whether the move was a near miss, close guess,
        //    or complete miss by considering all the coordinates around the gopher's position
        private String showMessage(int move) {
            GameplayActivity gameplay = gamePlayActivity;

            // If move that was made was a near miss (1 away from gopher's position)
            if ( (gameplay.GOPHERS_POSITION - 1 == move) || (gameplay.GOPHERS_POSITION + 1 == move) || (gameplay.GOPHERS_POSITION + 10 == move) || (gameplay.GOPHERS_POSITION - 10 == move)
                    || (gameplay.GOPHERS_POSITION - 9 == move) || (gameplay.GOPHERS_POSITION - 11 == move) || (gameplay.GOPHERS_POSITION + 9 == move) || (gameplay.GOPHERS_POSITION + 11 == move) ){
                return "Near miss";
            } // If move that was made was a close guess (2 away from gopher's position in any direction)
            else if ( (gameplay.GOPHERS_POSITION - 2 == move) || (gameplay.GOPHERS_POSITION + 2 == move) || (gameplay.GOPHERS_POSITION + 20 == move) || (gameplay.GOPHERS_POSITION - 20 == move)
                    || (gameplay.GOPHERS_POSITION - 19 == move) || (gameplay.GOPHERS_POSITION - 18 == move) || (gameplay.GOPHERS_POSITION - 21 == move) || (gameplay.GOPHERS_POSITION - 22 == move)
                    || (gameplay.GOPHERS_POSITION - 8 == move) || (gameplay.GOPHERS_POSITION - 12 == move) || (gameplay.GOPHERS_POSITION + 8 == move) || (gameplay.GOPHERS_POSITION + 12 == move)
                    || (gameplay.GOPHERS_POSITION + 19 == move) || (gameplay.GOPHERS_POSITION + 18 == move) || (gameplay.GOPHERS_POSITION + 21 == move) || (gameplay.GOPHERS_POSITION + 22 == move)){
                return "Close guess";
            } // Otherwise, it was a complete miss
            else{
                return "Complete miss";
            }
        }

        // checkIfValidMove:
        //   Checks if integer value passed in already exists in the list of moves made by players
        //   Returns true or false
        private boolean checkIfValidMove(int move) {
            GameplayActivity gameplay = gamePlayActivity;
            if (gameplay.movesTaken.contains(move)){
                return false;
            }
            return true;
        }

        // checkIfGopherWasFound:
        //   Checks if integer value passed in matches the gopher's position
        //   Returns true or false
        private boolean checkIfGopherWasFound(int move){
            GameplayActivity gameplay = gamePlayActivity;
            if (gameplay.GOPHERS_POSITION == move){
                return true;
            }
            return false;
        }
    }
}