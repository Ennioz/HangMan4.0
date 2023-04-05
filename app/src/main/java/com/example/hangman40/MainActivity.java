package com.example.hangman40;
import static com.example.hangman40.HangmanDatabaseHelper.COLUMN_WORD;
import static com.example.hangman40.HangmanDatabaseHelper.TABLE_NAME;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.media.MediaPlayer;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "HangmanPrefs";
    private static final String KEY_CURRENT_WORD = "currentWord";
    private static final String KEY_HIDDEN_WORD = "hiddenWord";
    private static final String KEY_ATTEMPTS_LEFT = "attemptsLeft";
    private static final String KEY_INCORRECT_GUESSES = "incorrectGuesses";
    private SharedPreferences prefs;
    private static final String KEY_SCORE = "score";private static final String KEY_HIGH_SCORE = "highScore";
    //private String[] words = {"ex"};
    private ArrayList<String> wordsList = new ArrayList<>();private String currentWord;private String hiddenWord;private String currentHint;private int attemptsLeft;private int score;private int highScore;private TextView tvHiddenWord;private TextView tvIncorrectGuesses;private TextView tvMessage;private EditText etGuess;private Button btnSubmit;private TextView tvScore;private int[] hangmanImages = {
            R.drawable.hangman_0,
            R.drawable.hangman_1,
            R.drawable.hangman_2,
            R.drawable.hangman_3,
            R.drawable.hangman_4,
            R.drawable.hangman_5,
            R.drawable.hangman_6,
            R.drawable.hangman_7
    };
    private ImageView imageView;
    private HangmanDatabaseHelper dbHelper;private SQLiteDatabase db;
    private Button bntPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);



        dbHelper = new HangmanDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        dbHelper.importDataFromCSV(db, "hangmandata.csv");


        tvHiddenWord = findViewById(R.id.tvHiddenWord);
        tvIncorrectGuesses = findViewById(R.id.tvIncorrectGuesses);
        tvMessage = findViewById(R.id.tvMessage);
        etGuess = findViewById(R.id.etGuess);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvScore = findViewById(R.id.tvScore);
        imageView = findViewById(R.id.imageView);


        // Display the initial score at the beginning of the game
        score = 0;
        tvScore.setText("Score: " + score);

        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset the score to 0 and update the score display
                score = 0;
                tvScore.setText("Score: " + score);

                initializeGame();
            }
        });

        Button btnHint = findViewById(R.id.btnHint);
        btnHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHintDialog();
            }
        });

        Button btnHighScore = findViewById(R.id.btnHighScore);
        btnHighScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighScoreDialog();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String guess = etGuess.getText().toString().toLowerCase();
                if (guess.length() == 1) {
                    submitGuess(guess);
                } else {
                    tvMessage.setText("Please enter a single letter.");
                }
            }
        });


        initializeGame();
    }
    private void showHintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hint");
        builder.setMessage(currentHint);
        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void initializeGame() {
        // Remove the lines resetting the score to 0
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_SCORE, score);
        editor.apply();

        // Retrieve words and hints from the database
        ArrayList<String[]> wordAndHintList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_WORD + ", " + HangmanDatabaseHelper.COLUMN_HINT + " FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                String word = cursor.getString(0);
                String hint = cursor.getString(1);
                wordAndHintList.add(new String[]{word, hint});
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Use random word and hint from ArrayList
        Random random = new Random();
        int selectedIndex = random.nextInt(wordAndHintList.size());
        currentWord = wordAndHintList.get(selectedIndex)[0];
        currentHint = wordAndHintList.get(selectedIndex)[1];

        hiddenWord = new String(new char[currentWord.length()]).replace("\0", "-");
        attemptsLeft = 7;

        tvHiddenWord.setText(hiddenWord);
        tvIncorrectGuesses.setText("");
        tvMessage.setText("");
        etGuess.setText("");
        btnSubmit.setEnabled(true);
        imageView.setImageResource(hangmanImages[0]);
    }


    private void submitGuess(String guess) {
        MediaPlayer mediaPlayer;

        if (currentWord.contains(guess)) {
            updateHiddenWord(guess);

            // Play the sound for guessing a single letter correctly
            mediaPlayer = MediaPlayer.create(this, R.raw.correct_letter);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Release the MediaPlayer resources once the sound has finished playing
                    mp.release();
                }
            });
            mediaPlayer.start();
        } else {
            attemptsLeft--;
            tvIncorrectGuesses.append(guess + " ");

            // Update the hangman image
            imageView.setImageResource(hangmanImages[7 - attemptsLeft]);

            // Play the sound for a wrong guess
            mediaPlayer = MediaPlayer.create(this, R.raw.wrong_guess);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Release the MediaPlayer resources once the sound has finished playing
                    mp.release();
                }
            });
            mediaPlayer.start();
        }
        checkGameStatus();

        // Clear the input field
        etGuess.getText().clear();
    }


    private void updateHiddenWord(String guess) {
        StringBuilder sb = new StringBuilder(hiddenWord);
        for (int i = 0; i < currentWord.length(); i++) {
            if (currentWord.charAt(i) == guess.charAt(0)) {
                sb.setCharAt(i, guess.charAt(0));
            }
        }
        hiddenWord = sb.toString();
        tvHiddenWord.setText(hiddenWord);
    }

    private void showResultDialog(String title, String message, boolean isWinner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        // Inflate the custom layout containing the ImageView
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_result, null);
        ImageView imageView = view.findViewById(R.id.imageView);

        // Load a random GIF based on whether the player won or lost
        int gifId;
        if (isWinner) {
            gifId = getResources().getIdentifier("win_gif_" + (new Random().nextInt(7) + 1), "raw", getPackageName());
            // Remove the extra score increment line
        } else {
            gifId = getResources().getIdentifier("lose_gif_" + (new Random().nextInt(13) + 1), "raw", getPackageName());
        }
        Glide.with(this).load(gifId).into(imageView);

        builder.setView(view);

        if (isWinner) {
            builder.setPositiveButton("Resume", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    initializeGame();
                }
            });
        } else {
            builder.setPositiveButton("OK", null);
        }
        builder.setNegativeButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                score = 0; // Reset the score
                tvScore.setText("Score: " + score);
                initializeGame();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    private void checkGameStatus() {
        MediaPlayer mediaPlayer;

        if (hiddenWord.equals(currentWord)) {
            tvMessage.setText("You won!");
            btnSubmit.setEnabled(false);

            // Add 100 points to the score when the player wins
            score += 100;

            // Update the score TextView
            tvScore.setText("Score: " + score);

            if (score > highScore) {
                highScore = score;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_HIGH_SCORE, highScore);
                editor.apply();
            }

            // Play the sound for guessing the entire word correctly
            mediaPlayer = MediaPlayer.create(this, R.raw.word_guessed);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Release the MediaPlayer resources once the sound has finished playing
                    mp.release();
                }
            });
            mediaPlayer.start();

            // Show a dialog with a random "winner" GIF
            showResultDialog("Congratulations!", "You won!", true);
        } else if (attemptsLeft <= 0) {
            tvMessage.setText("You lost! The word was: " + currentWord);
            tvScore.setText("Score: " + score);
            btnSubmit.setEnabled(false);

            // Play the sound for losing the game
            mediaPlayer = MediaPlayer.create(this, R.raw.game_over_sound);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Release the MediaPlayer resources once the sound has finished playing
                    mp.release();
                }
            });
            mediaPlayer.start();
            // Show a dialog with a random "loser" GIF
            showResultDialog("Sorry!", "You lost! The word was: " + currentWord, false);
        } else {
            tvMessage.setText("Attempts left: " + attemptsLeft);
        }
    }


    private void showHighScoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("High Score");
        builder.setMessage("Your high score: " + highScore);
        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}