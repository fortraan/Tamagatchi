package com.smartstar.christopherjohnson.tamagatchi;

import android.animation.Animator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    // TODO: Sleeping
    // TODO: Hunger et al goes down in background
    // TODO: Music (?)

    private static final String DEBUG_TAG = "Pocket Pet";

    ProgressBar hungerBar;
    ProgressBar healthBar;
    ProgressBar affectionBar;

    GifImageView eeveeView;

    Button food;
    Button menu;

    volatile float hunger;
    volatile float health;
    volatile float affection;

    private final float hungerAffectionMultiplier = 10;
    private final float fullBoostThreshold = 75;
    private final float fullBoost = 0.5f;

    long hungerLoopTime = 2000;
    long healthLoopTime = 1000;
    long affectionLoopTime = 1000;

    volatile boolean exitLoop = false;

    private boolean paused = false;

    VelocityTracker vt = null;

    View.OnTouchListener rubListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = MotionEventCompat.getActionMasked(event);

            switch(action) {
                case (MotionEvent.ACTION_DOWN):
                    Log.d(DEBUG_TAG, "Action was DOWN");
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    Log.d(DEBUG_TAG, "Action was MOVE");
                    affection += 2;
                    return true;
                case (MotionEvent.ACTION_UP):
                    Log.d(DEBUG_TAG, "Action was UP");
                    return true;
                case (MotionEvent.ACTION_CANCEL):
                    Log.d(DEBUG_TAG, "Action was CANCEL");
                    return true;
                case (MotionEvent.ACTION_OUTSIDE):
                    Log.d(DEBUG_TAG, "Movement occurred outside bounds " +
                            "of current screen element");
                    return true;
                default:
                    return true;
            }
        }
    };

    Handler loopHandler;

    Runnable hungerLoop = new Runnable() {
        @Override
        public void run() {
            hunger = (hunger - 1 < 0) ? 0 : hunger - 1;
            hungerBar.setProgress((int) hunger);

            if (!exitLoop) {
                loopHandler.postDelayed(hungerLoop, hungerLoopTime);
            }
        }
    };

    Runnable healthLoop = new Runnable() {
        @Override
        public void run() {
            if (health < 100 && hunger > 0) {
                health++;
            } else if (hunger <= 0) {
                health--;
            }
            healthBar.setProgress((int) health);

            if (!exitLoop) {
                loopHandler.postDelayed(healthLoop, healthLoopTime);
            }
        }
    };

    Runnable affectionLoop = new Runnable() {
        @Override
        public void run() {
            affection = (affection - (1 + (hungerAffectionMultiplier / ((hunger + 1)))) < 0) ? 0 : affection - (1 + (hungerAffectionMultiplier / ((hunger + 1))));
            if (hunger > fullBoostThreshold) {
                affection = (affection + fullBoost > 100) ? 100 : affection + fullBoost;
            }
            affectionBar.setProgress((int) affection);

            if (!exitLoop) {
                loopHandler.postDelayed(affectionLoop, affectionLoopTime);
            }
        }
    };

    View.OnClickListener foodListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hunger += 10;
            if (hunger > 100) {
                hunger = 100;
            }
            affection += 5;
            if (affection > 100) {
                affection = 100;
            }
        }
    };

    View.OnClickListener pauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            paused = !paused;
            if (paused) {
                loopHandler.removeCallbacks(hungerLoop);
                loopHandler.removeCallbacks(healthLoop);
                loopHandler.removeCallbacks(affectionLoop);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hungerBar = (ProgressBar) findViewById(R.id.hungerBar);
        healthBar = (ProgressBar) findViewById(R.id.healthBar);
        affectionBar = (ProgressBar) findViewById(R.id.affectionBar);

        eeveeView = (GifImageView) findViewById(R.id.eevee);
        eeveeView.setOnTouchListener(this.rubListener);

        food = (Button) findViewById(R.id.food);
        menu = (Button) findViewById(R.id.menu);

        food.setOnClickListener(foodListener);

        SharedPreferences settings = getSharedPreferences("save", 0);

        hunger = settings.getFloat("hunger", 100);
        if (!settings.contains("hunger")) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat("hunger", 100);
            editor.apply();
        }
        hungerBar.setProgress((int) hunger);

        health = settings.getFloat("health", 100);
        if (!settings.contains("health")) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat("health", 100);
            editor.apply();
        }
        healthBar.setProgress((int) health);

        affection = settings.getFloat("affection", 100);
        if (!settings.contains("affection")) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat("affection", 100);
            editor.apply();
        }
        affectionBar.setProgress((int) affection);

        loopHandler = new Handler();
        loopHandler.postDelayed(this.hungerLoop, 0);
        loopHandler.postDelayed(this.healthLoop, 0);
        loopHandler.postDelayed(this.affectionLoop, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getSharedPreferences("save", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("hunger");
        editor.putFloat("hunger", hunger);
        editor.remove("health");
        editor.putFloat("health", health);
        editor.remove("affection");
        editor.putFloat("affection", affection);
        editor.apply();
        exitLoop = true;
    }

    private void goToHospital() {
        loopHandler.removeCallbacks(hungerLoop);
        loopHandler.removeCallbacks(healthLoop);
        loopHandler.removeCallbacks(affectionLoop);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.hospital);

        TextView header = (TextView) findViewById(R.id.hospitalHeading);
        TextView body = (TextView) findViewById(R.id.hospitalBody);

        header.setAlpha(0);
        header.animate().alpha(1).setDuration(700).setListener(null);

        body.setAlpha(0);
        body.animate().alpha(1).setDuration(700).setListener(null);

        //final MediaPlayer mp = MediaPlayer.create(this, R.raw);
        //mp.start();
    }
}
