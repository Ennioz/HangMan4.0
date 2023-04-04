package com.example.hangman40;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    private InputStream inputStream;

    public CSVReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public List<hangmanwords> read() {
        List<hangmanwords> resultList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                hangmanwords word = new hangmanwords(row[0], row[1]);
                resultList.add(word);

            }

        } catch (IOException e) {
            throw new RuntimeException("Error in reading CSV file: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
        return resultList;
    }
}
