package com.example.hangman40;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;


public class StartPageActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startpage);


        imageView = findViewById(R.id.imageView);
        Glide.with(this)
                .load(R.raw.hangmanstart)
                .into(imageView);


        Button btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartPageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
