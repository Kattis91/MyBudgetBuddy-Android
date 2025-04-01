package com.example.mybudgetbuddy.components


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbuddy.R

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    onChange: (() -> Unit)? = null
) {
    val isDarkMode = isSystemInDarkTheme()

    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
    val labelColor = if (isDarkMode) Color.LightGray else Color.DarkGray

    StyledCard {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = textColor,
                fontSize = 16.sp
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display icon if provided
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = labelColor,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    // Place the label and text field directly in the Row
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = label,
                                color = labelColor,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }

    LaunchedEffect(value) { // Observe changes to the 'value' parameter
        onChange?.invoke() // Call the onChange callback
    }
}