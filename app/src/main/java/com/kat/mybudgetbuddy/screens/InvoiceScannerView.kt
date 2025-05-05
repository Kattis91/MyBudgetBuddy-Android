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
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
                ImprovedCameraPreview(
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
                                        Color(0xFF4CAF50), // Start color
                                        Color(0xFF43A047), // Middle color
                                        Color(0xFF388E3C)  // End color
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
fun ImprovedCameraPreview(
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

    // Use high accuracy model option for better text recognition
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val executor = remember { Executors.newSingleThreadExecutor() }
    val processingTouch = remember { AtomicBoolean(false) }

    // Store all detected text blocks for better context analysis
    val detectedTextBlocks = remember { SnapshotStateList<Text.TextBlock>() }
    val lastCaptureTime = remember { mutableStateOf(0L) }

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

            // Configure camera for higher resolution
            val cameraResolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setResolutionSelector(cameraResolutionSelector)
                .build()
                .apply {
                    setAnalyzer(executor, ImprovedTextAnalyzer(textRecognizer, detectedTextBlocks, lastCaptureTime))
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
                                // Get the view dimensions
                                val viewWidth = view.width.toFloat()
                                val viewHeight = view.height.toFloat()

                                // Convert touch point to relative coordinates (0.0-1.0)
                                val relativeX = touchX / viewWidth
                                val relativeY = touchY / viewHeight

                                // Process the point
                                processTextNearPoint(
                                    detectedTextBlocks.toList(),
                                    relativeX,
                                    relativeY,
                                    scannedAmount,
                                    scannedDueDate,
                                    showFeedback,
                                    feedbackMessage
                                )
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

class ImprovedTextAnalyzer(
    private val textRecognizer: TextRecognizer,
    private val detectedTextBlocks: SnapshotStateList<Text.TextBlock>,
    private val lastCaptureTime: MutableState<Long>
) : ImageAnalysis.Analyzer {

    // Process every Nth frame to avoid overloading
    private var frameCounter = 0
    private val processEveryNthFrame = 15  // Increase to reduce processing frequency
    private val minTimeBetweenCaptures = 500L // Milliseconds between text detection attempts

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        try {
            // Throttle processing for better performance and quality
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCaptureTime.value < minTimeBetweenCaptures) {
                imageProxy.close()
                return
            }

            // Only process every Nth frame
            frameCounter = (frameCounter + 1) % processEveryNthFrame
            if (frameCounter != 0) {
                imageProxy.close()
                return
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                lastCaptureTime.value = currentTime

                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        try {
                            CoroutineScope(Dispatchers.Main).launch {
                                // Clear old data periodically to avoid buildup
                                if (detectedTextBlocks.size > 30) {
                                    detectedTextBlocks.clear()
                                }

                                // Add new text blocks
                                visionText.textBlocks.forEach { block ->
                                    if (!detectedTextBlocks.any { it.text == block.text }) {
                                        detectedTextBlocks.add(block)
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

suspend fun processTextNearPoint(
    textBlocks: List<Text.TextBlock>,
    relativeX: Float,
    relativeY: Float,
    scannedAmount: MutableState<String>,
    scannedDueDate: MutableState<String>,
    showFeedback: MutableState<Boolean>,
    feedbackMessage: MutableState<String>
) {
    try {
        // First try to find blocks containing amounts or dates near the touch point
        val nearbyTexts = findTextsNearPoint(textBlocks, relativeX, relativeY)

        if (nearbyTexts.isNotEmpty()) {
            // Try to detect amount and date from nearby text
            processNearbyTexts(nearbyTexts, scannedAmount, scannedDueDate, showFeedback, feedbackMessage)
            return
        }

        // If no nearby text found, analyze all detected text
        val results = analyzeAllDetectedBlocks(textBlocks, scannedAmount, scannedDueDate)

        if (results.isNotEmpty()) {
            showFeedback(results.joinToString("\n"), showFeedback, feedbackMessage)
        } else {
            showFeedback("No amount or date found. Try moving the camera closer.", showFeedback, feedbackMessage)
        }
    } catch (e: Exception) {
        Log.e("InvoiceScanner", "Error in processTextNearPoint", e)
    }
}

fun findTextsNearPoint(textBlocks: List<Text.TextBlock>, relativeX: Float, relativeY: Float): List<String> {
    val nearbyTexts = mutableListOf<String>()

    // Find text blocks near the touch point
    textBlocks.forEach { block ->
        // Check if any of the block's bounding box corners are near the touch point
        val boundingBox = block.boundingBox
        if (boundingBox != null) {
            // Convert bounding box to relative coordinates
            val left = boundingBox.left.toFloat() / 1000f  // Using normalized coordinates
            val top = boundingBox.top.toFloat() / 1000f
            val right = boundingBox.right.toFloat() / 1000f
            val bottom = boundingBox.bottom.toFloat() / 1000f

            // Check if point is inside or very close to the bounding box (with margin)
            val margin = 0.05f  // 5% margin
            if (relativeX >= left - margin && relativeX <= right + margin &&
                relativeY >= top - margin && relativeY <= bottom + margin) {

                nearbyTexts.add(block.text)

                // Also add individual lines for more precise matching
                block.lines.forEach { line ->
                    nearbyTexts.add(line.text)
                }
            }
        }
    }

    return nearbyTexts
}

suspend fun processNearbyTexts(
    texts: List<String>,
    scannedAmount: MutableState<String>,
    scannedDueDate: MutableState<String>,
    showFeedback: MutableState<Boolean>,
    feedbackMessage: MutableState<String>
) {
    val results = mutableListOf<String>()

    for (text in texts) {
        // Try to find amount
        val amountResult = findAmount(text)
        if (amountResult != null && scannedAmount.value.isEmpty()) {
            scannedAmount.value = amountResult
            results.add("Amount captured: $amountResult")
        }

        // Try to find date
        val dateResult = findDate(text)
        if (dateResult != null && scannedDueDate.value.isEmpty()) {
            scannedDueDate.value = dateResult
            results.add("Due date captured: $dateResult")
        }
    }

    if (results.isNotEmpty()) {
        showFeedback(results.joinToString("\n"), showFeedback, feedbackMessage)
    } else {
        // If nothing found in nearby text, show guidance
        showFeedback("Tap directly on amount or due date", showFeedback, feedbackMessage)
    }
}

fun analyzeAllDetectedBlocks(
    textBlocks: List<Text.TextBlock>,
    scannedAmount: MutableState<String>,
    scannedDueDate: MutableState<String>
): List<String> {
    val results = mutableListOf<String>()

    // Join all text for comprehensive analysis
    val fullText = textBlocks.joinToString(" ") { it.text }

    // Find amount if not already set
    if (scannedAmount.value.isEmpty()) {
        val amount = findAmountInFullText(fullText)
        if (amount != null) {
            scannedAmount.value = amount
            results.add("Amount captured: $amount")
        }
    }

    // Find date if not already set
    if (scannedDueDate.value.isEmpty()) {
        val date = findDateInFullText(fullText)
        if (date != null) {
            scannedDueDate.value = date
            results.add("Due date captured: $date")
        }
    }

    return results
}

fun findAmount(text: String): String? {
    val cleanText = text.trim()
    if (cleanText.isEmpty()) return null

    try {
        // Patterns for various amount formats with priority
        val amountPatterns = listOf(
            // Pattern for "Due amount: 1,234.56" or "Total: 1,234.56"
            "(due|total|amount|sum|price|invoice|balance)[:\\s]+(\\d{1,3}(?:[., ]\\d{3})*[.,]\\d{2})",
            // Pattern for amounts with thousand separators and decimals (e.g., "1,234.56" or "1 234,56")
            "(\\d{1,3}(?:[., ]\\d{3})*[.,]\\d{2})",
            // Pattern for simple amounts like "123.45"
            "(\\d+[.,]\\d{2})"
        )

        for (pattern in amountPatterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(cleanText)
            if (matcher.find()) {
                // Different extraction based on pattern
                val match = if (pattern.startsWith("(due|total")) {
                    matcher.group(2) // Extract amount part from labeled amount
                } else {
                    matcher.group(1) // Extract whole match for direct amount patterns
                }

                if (match != null) {
                    // Standardize format - keep decimals but remove spaces
                    var standardized = match.replace(" ", "")

                    // Make sure we have a consistent decimal format (using .)
                    if (standardized.contains(",") && !standardized.contains(".")) {
                        standardized = standardized.replace(",", ".")
                    }

                    Log.d("InvoiceScanner", "Amount found: $standardized from text: $cleanText")
                    return standardized
                }
            }
        }
    } catch (e: Exception) {
        Log.e("InvoiceScanner", "Error finding amount", e)
    }

    return null
}

fun findAmountInFullText(fullText: String): String? {
    // First check for labeled amounts
    val labeledAmountPatterns = listOf(
        "(total|amount|sum|due|balance|payment)[:\\s]+(\\d{1,3}(?:[., ]\\d{3})*[.,]\\d{2})",
        "(amount|sum|total)[:\\s]+(\\d+)[.,](\\d{2})"
    )

    for (pattern in labeledAmountPatterns) {
        val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(fullText)
        if (matcher.find()) {
            val amountText = matcher.group(2) ?: continue
            var standardized = amountText.replace(" ", "")
            if (standardized.contains(",") && !standardized.contains(".")) {
                standardized = standardized.replace(",", ".")
            }
            return standardized
        }
    }

    // Then check for standalone amounts
    return findAmount(fullText)
}

fun findDate(text: String): String? {
    val cleanText = text.trim()
    if (cleanText.isEmpty()) return null

    try {
        // Expanded date patterns with labeled dates having higher priority
        val datePatterns = arrayOf(
            // Labeled dates with various formats
            "(due|payment|date)[:\\s]+(\\d{1,2}[-/.\\s]\\d{1,2}[-/.\\s]\\d{2,4})",
            "(due|payment|date)[:\\s]+(\\d{4}[-/.\\s]\\d{1,2}[-/.\\s]\\d{1,2})",
            // Common date formats - year first (2023-05-30)
            "(20\\d{2})[-/.\\s](0?[1-9]|1[0-2])[-/.\\s](0?[1-9]|[12]\\d|3[01])",
            // Day first (30-05-2023)
            "(0?[1-9]|[12]\\d|3[01])[-/.\\s](0?[1-9]|1[0-2])[-/.\\s](20\\d{2})",
            // Month first (05/30/2023)
            "(0?[1-9]|1[0-2])[-/.\\s](0?[1-9]|[12]\\d|3[01])[-/.\\s](20\\d{2})"
        )

        for (pattern in datePatterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(cleanText)
            if (matcher.find()) {
                val dateMatch = if (pattern.startsWith("(due|payment|date)")) {
                    matcher.group(2) // Extract date part from labeled date
                } else {
                    matcher.group() // Extract whole match for direct date patterns
                }

                if (dateMatch != null) {
                    val formattedDate = formatDate(dateMatch)
                    if (formattedDate != null) {
                        Log.d("InvoiceScanner", "Date found: $formattedDate from text: $cleanText")
                        return formattedDate
                    }
                }
            }
        }

        // Check for dates in words (e.g., "30 May 2023" or "May 30, 2023")
        val monthNames = "(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)"
        val textualDatePatterns = arrayOf(
            "(\\d{1,2})\\s+($monthNames)\\s+(20\\d{2})",
            "($monthNames)\\s+(\\d{1,2})(?:st|nd|rd|th)?[,.]?\\s+(20\\d{2})"
        )

        for (pattern in textualDatePatterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(cleanText)
            if (matcher.find()) {
                try {
                    // Different parsing based on pattern (day-month-year or month-day-year)
                    val formattedDate = if (pattern.startsWith("(\\d{1,2})")) {
                        // Pattern 1: day month year
                        val day = matcher.group(1)
                        val month = parseMonth(matcher.group(2))
                        val year = matcher.group(3)
                        "$year-$month-$day"
                    } else {
                        // Pattern 2: month day year
                        val month = parseMonth(matcher.group(1))
                        val day = matcher.group(2).replace(Regex("[^0-9]"), "")
                        val paddedDay = day.padStart(2, '0')
                        val year = matcher.group(3)
                        "$year-$month-$paddedDay"
                    }

                    Log.d("InvoiceScanner", "Textual date found: $formattedDate")
                    return formattedDate
                } catch (e: Exception) {
                    Log.e("InvoiceScanner", "Error parsing textual date", e)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("InvoiceScanner", "Error finding date", e)
    }

    return null
}

fun findDateInFullText(fullText: String): String? {
    // First look for labeled dates
    val labeledDatePatterns = listOf(
        "(due date|payment date|date due)[:\\s]+(\\d{1,2}[-/.\\s]\\d{1,2}[-/.\\s]\\d{2,4})",
        "(due date|payment date|date due)[:\\s]+(\\d{4}[-/.\\s]\\d{1,2}[-/.\\s]\\d{1,2})"
    )

    for (pattern in labeledDatePatterns) {
        val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(fullText)
        if (matcher.find()) {
            val dateText = matcher.group(2) ?: continue
            val formattedDate = formatDate(dateText)
            if (formattedDate != null) {
                return formattedDate
            }
        }
    }

    // Look for textual dates with "due" context
    val contextPatterns = listOf(
        "(due|pay(?:able)?\\s+by|payment\\s+by)\\s+(?:the\\s+)?(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?([A-Za-z]+)(?:,?\\s+|\\s*)(20\\d{2})",
        "(due|pay(?:able)?\\s+by|payment\\s+by)\\s+(?:the\\s+)?([A-Za-z]+)\\s+(\\d{1,2})(?:st|nd|rd|th)?(?:,?\\s+|\\s*)(20\\d{2})"
    )

    for (pattern in contextPatterns) {
        val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(fullText)
        if (matcher.find()) {
            try {
                val formattedDate = if (pattern.contains("(\\d{1,2})(?:st|nd|rd|th)?\\s")) {
                    // Day, Month, Year format
                    val day = matcher.group(2)?.padStart(2, '0')
                    val month = parseMonth(matcher.group(3))
                    val year = matcher.group(4)
                    "$year-$month-$day"
                } else {
                    // Month, Day, Year format
                    val month = parseMonth(matcher.group(2))
                    val day = matcher.group(3)?.padStart(2, '0')
                    val year = matcher.group(4)
                    "$year-$month-$day"
                }
                return formattedDate
            } catch (e: Exception) {
                Log.e("InvoiceScanner", "Error parsing contextual date", e)
            }
        }
    }

    // Then fall back to any date pattern
    return findDate(fullText)
}

fun parseMonth(monthText: String?): String {
    if (monthText == null) return "01"

    return when (monthText.lowercase().take(3)) {
        "jan" -> "01"
        "feb" -> "02"
        "mar" -> "03"
        "apr" -> "04"
        "may" -> "05"
        "jun" -> "06"
        "jul" -> "07"
        "aug" -> "08"
        "sep" -> "09"
        "oct" -> "10"
        "nov" -> "11"
        "dec" -> "12"
        else -> "01" // Default to January if unrecognized
    }
}

// Completing the formatDate function that was cut off
fun formatDate(dateString: String): String? {
    if (dateString.isEmpty()) return null

    try {
        // Clean up the input string
        val cleanDateString = dateString.replace("[^0-9/.-]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()

        // Check for invalid input
        if (!cleanDateString.matches(".*\\d+.*".toRegex())) {
            return null
        }

        val formats = arrayOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("dd-MM-yyyy", Locale.US),
            SimpleDateFormat("MM-dd-yyyy", Locale.US),
            SimpleDateFormat("yyyy/MM/dd", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("MM/dd/yyyy", Locale.US),
            SimpleDateFormat("yyyy.MM.dd", Locale.US),
            SimpleDateFormat("dd.MM.yyyy", Locale.US),
            SimpleDateFormat("MM.dd.yyyy", Locale.US),
            SimpleDateFormat("dd MM yyyy", Locale.US),
            SimpleDateFormat("MM dd yyyy", Locale.US),
            SimpleDateFormat("yyyy MM dd", Locale.US)
        )

        for (format in formats) {
            try {
                format.isLenient = false // Enforce strict parsing
                val date = format.parse(cleanDateString)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date

                    // Validate the date (skip dates in the distant past or future)
                    val year = cal.get(Calendar.YEAR)
                    if (year < 2010 || year > 2030) {
                        continue
                    }

                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val formatted = outputFormat.format(date)
                    Log.d("InvoiceScanner", "Date formatted: $formatted from $cleanDateString")
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

// Function to show feedback to the user
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

// Additional utility functions to make the recognition more robust

// Function to validate recognized amount
fun validateAmount(amount: String): Boolean {
    // Check if string has a valid decimal format (with either . or , as separator)
    val hasValidFormat = amount.matches("\\d+[.,]\\d{1,2}".toRegex())

    // Check if amount is within a reasonable range (e.g., not too large or too small)
    val numericValue = try {
        amount.replace(",", ".").toDouble()
    } catch (e: Exception) {
        0.0
    }

    // Reasonable amount range check
    val isReasonableAmount = numericValue > 0.0 && numericValue < 1000000.0

    return hasValidFormat && isReasonableAmount
}

// Function to validate recognized date
fun validateDate(date: String): Boolean {
    if (!date.matches("\\d{4}-\\d{2}-\\d{2}".toRegex())) {
        return false
    }

    try {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        // Basic validation
        if (year < 2010 || year > 2030) return false
        if (month < 1 || month > 12) return false
        if (day < 1 || day > 31) return false

        // Month-specific validation
        val daysInMonth = when(month) {
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }

        if (day > daysInMonth) return false

        // Check if date is not too far in the past or future
        val currentDate = Calendar.getInstance()
        val enteredDate = Calendar.getInstance()
        enteredDate.set(year, month - 1, day)

        val sixMonthsAgo = Calendar.getInstance()
        sixMonthsAgo.add(Calendar.MONTH, -6)

        val oneYearAhead = Calendar.getInstance()
        oneYearAhead.add(Calendar.YEAR, 1)

        return enteredDate.after(sixMonthsAgo) && enteredDate.before(oneYearAhead)

    } catch (e: Exception) {
        return false
    }
}

// Enhanced context-aware detection function
fun findKeyValuePair(text: String, key: String): String? {
    val pattern = "$key\\s*[:\\-=]\\s*([\\d.,]+|\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}|\\d{4}[/.-]\\d{1,2}[/.-]\\d{1,2})"
    val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text)

    if (matcher.find()) {
        return matcher.group(1)?.trim()
    }

    return null
}

// Function to find context for better detection
fun detectLabelledValues(fullText: String): Map<String, String> {
    val results = mutableMapOf<String, String>()

    // Array of common labels and their variations
    val amountLabels = arrayOf(
        "amount", "total", "due", "balance", "payment", "sum", "fee", "price",
        "amount due", "total due", "balance due", "payment due", "total amount"
    )

    val dateLabels = arrayOf(
        "date", "due date", "due on", "payment date", "pay by", "payable by",
        "expiry", "expiration", "expiry date", "due by", "valid until"
    )

    // Check for each amount label
    for (label in amountLabels) {
        findKeyValuePair(fullText, label)?.let { value ->
            if (value.matches(".*\\d+[.,]\\d{1,2}.*".toRegex())) {
                // Extract just the amount part
                val amountPattern = "(\\d{1,3}(?:[., ]\\d{3})*[.,]\\d{1,2})"
                val matcher = Pattern.compile(amountPattern).matcher(value)
                if (matcher.find()) {
                    results["amount"] = matcher.group(1)!!
                } else {
                    results["amount"] = value
                }
            }
        }
    }

    // Check for each date label
    for (label in dateLabels) {
        findKeyValuePair(fullText, label)?.let { value ->
            // Try to format the detected date
            formatDate(value)?.let { formattedDate ->
                results["date"] = formattedDate
            }
        }
    }

    return results
}

// Comprehensive function to handle invoice text
fun processInvoiceText(fullText: String, scannedAmount: MutableState<String>, scannedDueDate: MutableState<String>): List<String> {
    val results = mutableListOf<String>()

    // First try to detect labeled values for more accuracy
    val labelledValues = detectLabelledValues(fullText)

    // Process amount if found and not already set
    if ("amount" in labelledValues && scannedAmount.value.isEmpty()) {
        val amount = labelledValues["amount"]!!
        val standardizedAmount = standardizeAmount(amount)
        if (validateAmount(standardizedAmount)) {
            scannedAmount.value = standardizedAmount
            results.add("Amount captured: $standardizedAmount")
        }
    }

    // Process date if found and not already set
    if ("date" in labelledValues && scannedDueDate.value.isEmpty()) {
        val date = labelledValues["date"]!!
        if (validateDate(date)) {
            scannedDueDate.value = date
            results.add("Due date captured: $date")
        }
    }

    // If labelled detection didn't work, try generic pattern detection
    if (results.isEmpty()) {
        // Look for amounts if none set yet
        if (scannedAmount.value.isEmpty()) {
            val amount = findAmount(fullText)
            if (amount != null) {
                val standardizedAmount = standardizeAmount(amount)
                if (validateAmount(standardizedAmount)) {
                    scannedAmount.value = standardizedAmount
                    results.add("Amount captured: $standardizedAmount")
                }
            }
        }

        // Look for dates if none set yet
        if (scannedDueDate.value.isEmpty()) {
            val date = findDate(fullText)
            if (date != null && validateDate(date)) {
                scannedDueDate.value = date
                results.add("Due date captured: $date")
            }
        }
    }

    return results
}

// Function to standardize amount format
fun standardizeAmount(amount: String): String {
    // Remove all spaces
    var result = amount.replace(" ", "")

    // Standardize decimal separator to '.'
    if (result.contains(",") && !result.contains(".")) {
        result = result.replace(",", ".")
    }

    // Make sure we have two decimal places
    if (!result.contains(".")) {
        result = "$result.00"
    } else {
        val parts = result.split(".")
        if (parts.size == 2 && parts[1].length == 1) {
            result = "${parts[0]}.${parts[1]}0"
        }
    }

    return result
}

// Main function to complete the implementation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteInvoiceScannerView(
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
                ImprovedCameraPreview(
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
                            text = "Point camera at amount",
                            color = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }

                    if (internalDueDate.value.isEmpty()) {
                        Text(
                            text = "Point camera at due date",
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                }

                // Detected values display
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    if (internalAmount.value.isNotEmpty()) {
                        Text(
                            text = "Amount: ${internalAmount.value}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(Color(0xFF2E7D32).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }

                    if (internalDueDate.value.isNotEmpty()) {
                        Text(
                            text = "Due: ${internalDueDate.value}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(Color(0xFF2E7D32).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
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
                                        Color(0xFF4CAF50), // Start color
                                        Color(0xFF43A047), // Middle color
                                        Color(0xFF388E3C)  // End color
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    )
                }

                // Scanning helper that shows scanning in progress
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(200.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
                    val position by infiniteTransition.animateFloat(
                        initialValue = -100f,
                        targetValue = 100f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scanner_position"
                    )

                    // Scan line
                    Box(
                        modifier = Modifier
                            .offset(0.dp, position.dp)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF4CAF50),
                                        Color(0xFF43A047),
                                        Color(0xFF4CAF50),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera permission required",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}
