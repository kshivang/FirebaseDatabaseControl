/*
 *   Copyright (C) 2017  Shivang<shivang.iitk@gmail.com>
 *
 *   This file is part of Firebase Database Manager.
 *
 *       Firebase Database Manager is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       Firebase Database Manager is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with Firebase Database Manager.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shivang.firebasedatabasemanager.revealAnimation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Created by kshivang on 13/03/17.
 *
 */

public final class ViewAnimationUtils {

    private final static boolean LOLLIPOP_PLUS = SDK_INT >= LOLLIPOP;

    /**
     * Returns an Animator which can animate a clipping circle.
     * <p>
     * Any shadow cast by the View will respect the circular clip from this animator.
     * <p>
     * Only a single non-rectangular clip can be applied on a View at any time.
     * Views clipped by a circular reveal animation take priority over
     * {@link android.view.View#setClipToOutline(boolean) View Outline clipping}.
     * <p>
     * Note that the animation returned here is a one-shot animation. It cannot
     * be re-used, and once started it cannot be paused or resumed.
     *
     * @param view The View will be clipped to the clip circle.
     * @param centerX The x coordinate of the center of the clip circle.
     * @param centerY The y coordinate of the center of the clip circle.
     * @param startRadius The starting radius of the clip circle.
     * @param endRadius The ending radius of the clip circle.
     */
    public static Animator createCircularReveal(View view, int centerX, int centerY,
                                                float startRadius, float endRadius) {

        return createCircularReveal(view, centerX, centerY, startRadius, endRadius,
                View.LAYER_TYPE_SOFTWARE);
    }

    /**
     * Returns an Animator which can animate a clipping circle.
     * <p>
     * Any shadow cast by the View will respect the circular clip from this animator.
     * <p>
     * Only a single non-rectangular clip can be applied on a View at any time.
     * Views clipped by a circular reveal animation take priority over
     * {@link android.view.View#setClipToOutline(boolean) View Outline clipping}.
     * <p>
     * Note that the animation returned here is a one-shot animation. It cannot
     * be re-used, and once started it cannot be paused or resumed.
     *
     * @param view The View will be clipped to the clip circle.
     * @param centerX The x coordinate of the center of the clip circle.
     * @param centerY The y coordinate of the center of the clip circle.
     * @param startRadius The starting radius of the clip circle.
     * @param endRadius The ending radius of the clip circle.
     * @param layerType View layer type {@link View#LAYER_TYPE_HARDWARE} or {@link
     * View#LAYER_TYPE_SOFTWARE}
     */
    private static Animator createCircularReveal(View view, int centerX, int centerY,
                                                 float startRadius, float endRadius, int layerType) {

        if (!(view.getParent() instanceof RevealViewGroup)) {
            throw new IllegalArgumentException("Parent must be instance of RevealViewGroup");
        }

        RevealViewGroup viewGroup = (RevealViewGroup) view.getParent();
        ViewRevealManager rm = viewGroup.getViewRevealManager();

        if (!rm.hasCustomerRevealAnimator() && LOLLIPOP_PLUS) {
            return android.view.ViewAnimationUtils.createCircularReveal(view, centerX, centerY,
                    startRadius, endRadius);
        }

        ViewRevealManager.RevealValues viewData = new ViewRevealManager.RevealValues(view, centerX, centerY, startRadius, endRadius);
        ObjectAnimator animator = rm.createAnimator(viewData);

        if (layerType != view.getLayerType()) {
            animator.addListener(new ViewRevealManager.ChangeViewLayerTypeAdapter(viewData, layerType));
        }
        return animator;
    }
}
