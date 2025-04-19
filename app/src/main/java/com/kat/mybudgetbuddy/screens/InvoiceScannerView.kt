package com.kat.mybudgetbuddy.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
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
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val processedTexts = remember { mutableSetOf<String>() }

    // Store detected text for tap processing
    val detectedTextBlocks = remember { mutableStateListOf<String>() }

    DisposableEffect(key1 = lifecycleOwner) {
        onDispose {
            executor.shutdown()
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
                        // Store detected text for tap processing
                        detectedTextBlocks.clear()
                        detectedTextBlocks.add(text)
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

                // Set up tap listener to process real detected text
                view.setOnTouchListener { _, event ->
                    val tappedText = if (detectedTextBlocks.isNotEmpty()) {
                        detectedTextBlocks.joinToString(" ")
                    } else {
                        // Fallback if no text is detected
                        ""
                    }

                    // Only process if we have text
                    if (tappedText.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            processTextFromTap(
                                text = tappedText,
                                scannedAmount = scannedAmount,
                                scannedDueDate = scannedDueDate,
                                showFeedback = showFeedback,
                                feedbackMessage = feedbackMessage,
                                processedTexts = processedTexts
                            )
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

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Combine all detected text and pass it to the callback
                    val fullText = visionText.textBlocks.joinToString(" ") { block ->
                        block.text
                    }
                    if (fullText.isNotEmpty()) {
                        onTextFound(fullText)
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
    // Check for amounts with spaces like "10 000,00"
    val amountPattern = "(\\d{1,3}(?: \\d{3})+)[,.](\\d{2})"
    val amountMatcher = Pattern.compile(amountPattern).matcher(text)

    if (amountMatcher.find()) {
        val match = amountMatcher.group()
        Log.d("InvoiceScanner", "Amount match found: $match")

        // Remove spaces for storage but keep the format consistent
        val numericText = match.replace(" ", "")
        scannedAmount.value = numericText
        return "Amount captured: $match"
    }

    // Check for dates
    val datePattern = "(\\d{4}[-/.]\\d{2}[-/.]\\d{2}|\\d{2}[-/.]\\d{2}[-/.]\\d{4})"
    val dateMatcher = Pattern.compile(datePattern).matcher(text)

    if (dateMatcher.find()) {
        val dateMatch = dateMatcher.group()
        val formattedDate = formatDate(dateMatch)
        if (formattedDate != null) {
            scannedDueDate.value = formattedDate
            return "Due date captured: $formattedDate"
        }
    }

    return null
}

fun processScannedText(
    text: String,
    scannedAmount: MutableState<String>,
    scannedDueDate: MutableState<String>
): String? {
    val cleanText = text.trim()

    var message: String? = null

    // Enhanced date pattern checking
    val datePattern = "(\\d{4}[-/.]\\d{2}[-/.]\\d{2}|\\d{2}[-/.]\\d{2}[-/.]\\d{4})"
    val dateMatcher = Pattern.compile(datePattern).matcher(cleanText)

    if (dateMatcher.matches()) {
        val formattedDate = formatDate(cleanText)
        if (formattedDate != null) {
            scannedDueDate.value = formattedDate
            message = "Due date captured: $formattedDate"
        }
    }

    // Amount pattern check
    val amountPattern = "^\\d+[,.]\\d{2}$"
    if (Pattern.matches(amountPattern, cleanText)) {
        val numericText = cleanText.replace("[^0-9,.]".toRegex(), "")
        scannedAmount.value = numericText
        message = "Amount captured: $numericText"
    }

    return message
}

suspend fun showFeedback(
    message: String,
    showFeedback: MutableState<Boolean>,
    feedbackMessage: MutableState<String>
) {
    // Update feedback message and show it
    feedbackMessage.value = message
    showFeedback.value = true

    // Hide feedback after 3 seconds
    delay(3000)
    showFeedback.value = false
}

fun formatDate(dateString: String): String? {
    val formats = arrayOf(
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "yyyy/MM/dd",
        "dd/MM/yyyy",
        "yyyy.MM.dd",
        "dd.MM.yyyy"
    )

    formats.forEach { format ->
        try {
            val inputFormat = SimpleDateFormat(format, Locale.US)
            val date = inputFormat.parse(dateString)
            if (date != null) {
                // Output in the format expected by the parent view
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                return outputFormat.format(date)
            }
        } catch (e: Exception) {
            // Try next format
        }
    }

    return null
}