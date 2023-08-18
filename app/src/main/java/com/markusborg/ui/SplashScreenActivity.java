package com.markusborg.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

  // Splash screen timer
  private static int SPLASH_TIME_OUT = 4000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash_screen);

    new Handler().postDelayed(new Runnable() {

      @Override
      public void run() {
        // Starting app main activity after time out
        Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(i);
        // close this activity
        finish();
      }
    }, SPLASH_TIME_OUT);
  }

}