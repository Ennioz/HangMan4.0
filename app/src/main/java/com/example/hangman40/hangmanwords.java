
package com.example.hangman40;


public class hangmanwords {
    private int id;
    private String word;
    private String hint;

    public hangmanwords( String word, String hint) {
        this.word = word;
        this.hint = hint;
    }

    public String getWord() {
        return word;
    }

    public String getHint() {
        return hint;
    }

}


