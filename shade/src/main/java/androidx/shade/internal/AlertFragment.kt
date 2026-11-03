package androidx.shade.internal

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.base.activity.StandardActivityConfig
import com.bonepeople.android.widget.view.InsetsLayout
import com.gyf.immersionbar.ktx.immersionBar

internal class AlertFragment : Fragment() {
    private var webView: WebView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val alertWebView = WebView(requireContext())
        webView = alertWebView
        return InsetsLayout(requireContext()).apply {
            setBackgroundColor(Color.BLACK)
            insetsMask = InsetsLayout.INSET_ALL
            typeMask = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()
            addView(
                alertWebView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val alertWebView = checkNotNull(webView)
        alertWebView.settings.javaScriptEnabled = true
        alertWebView.webViewClient = WebViewClient()
        alertWebView.webChromeClient = WebChromeClient()

        val restored = savedInstanceState?.let { alertWebView.restoreState(it) != null } == true
        if (!restored) {
            alertWebView.loadUrl(requireArguments().getString(ARGUMENT_URL).orEmpty())
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentWebView = webView
                    if (currentWebView?.canGoBack() == true) {
                        currentWebView.goBack()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView?.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        webView?.apply {
            stopLoading()
            webChromeClient = null
            (parent as? ViewGroup)?.removeView(this)
            removeAllViews()
            destroy()
        }
        webView = null
        super.onDestroyView()
    }

    companion object {
        private const val ARGUMENT_URL = "AlertFragment.url"
        private val ACTIVITY_CONFIG = StandardActivityConfig(
            statusBar = {
                immersionBar {
                    transparentBar()
                    statusBarDarkFont(false)
                    navigationBarDarkIcon(false)
                }
            },
        )

        internal fun open(url: String) {
            if (url.isBlank()) return
            StandardActivity.open(
                AlertFragment().apply {
                    arguments = Bundle().apply { putString(ARGUMENT_URL, url) }
                },
                ACTIVITY_CONFIG,
            )
        }
    }
}