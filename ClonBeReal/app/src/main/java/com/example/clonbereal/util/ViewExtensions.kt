package com.example.clonbereal

import android.view.View
import android.view.animation.OvershootInterpolator

/**
 * Micro-animación de "pulsación": el view se encoge y rebota al soltar.
 * Da retroalimentación visual inmediata en cada botón.
 */
fun View.animatePress() {
    animate()
        .scaleX(0.92f)
        .scaleY(0.92f)
        .setDuration(90)
        .withEndAction {
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(OvershootInterpolator(2.5f))
                .setDuration(180)
                .start()
        }
        .start()
}
