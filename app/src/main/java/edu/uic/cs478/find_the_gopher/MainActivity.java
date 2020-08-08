package edu.uic.cs478.find_the_gopher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button_continuous = (Button) findViewById(R.id.button_continuous);
        Button button_guessByGuess = (Button) findViewById(R.id.button_guessbyguess);

        // When player chooses continuous game mode, create intent and start activity
        button_continuous.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GameplayActivity.class);
                intent.putExtra("gameplay mode", "continuous");
                startActivity(intent);
            }
        });

        // When player chooses guess-by-guess game mode, create intent and start activity
        button_guessByGuess.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GameplayActivity.class);
                intent.putExtra("gameplay mode", "guess-by-guess");
                startActivity(intent);
            }
        });

    }
}