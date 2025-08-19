package com.app.pakeplus

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.webkit.JavascriptInterface
import android.widget.Toast
import java.io.File
import androidx.activity.enableEdgeToEdge
// import android.view.Menu
// import android.view.WindowInsets
// import com.google.android.material.snackbar.Snackbar
// import com.google.android.material.navigation.NavigationView
// import androidx.navigation.findNavController
// import androidx.navigation.ui.AppBarConfiguration
// import androidx.navigation.ui.navigateUp
// import androidx.navigation.ui.setupActionBarWithNavController
// import androidx.navigation.ui.setupWithNavController
// import androidx.drawerlayout.widget.DrawerLayout
// import com.app.pakeplus.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration
//    private lateinit var binding: ActivityMainBinding

    private lateinit var webView: WebView
    private lateinit var gestureDetector: GestureDetectorCompat
    
    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 100
        private const val REQUEST_MANAGE_EXTERNAL_STORAGE = 101
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.single_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ConstraintLayout)) { view, insets ->
            val systemBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                0, systemBar.top, 0, 0
            )
            insets
        }

        webView = findViewById<WebView>(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true                 // 启用JS
            domStorageEnabled = true                 // 启用DOM存储（Vue 需要）
            allowFileAccess = true                   // 允许文件访问
            allowContentAccess = true                // 允许内容访问
            allowUniversalAccessFromFileURLs = true  // 允许从文件URL访问所有资源
            allowFileAccessFromFileURLs = true       // 允许文件URL之间的访问
            setSupportMultipleWindows(true)
            databaseEnabled = true                   // 启用数据库
            cacheMode = WebView.LOAD_DEFAULT        // 设置缓存模式
        }

        // webView.settings.userAgentString = ""

        webView.settings.loadWithOverviewMode = true
        webView.settings.setSupportZoom(false)

        // clear cache
        webView.clearCache(true)

        // inject js
        webView.webViewClient = MyWebViewClient()

        // get web load progress
        webView.webChromeClient = MyChromeClient()
        
        // 添加JavaScript接口
        webView.addJavascriptInterface(FileAccessInterface(), "AndroidFileAccess")

        // Setup gesture detector
        gestureDetector =
            GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null) return false

                    val diffX = e2.x - e1.x
                    val diffY = e2.y - e1.y

                    // Only handle horizontal swipes
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                            if (diffX > 0) {
                                // Swipe right - go back
                                if (webView.canGoBack()) {
                                    webView.goBack()
                                    return true
                                }
                            } else {
                                // Swipe left - go forward
                                if (webView.canGoForward()) {
                                    webView.goForward()
                                    return true
                                }
                            }
                        }
                    }
                    return false
                }
            })

        // Set touch listener for WebView
        webView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        // 请求文件访问权限
        requestStoragePermissions()
        
        // webView.loadUrl("https://juejin.cn/")
        webView.loadUrl("file:///android_asset/index.html")

//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(R.layout.single_main)

//        setSupportActionBar(binding.appBarMain.toolbar)

//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }

//        val drawerLayout: DrawerLayout = binding.drawerLayout
//        val navView: NavigationView = binding.navView
//        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
//            ), drawerLayout
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }

    inner class MyWebViewClient : WebViewClient() {

        // vConsole debug
        private var debug = true

        @Deprecated("Deprecated in Java", ReplaceWith("false"))
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return false
        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            println("webView onReceivedError: ${error?.description}")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (debug) {
                // vConsole
                val vConsole = assets.open("vConsole.js").bufferedReader().use { it.readText() }
                val openDebug = """var vConsole = new window.VConsole()"""
                view?.evaluateJavascript(vConsole + openDebug, null)
            }
            // inject js
            val injectJs = assets.open("custom.js").bufferedReader().use { it.readText() }
            view?.evaluateJavascript(injectJs, null)
        }
    }

    inner class MyChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            val url = view?.url
            println("wev view url:$url")
        }
    }
    
    // 请求存储权限
    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 及以上版本
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE)
            }
        } else {
            // Android 10 及以下版本
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            
            val permissionsToRequest = permissions.filter { permission ->
                ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
            }
            
            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toTypedArray(),
                    REQUEST_STORAGE_PERMISSION
                )
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                if (allGranted) {
                    println("存储权限已授予")
                } else {
                    println("存储权限被拒绝")
                }
            }
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_MANAGE_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        println("已获得管理所有文件的权限")
                    } else {
                        println("未获得管理所有文件的权限")
                    }
                }
            }
        }
    }
    
    // JavaScript接口类，用于文件访问
    inner class FileAccessInterface {
        @JavascriptInterface
        fun getExternalStoragePath(): String {
            return Environment.getExternalStorageDirectory().absolutePath
        }
        
        @JavascriptInterface
        fun getAppFilesPath(): String {
            return filesDir.absolutePath
        }
        
        @JavascriptInterface
        fun getAppExternalFilesPath(): String {
            return getExternalFilesDir(null)?.absolutePath ?: ""
        }
        
        @JavascriptInterface
        fun fileExists(path: String): Boolean {
            return try {
                File(path).exists()
            } catch (e: Exception) {
                false
            }
        }
        
        @JavascriptInterface
        fun readFile(path: String): String {
            return try {
                File(path).readText()
            } catch (e: Exception) {
                "Error reading file: ${e.message}"
            }
        }
        
        @JavascriptInterface
        fun listFiles(path: String): String {
            return try {
                val dir = File(path)
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.joinToString(",") { it.name } ?: "Directory is empty"
                } else {
                    "Path does not exist or is not a directory"
                }
            } catch (e: Exception) {
                "Error listing files: ${e.message}"
            }
        }
        
        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}