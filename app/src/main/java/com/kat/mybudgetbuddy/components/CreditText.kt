package com.kat.mybudgetbuddy.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun CreditsText() {
    val context = LocalContext.current
    val url = "https://www.flaticon.com"

    Text(
        text = "Flaticon.",
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        },
        textDecoration = TextDecoration.Underline
    )
}