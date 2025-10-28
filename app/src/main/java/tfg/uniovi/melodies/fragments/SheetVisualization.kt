package tfg.uniovi.melodies.fragments

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Typeface
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGExternalFileResolver
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentSheetVisualizationBinding
import tfg.uniovi.melodies.model.HistoryEntry
import tfg.uniovi.melodies.model.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.CHECK
import tfg.uniovi.melodies.fragments.viewmodels.NoteCheckingState
import tfg.uniovi.melodies.fragments.viewmodels.PAGING
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationViewModel
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationViewModelFactory
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.processing.PitchDetector.MIC_REQ_CODE
import tfg.uniovi.melodies.processing.PitchDetector.startListening
import tfg.uniovi.melodies.processing.PitchDetector.stopListening
import tfg.uniovi.melodies.fragments.utils.BottomNavUtils
import tfg.uniovi.melodies.fragments.utils.ShowAlertDialog
import tfg.uniovi.melodies.processing.parser.XMLParserException


//LOG TAGS
private const val SET_UP = "VIEW_MODEL_SET_UP"
private const val PARSING = "PARSING_XML"
private const val SVG_OBSERVER = "SVG_OBSERVER"
private const val CONSOLE = "WebViewConsole"
private const val NAVIGATION = "NAVIGATION"
private const val RENDER = "RENDER"
private const val SVG_RENDER = "SVG_RENDER"
private const val PAINTING = "PAINTING"

//JAVASCRIPT FUNCTIONS FOR VEROVIO TOOLKIT
private const val VEROVIO_HTML = "file:///android_asset/verovio.html"
private const val LOAD_FROM_BASE_64 = "loadMusicXmlFromBase64"
private const val GET_PAGE_COUNT = "getPageCount"
private const val RENDER_PAGE_TO_DOM = "renderPageToDom"


class SheetVisualization : Fragment() {

    private val args : SheetVisualizationArgs by navArgs()
    private lateinit var binding: FragmentSheetVisualizationBinding
    private lateinit var sheetVisualizationViewModel: SheetVisualizationViewModel
    private lateinit var musicXMLSheet: MusicXMLSheet
    private var totalPages = 1
    private var elapsedFormatted: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestMicPermission()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onStop() {
        super.onStop()
        //Pitch detector stop listening and processing audio
        stopListening()
        BottomNavUtils.setBottomNavMenuVisibility(this,View.VISIBLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSheetVisualizationBinding.inflate(inflater, container, false)

        SVG.registerExternalFileResolver(AssetFileResolver())
        sheetVisualizationViewModel = ViewModelProvider(this, SheetVisualizationViewModelFactory(
            PreferenceManager.getUserId(requireContext())!!
        ))[SheetVisualizationViewModel::class.java]

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            BottomNavUtils.setBottomNavMenuVisibility(this, View.GONE)
        }
        setupWebView()
        setupNavigationButtons()
        viewModelSetUp()
        toolBarSetUp()
    }

