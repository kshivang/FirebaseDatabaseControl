package com.shivang.firebasedatabasemanager.revealAnimation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.Property;
import android.view.View;
import java.util.HashMap;
import java.util.Map;

class ViewRevealManager {
    private static final ClipRadiusProperty REVEAL = new ClipRadiusProperty();

    private Map<View, RevealValues> targets = new HashMap<>();

    ViewRevealManager() {

    }

    ObjectAnimator createAnimator(RevealValues data) {
        ObjectAnimator animator =
                ObjectAnimator.ofFloat(data, REVEAL, data.startRadius, data.endRadius);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationStart(Animator animation) {
                RevealValues values = getValues(animation);
                values.clip(true);
            }

            @Override public void onAnimationEnd(Animator animation) {
                RevealValues values = getValues(animation);
                values.clip(false);
                targets.remove(values.target());
            }
        });

        targets.put(data.target(), data);
        return animator;
    }

    private static RevealValues getValues(Animator animator) {
        return (RevealValues) ((ObjectAnimator) animator).getTarget();
    }

    /**
     * @return True if you don't want use Android native reveal animator
     * in order to use your own custom one
     */
    boolean hasCustomerRevealAnimator() {
        return false;
    }


    /**
     * Applies path clipping on a canvas before drawing child,
     * you should save canvas state before transformation and
     * restore it afterwards
     *
     * @param canvas Canvas to apply clipping before drawing
     * @param child Reveal animation target
     * @return True if transformation was successfully applied on
     * referenced child, otherwise child be not the target and
     * therefore animation was skipped
     */
    boolean transform(Canvas canvas, View child) {
        final RevealValues revealData = targets.get(child);
        return revealData != null && revealData.applyTransformation(canvas, child);
    }

    static final class RevealValues {
        private static final Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        static {
            debugPaint.setColor(Color.GREEN);
            debugPaint.setStyle(Paint.Style.FILL);
            debugPaint.setStrokeWidth(2);
        }

        final int centerX;
        final int centerY;

        final float startRadius;
        final float endRadius;

        // Flag that indicates whether view is clipping now, mutable
        boolean clipping;

        // Revealed radius
        float radius;

        // Animation target
        View target;

        // Android Canvas is tricky, we cannot clip circles directly with Canvas API
        // but it is allowed using Path, therefore we use it :|
        Path path = new Path();

        Region.Op op = Region.Op.REPLACE;

        RevealValues(View target, int centerX, int centerY, float startRadius, float endRadius) {
            this.target = target;
            this.centerX = centerX;
            this.centerY = centerY;
            this.startRadius = startRadius;
            this.endRadius = endRadius;
        }

        void radius(float radius) {
            this.radius = radius;
        }

        /** @return current clipping radius */
        float radius() {
            return radius;
        }

        /** @return Animating view */
        View target() {
            return target;
        }

        void clip(boolean clipping) {
            this.clipping = clipping;
        }

        /**
         * Applies path clipping on a canvas before drawing child,
         * you should save canvas state before transformation and
         * restore it afterwards
         *
         * @param canvas Canvas to apply clipping before drawing
         * @param child Reveal animation target
         * @return True if transformation was successfully  applied on
         * referenced child, otherwise child be not the target and
         * therefore animation was skipped
         */
        boolean applyTransformation(Canvas canvas, View child) {
            if (child != target || !clipping) {
                return false;
            }

            path.reset();
            // trick to applyTransformation animation, when even x & y translations are running
            path.addCircle(child.getX() + centerX, child.getY() + centerY, radius, Path.Direction.CW);

            canvas.clipPath(path, op);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                child.invalidateOutline();
            }
            return true;
        }
    }

    /**
     * Property animator. For performance improvements better to use
     * directly variable member (but it's little enhancement that always
     * caught as dangerous, let's see)
     */
    private static final class ClipRadiusProperty extends Property<RevealValues, Float> {

        ClipRadiusProperty() {
            super(Float.class, "supportCircularReveal");
        }

        @Override public void set(RevealValues data, Float value) {
            data.radius(value);
            data.target().invalidate();
        }

        @Override public Float get(RevealValues v) {
            return v.radius();
        }
    }

    /**
     * As class name cue's it changes layer type of {@link View} on animation createAnimator
     * in order to improve animation smooth & performance and returns original value
     * on animation end
     */
    static class ChangeViewLayerTypeAdapter extends AnimatorListenerAdapter {
        private RevealValues viewData;
        private int featuredLayerType;
        private int originalLayerType;

        ChangeViewLayerTypeAdapter(RevealValues viewData, int layerType) {
            this.viewData = viewData;
            this.featuredLayerType = layerType;
            this.originalLayerType = viewData.target.getLayerType();
        }

        @Override public void onAnimationStart(Animator animation) {
            viewData.target().setLayerType(featuredLayerType, null);
        }

        @Override public void onAnimationCancel(Animator animation) {
            viewData.target().setLayerType(originalLayerType, null);
        }

        @Override public void onAnimationEnd(Animator animation) {
            viewData.target().setLayerType(originalLayerType, null);
        }
    }
}
