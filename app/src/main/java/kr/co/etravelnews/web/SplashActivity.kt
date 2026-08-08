package kr.co.etravelnews.web

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import kr.co.etravelnews.web.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private val splashDelayMs = 2000L

    private val openMain = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSystemBars(
            content = binding.root,
            lightStatusBar = true,
            lightNavigationBar = true,
        )

        val slideIn = AnimationUtils.loadAnimation(this, R.anim.splash_slide_in)
        binding.splashLogo.startAnimation(slideIn)
        handler.postDelayed(openMain, splashDelayMs)
    }

    override fun onDestroy() {
        handler.removeCallbacks(openMain)
        super.onDestroy()
    }
}
