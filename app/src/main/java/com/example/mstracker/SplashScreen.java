package com.example.mstracker;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {

    // Total time before moving to MainActivity
    private static final int SPLASH_DURATION_MS = 2900;
    private View ring1;
    private View ring2;
    private View ring3;
    private ImageView logo;
    private TextView title;
    private TextView sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get views
        ring1 = findViewById(R.id.ring1);
        ring2 = findViewById(R.id.ring2);
        ring3 = findViewById(R.id.ring3);
        logo  = findViewById(R.id.logoIcon);
        title = findViewById(R.id.appTitle);
        sub   = findViewById(R.id.appSubtitle);

        // Start animation sequence
        startRipples(ring1, ring2, ring3);
        startLogoAnim(logo, 550);
        startWordmarkAnim(title, sub, 1100);

        // Navigate to main after splash
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, HomeScreen.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION_MS);
    }

    /**
     * Fires 3 rings that scale up from 1x → 4x and fade out, staggered 180ms apart.
     */
    private void startRipples(View ring1, View ring2, View ring3) {
        View[] rings = {ring1, ring2, ring3};
        int[] delays = {80, 260, 440};

        for (int i = 0; i < rings.length; i++) {
            View ring = rings[i];
            int delay = delays[i];

            ring.postDelayed(() -> {
                // Scale X
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(ring, View.SCALE_X, 0.3f, 4.2f);
                // Scale Y
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(ring, View.SCALE_Y, 0.3f, 4.2f);
                // Fade out
                ObjectAnimator fade = ObjectAnimator.ofFloat(ring, View.ALPHA, 0.85f, 0f);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(scaleX, scaleY, fade);
                set.setDuration(1300);
                set.setInterpolator(new AccelerateDecelerateInterpolator());
                set.start();
            }, delay);
        }
    }

    /**
     * Logo icon springs in with OvershootInterpolator — scale 0 → 1 with a slight overshoot.
     */
    private void startLogoAnim(ImageView logo, int delay) {
        logo.postDelayed(() -> {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0f, 1f);
            ObjectAnimator alpha  = ObjectAnimator.ofFloat(logo, View.ALPHA,   0f, 1f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY, alpha);
            set.setDuration(500);
            set.setInterpolator(new OvershootInterpolator(1.6f));
            set.start();
        }, delay);
    }

    /**
     * Title and subtitle slide up and fade in, subtitle 120ms after the title.
     */
    private void startWordmarkAnim(TextView title, TextView subtitle, int delay) {
        // Title
        title.postDelayed(() -> {
            ObjectAnimator fadeIn      = ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f);
            ObjectAnimator slideUp     = ObjectAnimator.ofFloat(title, View.TRANSLATION_Y, 24f, 0f);
            AnimatorSet titleSet = new AnimatorSet();
            titleSet.playTogether(fadeIn, slideUp);
            titleSet.setDuration(420);
            titleSet.setInterpolator(new DecelerateInterpolator(2f));
            titleSet.start();
        }, delay);

        // Subtitle — 120ms after title
        subtitle.postDelayed(() -> {
            ObjectAnimator fadeIn  = ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 1f);
            ObjectAnimator slideUp = ObjectAnimator.ofFloat(subtitle, View.TRANSLATION_Y, 24f, 0f);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(fadeIn, slideUp);
            subSet.setDuration(420);
            subSet.setInterpolator(new DecelerateInterpolator(2f));
            subSet.start();
        }, delay + 120);
    }
}