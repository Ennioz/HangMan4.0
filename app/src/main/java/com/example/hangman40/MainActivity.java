package com.example.hangman40;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.media.MediaPlayer;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "HangmanPrefs";
    private static final String KEY_CURRENT_WORD = "currentWord";
    private static final String KEY_HIDDEN_WORD = "hiddenWord";
    private static final String KEY_ATTEMPTS_LEFT = "attemptsLeft";
    private static final String KEY_INCORRECT_GUESSES = "incorrectGuesses";
    private static final String KEY_SCORE = "score";
    private static final String KEY_HIGH_SCORE = "highScore";

    private String[] words = {"ex"};


    private String currentWord;
    private String hiddenWord;
    private int attemptsLeft;
    private int score;
    private int highScore;

    private TextView tvHiddenWord;
    private TextView tvIncorrectGuesses;
    private TextView tvMessage;
    private EditText etGuess;
    private Button btnSubmit;

    private TextView tvScore;

    private int[] hangmanImages = {
            R.drawable.hangman_0,
            R.drawable.hangman_1,
            R.drawable.hangman_2,
            R.drawable.hangman_3,
            R.drawable.hangman_4,
            R.drawable.hangman_5,
            R.drawable.hangman_6
    };

    private ImageView imageView;

    private HangmanDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private void initializeGame() {
        if (attemptsLeft <= 0) {

        }

        if (score > highScore) {
            highScore = score;
            SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_HIGH_SCORE, highScore);
            editor.apply();
        }

        Random random = new Random();
        currentWord = words[random.nextInt(words.length)];
        hiddenWord = new String(new char[currentWord.length()]).replace("\0", "-");
        attemptsLeft = 6;

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
            imageView.setImageResource(hangmanImages[6 - attemptsLeft]);

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

    private void showResultDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

            score++;
            tvScore.setText("Score: " + score);
            initializeGame();
            if (score > highScore) {
                highScore = score;
                SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_HIGH_SCORE, highScore);
                editor.apply();
            }

            showResultDialog("Congratulations!", "You won!");
        } else if (attemptsLeft <= 0) {
            tvMessage.setText("You lost! The word was: " + currentWord);
            tvScore.setText("Score: " + score);
            btnSubmit.setEnabled(false);

            showResultDialog("Sorry!", "You lost! The word was: " + currentWord);
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