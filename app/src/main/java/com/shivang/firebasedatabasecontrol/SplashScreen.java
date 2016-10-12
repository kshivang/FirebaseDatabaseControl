package com.shivang.firebasedatabasecontrol;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by kshivang on 13/10/16.
 * Simple Splash Screen
 */

public class SplashScreen extends AppCompatActivity{

    View rlHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        rlHolder = findViewById(R.id.rl_intro_holder);

        rlHolder.post(new Runnable() {
            @Override
            public void run() {
                int cx = (rlHolder.getLeft() + rlHolder.getRight()) / 2;
                int cy = (rlHolder.getTop() + rlHolder.getBottom()) / 2;

                // get the final radius for the clipping circle
                int dx = Math.max(cx, rlHolder.getWidth() - cx);
                int dy = Math.max(cy, rlHolder.getHeight() - cy);
                float finalRadius = (float) Math.hypot(dx, dy);

                // Android native animator

                Animator animator =
                        ViewAnimationUtils.createCircularReveal(rlHolder, 0, cy, 0, finalRadius * 2);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(1000);
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                finish();
                            }
                        }, 2000);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        });
    }
}
