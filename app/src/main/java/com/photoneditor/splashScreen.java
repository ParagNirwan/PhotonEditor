package com.photoneditor;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

public class splashScreen extends AppCompatActivity {
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        progressBar = findViewById(R.id.progressBar2);

        // Set up the animation
        animateProgressBar();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(splashScreen.this,MainActivity.class));finish();
            }
        },3000);

    }
    private void animateProgressBar() {



        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress",95);
        progressAnimator.setDuration(2500);
        progressAnimator.start();
    }

}