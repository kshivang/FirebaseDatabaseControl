package com.shivang.firebasedatabasemanager.revealAnimation;

import android.view.ViewGroup;

/**
 * Indicator for internal API that {@link ViewGroup} support
 * Circular Reveal animation
 */
interface RevealViewGroup {

    /**
     * @return Bridge between view and circular reveal animation
     */
    ViewRevealManager getViewRevealManager();
}
