package tfg.uniovi.melodies.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.caverock.androidsvg.SVG
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentSheetVisualizationBinding
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.MIC_REQ_CODE
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.stopListening

private const val VEROVIO_HTML = "file:///android_asset/verovio.html"

class SheetVisualization : Fragment() {

    private val args : SheetVisualizationArgs by navArgs()
    private lateinit var binding: FragmentSheetVisualizationBinding
    private var currentPage = 1
    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestMicPermission()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSheetVisualizationBinding.inflate(inflater, container, false)
        /*
        val checker = SheetChecker()
        val noteToPlay = checker.getNotesToPlay()
        binding.noteToPlay.text = noteToPlay
        Log.d("SHEET_vISUALIZATION", "The note to play is: $noteToPlay")
        lifecycle.coroutineScope.launch(Dispatchers.Default) {
            var num = 3
            while(num>0){
                val isGood = checker.isNotePlayedCorrectly()
                if(isGood){
                    withContext(Dispatchers.Main) {
                        binding.result.text = num.toString()
                        Log.d("SHEET_VISUAL", "The note has been played!!!")
                    }
                    num--
                }
            }
        }*/
        //TODO REMOVE
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d("JS_CONSOLE", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                return true
            }
        }
        setBottomNavMenuVisibility(View.GONE)
        setupWebView()
        setupNavigationButtons()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        toolbar.title = args.sheetId

    }
    override fun onStop() {
        super.onStop()
        //Pitch detector stop listening and processing audio
        stopListening()
        setBottomNavMenuVisibility(View.VISIBLE)

    }

    /**
     * Changes the visibility of the Bottom Navigation
     *
     * @param visibility the value for the Bottom Navigation Menu Visibility (View.VISIBLE/View.GONE)
     */
    private fun setBottomNavMenuVisibility(visibility: Int){
        if(visibility == View.GONE || visibility == View.VISIBLE){
            val navView =
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView.visibility = visibility
        }
    }


    /**
     * Configures the WebView with Verovio JS
     */
    private fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                loadMusicXMLAndRender()
            }
        }
        binding.webView.loadUrl(VEROVIO_HTML)

    }

    /**
     * Loads MUSICXML and gives it to VEROVIO
     */
    private fun loadMusicXMLAndRender() {
        lifecycleScope.launch(Dispatchers.IO) {
            val inputStream = requireContext().assets.open("cuna.xml")
            val musicXml = inputStream.bufferedReader().use { it.readText() }
            val encodedXml = Base64.encodeToString(musicXml.toByteArray(), Base64.NO_WRAP)

            withContext(Dispatchers.Main) {
                binding.webView.evaluateJavascript(
                    "loadMusicXmlFromBase64('$encodedXml');"
                ) { _ ->
                    binding.webView.evaluateJavascript("getPageCount();") { pageCount ->
                        totalPages = pageCount.toIntOrNull() ?: 1
                        renderCurrentPage()
                    }
                }
            }
        }
    }

    /**
     * Renders current page and shows its SVG in the ImageView
     */
    private fun renderCurrentPage(){
        binding.webView.evaluateJavascript("renderPageToDom($currentPage);"){rawSvg ->
            val svgClean = cleanSvg(rawSvg)
            binding.sheetImageView.setSVG(SVG.getFromString(svgClean))
        }
    }

    private fun cleanSvg(svgRaw: String): String {
        return svgRaw
            .removeSurrounding("\"")
            .replace("\\n", "")
            .replace("\\\"", "\"")
            .replace("\\/", "/")
            .replace("\\u003C", "<")
            .replace("\\u003E", ">")
            .replace("\\u0026", "&")
    }

    /**
     * Configures buttons to next and previous pages
     */
    private fun setupNavigationButtons() {
        binding.prevButton.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                renderCurrentPage()
            }
        }
        binding.nextButton.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                renderCurrentPage()
            }
        }
    }

    /**
        Requests mic permissions to start the pitch detection
     */
    private fun requestMicPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            //Pitch detector start listening and processing audio
            //TODO startListening(lifecycle.coroutineScope)
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_REQ_CODE
            )
        }
    }
}