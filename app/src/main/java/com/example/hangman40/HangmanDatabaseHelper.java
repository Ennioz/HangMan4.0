package com.example.hangman40;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HangmanDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HangManXX.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "words";
    public static final String COLUMN_WORD = "word";
    public static final String COLUMN_HINT = "hint";

    private final Context mContext;

    public HangmanDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the tables and initial data for your database
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_WORD + " TEXT, " +
                COLUMN_HINT + " TEXT);";
        db.execSQL(createTableQuery);

        importDataFromCSV(db, "hangmandata.csv");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // handle database upgrades if needed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void importDataFromCSV(SQLiteDatabase db, String csvFileName) {
        InputStream inputStream = null;
        try {
            inputStream = mContext.getAssets().open(csvFileName);
        } catch (IOException e) {
            Log.e("Hangman", "Error opening " + csvFileName + ": " + e.getMessage());
            return;
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length != 2) {
                    Log.e("Hangman", "Skipping bad CSV row: " + line);
                    continue;
                }
                String word = columns[0].trim();
                String hint = columns[1].trim();
                String insertQuery = "INSERT INTO " + TABLE_NAME + " ( " + COLUMN_WORD + ", " + COLUMN_HINT + ") " +
                        "VALUES ('" + word + "', '" + hint + "')";
                db.execSQL(insertQuery);
                Log.d("Hangman", "Inserted word: " + word + ", hint: " + hint);
            }
        } catch (IOException e) {
            Log.e("Hangman", "Error reading CSV file: " + e.getMessage());
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                Log.e("Hangman", "Error closing CSV file: " + e.getMessage());
            }
        }
    }
}
