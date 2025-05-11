package com.pentabytex.alshafimedledger.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object InsetsUtil {
    fun applyInsetsWithInitialPadding(view: View) {
        val initialPadding = arrayOf(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                initialPadding[0] + systemBars.left,
                initialPadding[1] + systemBars.top,
                initialPadding[2] + systemBars.right,
                initialPadding[3] + systemBars.bottom
            )
            insets
        }
    }
}
