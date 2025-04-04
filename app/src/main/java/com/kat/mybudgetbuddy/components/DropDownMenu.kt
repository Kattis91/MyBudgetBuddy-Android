package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex


@Composable
fun CategoryMenu(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    showNewCategoryField: Boolean,
    onShowNewCategoryFieldChange: (Boolean) -> Unit,
    newCategory: String = "",
    onNewCategoryChange: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val rowReference = remember { mutableStateOf<LayoutCoordinates?>(null) }

    val isDarkMode = isSystemInDarkTheme()

    // Swift-like gradient colors
    val gradientColors = if (isDarkMode) {
        listOf(Color.DarkGray, Color.Black)
    } else {
        listOf(
            Color(red = 245f/255f, green = 247f/255f, blue = 245f/255f),
            Color(red = 240f/255f, green = 242f/255f, blue = 240f/255f)
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Keep Card but with Swift-like styling
        Box(
            modifier = Modifier
                .zIndex(0f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(45.dp)
                .shadow(
                    elevation = if (isDarkMode) 2.dp else 1.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = if (isDarkMode) 0.35f else 0.25f),
                    ambientColor = Color.Black.copy(alpha = if (isDarkMode) 0.35f else 0.25f),
                    clip = false
                )
                .graphicsLayer(
                    shadowElevation = 4f,
                    shape = RoundedCornerShape(16.dp)
                )
                .offset(x = (3).dp, y = (-3).dp)
                .background(
                    Brush.horizontalGradient(
                        colors = gradientColors,
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .border(
                    width = if (isDarkMode) 0.6.dp else 0.8.dp,
                    color = Color.White.copy(alpha = if (isDarkMode) 0.3f else 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
                .onGloballyPositioned { coordinates ->
                    rowReference.value = coordinates
                }
        ) {
            if (showNewCategoryField) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomTextField(
                        value = newCategory,
                        onValueChange = { onNewCategoryChange(it) },
                        label = "New category",
                        icon = Icons.Filled.AddCircle,
                    )
                    IconButton(onClick = {
                        onShowNewCategoryFieldChange(false)
                        onCategorySelected("")
                        onNewCategoryChange("")
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Blue
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clickable {
                            expanded = true
                            focusManager.clearFocus()
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCategory.ifEmpty { "Choose Category" },
                        color = if (isDarkMode) Color.White else Color.Black,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                }
            }
        }

        // Popup styling remains mostly the same
        if (expanded) {
            val positionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    val x = (anchorBounds.left + anchorBounds.right - popupContentSize.width) / 2
                    val y = anchorBounds.bottom + 10
                    return IntOffset(x, y)
                }
            }

            Popup(
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
                popupPositionProvider = positionProvider
            ) {
                Surface(
                    modifier = Modifier
                        .width(280.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                        .zIndex(1f), // Ensures dropdown is above other elements
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.95f)
                )
                {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEachIndexed { index, category ->
                            CustomDropdownItem(
                                text = category,
                                onClick = {
                                    onCategorySelected(category)
                                    expanded = false
                                }
                            )

                            if (index < categories.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 0.5.dp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray
                        )

                        CustomDropdownItem(
                            text = "Add new category",
                            onClick = {
                                onCategorySelected("new")
                                onShowNewCategoryFieldChange(true)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add new category",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}