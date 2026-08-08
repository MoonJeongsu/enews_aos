package kr.co.etravelnews.web

import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

fun ComponentActivity.setupSystemBars(
    content: View,
    lightStatusBar: Boolean,
    lightNavigationBar: Boolean = true,
    statusBarBackdrop: View? = null,
) {
    enableEdgeToEdge()
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = lightStatusBar
        isAppearanceLightNavigationBars = lightNavigationBar
    }
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        if (statusBarBackdrop != null) {
            statusBarBackdrop.updateLayoutParams {
                height = insets.top
            }
            view.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom,
            )
        } else {
            view.updatePadding(
                left = insets.left,
                top = insets.top,
                right = insets.right,
                bottom = insets.bottom,
            )
        }
        windowInsets
    }
}
