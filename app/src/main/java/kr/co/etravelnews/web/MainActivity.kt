package kr.co.etravelnews.web

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import kr.co.etravelnews.web.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lastBackPressedAt = 0L

    private val bannerImages = intArrayOf(
        R.drawable.banner_dasom_01,
        R.drawable.banner_dasom_02,
        R.drawable.banner_dasom_03,
    )
    private var bannerIndex = 0
    private val bannerHandler = Handler(Looper.getMainLooper())
    private val bannerRotateRunnable = object : Runnable {
        override fun run() {
            if (!::binding.isInitialized) return
            bannerIndex = (bannerIndex + 1) % bannerImages.size
            showBanner(bannerIndex)
            bannerHandler.postDelayed(this, BANNER_ROTATE_INTERVAL_MS)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSystemBars(
            content = binding.root,
            lightStatusBar = false,
            lightNavigationBar = true,
            statusBarBackdrop = binding.statusBarBackdrop,
        )

        bannerIndex = savedInstanceState?.getInt(STATE_BANNER_INDEX, 0) ?: 0
        setupWebView()
        setupPartnerBanner()
        binding.retryButton.setOnClickListener { reloadHome() }
        setupBackPress()

        if (savedInstanceState == null) {
            reloadHome()
        } else {
            binding.webView.restoreState(savedInstanceState)
        }
    }

    private fun setupPartnerBanner() {
        showBanner(bannerIndex)
        binding.partnerBanner.setOnClickListener {
            openPartnerBannerUrl()
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
            binding.statusBarBackdrop.updateLayoutParams {
                height = systemBars.top
            }
            view.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            binding.partnerBannerContainer.visibility =
                if (imeVisible) View.GONE else View.VISIBLE
            windowInsets
        }
        binding.root.requestApplyInsets()
    }

    private fun showBanner(index: Int) {
        binding.partnerBanner.scaleType =
            if (index == SANTORINI_BANNER_INDEX) {
                ImageView.ScaleType.FIT_XY
            } else {
                ImageView.ScaleType.CENTER_CROP
            }
        binding.partnerBanner.setImageResource(bannerImages[index])
    }

    private fun startBannerRotation() {
        bannerHandler.removeCallbacks(bannerRotateRunnable)
        if (bannerImages.size > 1) {
            bannerHandler.postDelayed(bannerRotateRunnable, BANNER_ROTATE_INTERVAL_MS)
        }
    }

    private fun stopBannerRotation() {
        bannerHandler.removeCallbacks(bannerRotateRunnable)
    }

    private fun openPartnerBannerUrl() {
        val uri = Uri.parse(getString(R.string.partner_banner_url))
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure {
            Toast.makeText(this, R.string.partner_banner_open_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.visibility =
                    if (newProgress in 1..99) View.VISIBLE else View.GONE
                binding.progressBar.progress = newProgress
            }
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                val url = request?.url ?: return false
                return handleExternalUrl(url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showContent()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                if (request?.isForMainFrame == true) {
                    showError()
                }
            }
        }
    }

    private fun handleExternalUrl(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase().orEmpty()
        return when {
            scheme == "http" || scheme == "https" -> false
            scheme == "tel" || scheme == "mailto" || scheme == "sms" || scheme == "geo" -> {
                runCatching {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
                true
            }
            scheme == "intent" -> {
                runCatching {
                    val parsed = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                    try {
                        startActivity(parsed)
                    } catch (_: Exception) {
                        val fallback = parsed.getStringExtra("browser_fallback_url")
                        if (!fallback.isNullOrBlank()) {
                            binding.webView.loadUrl(fallback)
                        }
                    }
                }
                true
            }
            else -> {
                runCatching {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
                true
            }
        }
    }

    private fun reloadHome() {
        showContent()
        binding.webView.loadUrl(getString(R.string.home_url))
    }

    private fun showContent() {
        binding.errorContainer.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }

    private fun showError() {
        binding.webView.visibility = View.INVISIBLE
        binding.errorContainer.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                        return
                    }
                    val now = System.currentTimeMillis()
                    if (now - lastBackPressedAt < 2000L) {
                        finish()
                    } else {
                        lastBackPressedAt = now
                        Toast.makeText(
                            this@MainActivity,
                            R.string.exit_confirm,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
        )
    }

    override fun onStart() {
        super.onStart()
        startBannerRotation()
    }

    override fun onStop() {
        stopBannerRotation()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_BANNER_INDEX, bannerIndex)
        binding.webView.saveState(outState)
    }

    override fun onDestroy() {
        stopBannerRotation()
        binding.webView.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }

    companion object {
        private const val BANNER_ROTATE_INTERVAL_MS = 4_000L
        private const val STATE_BANNER_INDEX = "state_banner_index"
        private const val SANTORINI_BANNER_INDEX = 1
    }
}