    private fun viewModelSetUp() {
        sheetVisualizationViewModel.musicXMLSheet.observe(viewLifecycleOwner) { musicxml ->
            if(sheetVisualizationViewModel.hasMusicXMLSheetChanged){
                sheetVisualizationViewModel.hasMusicXMLSheetChanged =false
                musicxml?.let {
                    Log.d(SET_UP, "Setting up musicxml and its observes")
                    binding.toolbar.title = it.name
                    try{
                        sheetVisualizationViewModel.parseMusicXML()
                    }catch (e : XMLParserException){
                        ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(requireContext(),
                            getString(R.string.sheet_visualization_invalid_xml),
                            getString(R.string.sheet_visualization_invalid_xml_msg),
                            PARSING,
                            "alert dialog showed because xml missing attributes and/or elements")
                            sheetVisualizationViewModel.updateNoteCheckingState(NoteCheckingState.NOT_AVAILABLE)
                    }
                    val encondedXML =
                        Base64.encodeToString(it.stringSheet.toByteArray(), Base64.NO_WRAP)
                    binding.webView.evaluateJavascript(
                        "$LOAD_FROM_BASE_64('$encondedXML');"
                    ) { _ ->
                        binding.webView.evaluateJavascript("$GET_PAGE_COUNT();") { pageCount ->
                            totalPages = pageCount.toIntOrNull() ?: 1
                            sheetVisualizationViewModel.setTotalPages(totalPages)
                            sheetVisualizationViewModel.moveForward()
                        }
                    }

                }
            }

            this.musicXMLSheet = musicxml

        }

        sheetVisualizationViewModel.svg.observe(viewLifecycleOwner) { svg ->
            try {
                Log.d(PAGING, "SVG CHANGED")
                binding.sheetImageView.setSVG(SVG.getFromString(svg))
                binding.sheetImageView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                sheetVisualizationViewModel.updateCurrentPage()
            } catch (e: Exception) {
                Log.e(SVG_OBSERVER, "Failed to load SVG", e)
            }
        }


        sheetVisualizationViewModel.currentPage.observe(viewLifecycleOwner) { newCurrentPage ->
            if(newCurrentPage!=0)
                renderCurrentPage()
        }

        sheetVisualizationViewModel.shouldNavigateToNextPage.observe(viewLifecycleOwner) { should ->
            if (should) {
                val currentPage = sheetVisualizationViewModel.currentPage.value?:1
                Log.d(NAVIGATION, "Auto-navigating from page $currentPage to ${currentPage + 1}")
                sheetVisualizationViewModel.moveForward()
            }
        }

        val returnToHome: () -> Unit = {
            stopListening()
            findNavController().navigate(R.id.home_fragment)
        }

        sheetVisualizationViewModel.noteCheckingState.observe(viewLifecycleOwner) { state ->
            Log.d(CHECK, "Changed to state $state")
            if (state == NoteCheckingState.CHECKING) {
                val currentOrientation = resources.configuration.orientation
                requireActivity().requestedOrientation = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
            if (state == NoteCheckingState.FINISHED) {
                sheetVisualizationViewModel.updateNoteCheckingState(NoteCheckingState.NONE)

                val message = getString(R.string.sheet_visualization_finish_msg) +
                        " " + musicXMLSheet.name +
                        "\n\n ${getString(R.string.tot_time)} $elapsedFormatted"

                ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(
                    requireContext(),
                    getString(R.string.sheet_visualization_finish),
                    message,
                    CHECK,
                    "${musicXMLSheet.name} was finished in $elapsedFormatted",
                    returnToHome
                )
            }

        }
        sheetVisualizationViewModel.elapsedTimeMs.observe(viewLifecycleOwner) { elapsed ->
            val totalSeconds = (elapsed / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60

            elapsedFormatted = when {
                minutes > 0 -> "$minutes min $seconds s"
                else -> "$seconds s"
            }
            sheetVisualizationViewModel.saveNewHistoryEntry(HistoryEntry(musicXMLSheet.name, elapsedFormatted))

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
        binding.webView.settings.allowFileAccess = true
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    Log.d(
                        CONSOLE,
                        "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}"
                    )
                }
                return true
            }
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                sheetVisualizationViewModel.loadMusicSheet(args.sheetIdNFolderId)
            }
        }
        val headers = mapOf(
            "Cache-Control" to "no-cache, no-store, must-revalidate"
        )
        binding.webView.loadUrl(VEROVIO_HTML,headers)
    }

    /**
     * Renders current page and shows its SVG in the ImageView
     */
    private fun renderCurrentPage() {
        val currentPage = sheetVisualizationViewModel.currentPage.value
        Log.d(RENDER, "Rendering page: $currentPage")
        binding.webView.evaluateJavascript("$RENDER_PAGE_TO_DOM($currentPage);") { rawSvg ->
                try {
                    val svgClean = cleanSvg(rawSvg)
                    Log.d(PAINTING, "SVG NEEDS TO PAINTED")
                    sheetVisualizationViewModel.updateSVGValue(svgClean)

                } catch (e: Exception) {
                    Log.e(SVG_RENDER, "Failed to render SVG", e)
                }
            }
    }

    /**
     * Returns a cleaned version of the svg of the music sheet with the correct `@font-face'
     * declaration and the correct symbols
     *
     * @param svgRaw [String] that represents the svg as Verovio created it
     */
    private fun cleanSvg(svgRaw: String): String {

        // Eliminating @font-face
        val fontFaceRegex = Regex("<style[\\s\\S]*?@font-face[\\s\\S]*?</style>")

        // Adding out own @font-face with Bravura and having a src with a url of the desired assets
        val correctFontFaceStyle = """
        <style type="text/css">
            @font-face {
                font-family: 'Bravura';
                src: url('file:///android_asset/fonts/Bravura.otf');
            }
        </style>
    """

        // We clean the svg to add out correct declaration of @font-face
        val processedSvg = svgRaw
            .replace(fontFaceRegex, "")
            .replace("<defs>", "<defs>$correctFontFaceStyle")

        // We clean the rest of the svg
        return processedSvg
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
    inner class AssetFileResolver : SVGExternalFileResolver() {
        /**
         * Resolves fonts requested by **android-svg** when encountering a `@font-face` with `url()`.
         *
         * This implementation forces the use of the **Bravura** font by loading it from the app's assets.
         * For any other font, it returns `null`, allowing **android-svg** to fall back to its default logic
         * (e.g., looking for system fonts).
         *
         * @param fontFamily the requested font family name, or `null` if none is specified.
         * @param fontWeight the requested font weight (e.g., `400` for normal, `700` for bold).
         * @param fontStyle the requested font style (e.g., `"normal"`, `"italic"`), or `null` if none is specified.
         *
         * @return a [Typeface] instance for Bravura if matched, or `null` to delegate resolution to android-svg.
         */
        override fun resolveFont(fontFamily: String?, fontWeight: Int, fontStyle: String?): Typeface? {

            // As we are trying to only use Bravura, it loads it from the assets
            if (fontFamily != null && fontFamily.equals("Bravura", ignoreCase = true)) {
                return Typeface.createFromAsset(requireContext().assets, "fonts/Bravura.otf")
            }

            // Any other fonts, we return null.
            // This allows android-svg to go with its default logic, maybe looking for system fonts
            return null
        }
        override fun resolveImage(href: String?): Bitmap? {
            return null
        }
    }
}

