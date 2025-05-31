package tfg.uniovi.melodies.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.caverock.androidsvg.SVG
import com.google.android.material.bottomnavigation.BottomNavigationView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentSheetVisualizationBinding
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationViewModel
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationViewModelFactory
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.MIC_REQ_CODE
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.stopListening
import tfg.uniovi.melodies.utils.parser.SVGParserException
import java.util.UUID

private const val VEROVIO_HTML = "file:///android_asset/verovio.html"

class SheetVisualization : Fragment() {

    private val args : SheetVisualizationArgs by navArgs()
    private lateinit var binding: FragmentSheetVisualizationBinding
    private lateinit var sheetVisualizationViewModel: SheetVisualizationViewModel
    private lateinit var musicXMLSheet: MusicXMLSheet
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
            UUID.fromString("a5ba172c-39d8-4181-9b79-76b8f23b5d18")
        )).get(SheetVisualizationViewModel::class.java)


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
                val encondedXML =
                    Base64.encodeToString(it.stringSheet.toByteArray(), Base64.NO_WRAP)
                binding.webView.evaluateJavascript(
                    "loadMusicXmlFromBase64('$encondedXML');"
                ) { _ ->
                    binding.webView.evaluateJavascript("getPageCount();") { pageCount ->
                        totalPages = pageCount.toIntOrNull() ?: 1
                        renderCurrentPage()
                    }
                }
                binding.toolbar.title = it.name
                try{
                    sheetVisualizationViewModel.parseMusicXML()
                }catch (e : SVGParserException){
                    showAlertDialog()

                }

            }
        }
        sheetVisualizationViewModel.svg.observe(viewLifecycleOwner){ svg ->
            renderCurrentPage() //TODO is this oke

        }

        sheetVisualizationViewModel.correctlyPlayedNotesCounter.observe(viewLifecycleOwner){ newCorrect ->
            //highlightNoteByIndex(,newCorrect,"green") svg as attribute?

        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(requireContext()).setTitle("Invalid MusicXML")
            .setMessage(
                "MusicXML is missing attributes and/or elements, " +
                        "no feedback will be given tap each end of the screen to navigate"
            )
            .setIcon(R.drawable.icon_alert)
            .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                Log.d("PARSE", "alert dialog showed")
            }.show()
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
        //TODO REMOVE
        binding.webView.webChromeClient = object : WebChromeClient() {
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
     * Loads MUSICXML and gives it to VEROVIO
     */

    private fun highlightNoteByIndex(svg: String, index: Int, color: String): String {
        // Find the notes
        val regex = Regex("""<g[^>]*class="[^"]*note[^"]*"[^>]*>.*?</g>""", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(svg).toList()

        if (index >= matches.size) {
            Log.d("SheetVisualizer","Índice fuera de rango: ${matches.size} notas encontradas")
            return svg
        }

        val targetGroup = matches[index].value

        // Añade o reemplaza fill="color" en los elementos internos
        val modifiedGroup = targetGroup.replace(
            Regex("""(fill="[^"]*")"""),
            """fill="$color""""
        ).ifEmpty {
            // Si no tiene atributo fill, lo añadimos a cada elemento gráfico
            targetGroup.replace("<path", """<path fill="$color"""")
        }

        return svg.replace(targetGroup, modifiedGroup)
    }

    /**
     * Renders current page and shows its SVG in the ImageView
     */
    private fun renderCurrentPage() {
        binding.webView.evaluateJavascript("renderPageToDom($currentPage);") { rawSvg ->
            val svgClean = cleanSvg(rawSvg)
            binding.sheetImageView.setSVG(SVG.getFromString(svgClean)) // TODO DA NULL AL GIRAR
        }
    }
    private fun countNotesInSvg(svg: String): Int {
        val regex = Regex("""<g[^>]*class="[^"]*note[^"]*"[^>]*>.*?</g>""")
        return regex.findAll(svg).count()
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