package com.kat.mybudgetbuddy.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScannerView(
    onDismiss: () -> Unit,
    scannedAmount: String,
    onAmountChange: (String) -> Unit,
    scannedDueDate: String,
    onDueDateChange: (String) -> Unit
) {
    val context = LocalContext.current

    // Create internal MutableState objects
    val internalAmount = remember { mutableStateOf(scannedAmount) }
    val internalDueDate = remember { mutableStateOf(scannedDueDate) }

    val showFeedback = remember { mutableStateOf(false) }
    val feedbackMessage = remember { mutableStateOf("") }

    LaunchedEffect(internalAmount.value) {
        if (internalAmount.value != scannedAmount) {
            onAmountChange(internalAmount.value)
        }
    }

    LaunchedEffect(internalDueDate.value) {
        if (internalDueDate.value != scannedDueDate) {
            onDueDateChange(internalDueDate.value)
        }
    }

    val hasRequiredPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRequiredPermission.value = isGranted
    }

    LaunchedEffect(key1 = true) {
        if (!hasRequiredPermission.value) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Invoice") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Check, contentDescription = "Done")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasRequiredPermission.value) {
                CameraPreview(
                    scannedAmount = internalAmount,
                    scannedDueDate = internalDueDate,
                    showFeedback = showFeedback,
                    feedbackMessage = feedbackMessage
                )

                // Guidance overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    if (internalAmount.value.isEmpty()) {
                        Text(
                            text = "Tap the amount",
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }

                    if (internalDueDate.value.isEmpty()) {
                        Text(
                            text = "Tap the due date",
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                }

                // Feedback message
                AnimatedVisibility(
                    visible = showFeedback.value,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(
                        text = feedbackMessage.value,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50), // Start color - addIncomeStart
                                        Color(0xFF43A047), // Middle color - addIncomeMiddle
                                        Color(0xFF388E3C)  // End color - addIncomeEnd
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    )
                }
            } else {
                Text(
                    text = "Camera permission required",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraPreview(
    scannedAmount: MutableState<String>,
    scannedDueDate: MutableState<String>,
    showFeedback: MutableState<Boolean>,
    feedbackMessage: MutableState<String>
) {
    val context = LocalContext.current
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val processedTexts = remember { mutableSetOf<String>() }
    val processingTouch = remember { AtomicBoolean(false) }

    val detectedTextBlocks = remember {
        SnapshotStateList<String>().apply {
            // Pre-allocate some capacity
            addAll(List(10) { "" })
            clear()
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                try {
                    cameraProviderFuture.get().unbindAll()
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Error unbinding camera", e)
                }
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error unbinding camera on dispose", e)
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) { view ->
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(executor, TextAnalyzer(textRecognizer) { text ->
                        // Add new detected text while keeping the list manageable
                        CoroutineScope(Dispatchers.Main).launch {
                            if (detectedTextBlocks.size > 20) { // Keep list size reasonable
                                detectedTextBlocks.removeAt(0)
                            }
                            detectedTextBlocks.add(text)
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind any existing use cases before rebinding
                cameraProvider.unbindAll()

                // Bind camera provider
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                // Set up tap listener
                view.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN && !processingTouch.get()) {
                        processingTouch.set(true)
                        val touchX = event.x
                        val touchY = event.y

                        Log.d("CameraPreview", "Touch at X: $touchX, Y: $touchY")

                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                // Create a safe copy of the list to work with
                                val textBlocksCopy = detectedTextBlocks.toList()

                                // Process all detected text blocks
                                for (text in textBlocksCopy) {
                                    if (text.isEmpty()) continue

                                    val result = processFullText(text, scannedAmount, scannedDueDate)
                                    if (result != null) {
                                        showFeedback(result, showFeedback, feedbackMessage)
                                        processingTouch.set(false)
                                        return@launch
                                    }
                                }

                                // If no full match, try individual components
                                val messages = mutableListOf<String>()
                                textBlocksCopy.forEach { textBlock ->
                                    if (textBlock.isEmpty()) return@forEach

                                    val textComponents = textBlock.split(Regex("\\s+"))
                                    for (component in textComponents) {
                                        if (component.isEmpty()) continue

                                        val message = processScannedText(component, scannedAmount, scannedDueDate)
                                        if (message != null) {
                                            messages.add(message)
                                        }
                                    }
                                }

                                if (messages.isNotEmpty()) {
                                    showFeedback(messages.joinToString("\n"), showFeedback, feedbackMessage)
                                }
                            } catch (e: Exception) {
                                Log.e("CameraPreview", "Error processing touch", e)
                            } finally {
                                processingTouch.set(false)
                            }
                        }
                    }
                    true
                }
            } catch (e: Exception) {
                Log.e("CameraPreview", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

class TextAnalyzer(
    private val textRecognizer: TextRecognizer,
    private val onTextFound: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Process every Nth frame to avoid overloading
    private var frameCounter = 0
    private val processEveryNthFrame = 5

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        try {
            // Only process every Nth frame
            frameCounter = (frameCounter + 1) % processEveryNthFrame
            if (frameCounter != 0) {
                imageProxy.close()
                return
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        try {
                            // Limit the amount of text to process
                            val maxBlocks = minOf(5, visionText.textBlocks.size)
                            for (i in 0 until maxBlocks) {
                                val block = visionText.textBlocks[i]
                                if (block.text.isNotEmpty()) {
                                    onTextFound(block.text)
                                }

                                // Process at most 3 lines per block
                                val maxLines = minOf(3, block.lines.size)
                                for (j in 0 until maxLines) {
                                    val line = block.lines[j]
                                    if (line.text.isNotEmpty()) {
                                        onTextFound(line.text)
                                    }

                                    // Only process elements with numbers (potential amounts or dates)
                                    line.elements.forEach { element ->
                                        if (element.text.matches(Regex(".*\\d+.*"))) {
                                            onTextFound(element.text)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TextAnalyzer", "Error processing text blocks", e)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TextAnalyzer", "Text recognition failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        } catch (e: Exception) {
            Log.e("TextAnalyzer", "Error in analyze", e)
            imageProxy.close()
        }
    }
}

suspend fun processTextFromTap(
    text: String,
    scannedAmount: MutableState<String>,
    scannedDueDate: MutableState<String>,
    showFeedback: MutableState<Boolean>,
    feedbackMessage: MutableState<String>,
    processedTexts: MutableSet<String>
) {
    val cleanText = text.trim()
    Log.d("InvoiceScanner", "Tapped text: $cleanText")

    // Try to process the entire text first
    val message = processFullText(cleanText, scannedAmount, scannedDueDate)

    if (message != null) {
        showFeedback(message, showFeedback, feedbackMessage)
        return
    }

    // If full text processing didn't work, try component-based processing
    val messages = mutableListOf<String>()
    val textComponents = cleanText.split("\\s+".toRegex())

    for (component in textComponents) {
        val componentMessage = processScannedText(component, scannedAmount, scannedDueDate)
        if (componentMessage != null) {
            messages.add(componentMessage)
        }
    }

    if (messages.isNotEmpty()) {
        showFeedback(messages.joinToString("\n"), showFeedback, feedbackMessage)
    }
}

fun processFullText(
    text: String,
    scannedAmount: MutableState<String>,
    scannedDueDate: MutableState<String>
): String? {
    try {
        val cleanText = text.trim()
        Log.d("InvoiceScanner", "Processing text: $cleanText")

        if (cleanText.isEmpty()) {
            return null
        }
        // Check for amounts with spaces like "10 000,00"
        try {
            val amountPattern = "(\\d{1,3}(?: \\d{3})+)[,.](\\d{2})"
            val amountMatcher = Pattern.compile(amountPattern).matcher(cleanText)

            if (amountMatcher.find()) {
                val match = amountMatcher.group() ?: return null
                Log.d("InvoiceScanner", "Amount match found: $match")

                // Remove spaces for storage but keep format consistent
                val numericText = match.replace(" ", "")
                scannedAmount.value = numericText
                return "Amount captured: $match"
            }
        } catch (e: Exception) {
            Log.e("InvoiceScanner", "Error matching amount pattern with spaces", e)
        }

        // Check for simple amount pattern (no spaces)
        try {
            val simpleAmountPattern = "(\\d+)[,.](\\d{2})"
            val simpleAmountMatcher = Pattern.compile(simpleAmountPattern).matcher(cleanText)

            if (simpleAmountMatcher.find()) {
                val match = simpleAmountMatcher.group() ?: return null
                Log.d("InvoiceScanner", "Simple amount match found: $match")

                scannedAmount.value = match
                return "Amount captured: $match"
            }
        } catch (e: Exception) {
            Log.e("InvoiceScanner", "Error matching simple amount pattern", e)
        }

        // Check for different date formats
        val datePatterns = arrayOf(
            "\\b(20\\d{2})[-/.](0[1-9]|1[0-2])[-/.](0[1-9]|[12]\\d|3[01])\\b",
            "\\b(0[1-9]|[12]\\d|3[01])[-/.](0[1-9]|1[0-2])[-/.](20\\d{2})\\b"
        )

        for (pattern in datePatterns) {
            try {
                val dateMatcher = Pattern.compile(pattern).matcher(cleanText)
                if (dateMatcher.find()) {
                    val dateMatch = dateMatcher.group() ?: continue
                    Log.d("InvoiceScanner", "Date match found: $dateMatch")
                    val formattedDate = formatDate(dateMatch)
                    if (formattedDate != null) {
                        scannedDueDate.value = formattedDate
                        return "Due date captured: $formattedDate"
                    }
                }
            } catch (e: Exception) {
                Log.e("InvoiceScanner", "Error matching date pattern: $pattern", e)
            }
        }

        return null
    } catch (e: Exception) {
        Log.e("InvoiceScanner", "Error in processFullText", e)
        return null
    }
}

fun processScannedText(
    text: String,
    scannedAmount: MutableState<String>,
    scannedDueDate: MutableState<String>
): String? {
    val cleanText = text.trim()
    Log.d("InvoiceScanner", "Processing component: $cleanText")

    // Enhanced date pattern checking
    val datePattern = "(\\d{4}[-/.]\\d{2}[-/.]\\d{2}|\\d{2}[-/.]\\d{2}[-/.]\\d{4})"
    val dateMatcher = Pattern.compile(datePattern).matcher(cleanText)

    if (dateMatcher.matches()) {
        val formattedDate = formatDate(cleanText)
        if (formattedDate != null) {
            scannedDueDate.value = formattedDate
            return "Due date captured: $formattedDate"
        }
    }

    // Amount pattern check - try multiple formats
    if (cleanText.matches("^\\d+[,.]\\d{2}$".toRegex())) {
        val numericText = cleanText.replace("[^0-9,.]".toRegex(), "")
        scannedAmount.value = numericText
        return "Amount captured: $numericText"
    }

    // Try to extract amount from text with other content
    val amountPattern = "(\\d+)[,.](\\d{2})"
    val amountMatcher = Pattern.compile(amountPattern).matcher(cleanText)

    if (amountMatcher.find()) {
        val match = amountMatcher.group()
        scannedAmount.value = match
        return "Amount captured: $match"
    }

    return null
}

suspend fun showFeedback(
    message: String,
    showFeedback: MutableState<Boolean>,
    feedbackMessage: MutableState<String>
) {
    try {
        // Update feedback message and show it
        feedbackMessage.value = message
        showFeedback.value = true

        // Hide feedback after 3 seconds
        delay(3000)
        showFeedback.value = false
    } catch (e: Exception) {
        Log.e("InvoiceScanner", "Error showing feedback", e)
    }
}

fun formatDate(dateString: String): String? {
    if (dateString.isEmpty()) return null

    try {
        val formats = arrayOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("dd-MM-yyyy", Locale.US),
            SimpleDateFormat("yyyy/MM/dd", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("yyyy.MM.dd", Locale.US),
            SimpleDateFormat("dd.MM.yyyy", Locale.US)
        )

        for (format in formats) {
            try {
                format.isLenient = false // Add this line to enforce strict parsing
                val date = format.parse(dateString)
                if (date != null) {
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val formatted = outputFormat.format(date)
                    Log.d("InvoiceScanner", "Date formatted: $formatted")
                    return formatted
                }
            } catch (e: Exception) {
                // Try next format
                Log.v("InvoiceScanner", "Date format didn't match: ${format.toPattern()}")
            }
        }

        // If we reach here, no format matched successfully
        Log.w("InvoiceScanner", "Could not parse date string: $dateString")
        return null
    } catch (e: Exception) {
        Log.e("InvoiceScanner", "Error formatting date: $dateString", e)
        return null
    }
}