package tfg.uniovi.melodies.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.caverock.androidsvg.SVG
import com.google.android.material.bottomnavigation.BottomNavigationView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentSheetVisualizationBinding
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.NoteCheckingState
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationViewModel
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationViewModelFactory
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.MIC_REQ_CODE
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.startListening
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.stopListening
import tfg.uniovi.melodies.utils.ShowAlertDialog
import tfg.uniovi.melodies.utils.parser.SVGParserException

private const val VEROVIO_HTML = "file:///android_asset/verovio.html"

class SheetVisualization : Fragment() {

    private val args : SheetVisualizationArgs by navArgs()
    private lateinit var binding: FragmentSheetVisualizationBinding
    private lateinit var sheetVisualizationViewModel: SheetVisualizationViewModel
    private lateinit var musicXMLSheet: MusicXMLSheet
    //private var currentPage = 1
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

    override fun onStop() {
        super.onStop()
        //Pitch detector stop listening and processing audio
        stopListening()
        setBottomNavMenuVisibility(View.VISIBLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSheetVisualizationBinding.inflate(inflater, container, false)

        sheetVisualizationViewModel = ViewModelProvider(this, SheetVisualizationViewModelFactory(
            PreferenceManager.getUserId(requireContext())!!
        ))[SheetVisualizationViewModel::class.java]

        setupWebView()
        setupNavigationButtons()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            setBottomNavMenuVisibility(View.GONE)
        }
        viewModelSetUp()
        toolBarSetUp()
    }

    private fun viewModelSetUp() {
        sheetVisualizationViewModel.musicXMLSheet.observe(viewLifecycleOwner) { musicxml ->
            this.musicXMLSheet = musicxml
            musicxml?.let {
                binding.toolbar.title = it.name
                try{
                    sheetVisualizationViewModel.parseMusicXML()
                }catch (e : SVGParserException){
                    ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(requireContext(),
                        "Invalid MusicXML",
                        "MusicXML is missing attributes and/or elements, " +
                                "no feedback will be given tap each end of the screen to navigate",
                        "PARSING_XML",
                        "alert dialog showed because xml missing attributes and/or elements")
                }
                val encondedXML =
                    Base64.encodeToString(it.stringSheet.toByteArray(), Base64.NO_WRAP)
                binding.webView.evaluateJavascript(
                    "loadMusicXmlFromBase64('$encondedXML');"
                ) { _ ->
                    binding.webView.evaluateJavascript("getPageCount();") { pageCount ->
                        totalPages = pageCount.toIntOrNull() ?: 1
                        sheetVisualizationViewModel.setTotalPages(totalPages)
                        sheetVisualizationViewModel.moveForward()
                    }
                }

            }
        }

        sheetVisualizationViewModel.svg.observe(viewLifecycleOwner) { svg ->
            try {
                Log.d("PAGING", "SVG CHANGED")
                binding.sheetImageView.setSVG(SVG.getFromString(svg))
                binding.sheetImageView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                sheetVisualizationViewModel.updateCurrentPage()
            } catch (e: Exception) {
                Log.e("SVG_OBSERVER", "Failed to load SVG", e)
            }
        }
/*
        sheetVisualizationViewModel.parsingFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                binding.sheetImageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver
                .OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // Remove listener to avoid multiple calls
                        binding.sheetImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        sheetVisualizationViewModel.startNoteChecking()
                    }
                })
            }
        }*/


        sheetVisualizationViewModel.currentPage.observe(viewLifecycleOwner) { newCurrentPage ->
            if(newCurrentPage!=0)
                renderCurrentPage()
        }

        sheetVisualizationViewModel.shouldNavigateToNextPage.observe(viewLifecycleOwner) { should ->
            if (should) {
                val currentPage = sheetVisualizationViewModel.currentPage.value?:1
                Log.d("NAVIGATION", "Auto-navigating from page $currentPage to ${currentPage + 1}")
                //sheetVisualizationViewModel.updateCurrentPage(currentPage++)
                sheetVisualizationViewModel.moveForward()
            }
        }

        val returnToHome = { findNavController().navigate(R.id.home_fragment) }

        sheetVisualizationViewModel.noteCheckingState.observe(viewLifecycleOwner) { state ->
            Log.d("CHECKER", "Changed to state $state")
            if (state == NoteCheckingState.FINISHED) {
                ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(
                    requireContext(),
                    "Finished!",
                    "Congratulations you finished practicing ${musicXMLSheet.name}",
                    "CHECKER",
                    "${musicXMLSheet.name} was finished",
                    returnToHome
                )
            }
        }
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
     * Sets up the toolbar and its back button
     */
    private fun toolBarSetUp() {
        val toolbar = binding.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Configures the WebView with Verovio JS
     */
    private fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(true) //TODO remove
        binding.webView.webChromeClient = object : WebChromeClient() { //TODO remove
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d("JS_CONSOLE", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                return true
            }
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                sheetVisualizationViewModel.loadMusicSheet(args.sheetIdNFolderId)
            }
        }

        binding.webView.loadUrl(VEROVIO_HTML)
    }

    /**
     * Renders current page and shows its SVG in the ImageView
     */
    private fun renderCurrentPage() {
        val currentPage = sheetVisualizationViewModel.currentPage.value
        Log.d("RENDER", "Rendering page: $currentPage")
        binding.webView.evaluateJavascript("renderPageToDom($currentPage);") { rawSvg ->
                try {
                    val svgClean = cleanSvg(rawSvg)
                    Log.d("PAINTING", "SVG NEEDS TO PAINTED")
                    // Solo actualizar el SVG en el ViewModel si realmente cambió
                    if (sheetVisualizationViewModel.svg.value != svgClean) {
                        sheetVisualizationViewModel.updateSVGValue(svgClean)
                    }
                } catch (e: Exception) {
                    Log.e("SVG_RENDER", "Failed to render SVG", e)
                }
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
            sheetVisualizationViewModel.moveBack()
        }

        binding.nextButton.setOnClickListener {
            sheetVisualizationViewModel.moveForward()
        }
    }

    /**
     * Requests mic permissions to start the pitch detection
     */
    private fun requestMicPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListening(lifecycle.coroutineScope)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_REQ_CODE
            )
        }
    }
}