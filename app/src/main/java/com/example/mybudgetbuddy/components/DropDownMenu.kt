package com.example.mybudgetbuddy.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

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

    // For calculating dropdown position
    val rowReference = remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            if (showNewCategoryField) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
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
                        .clickable {
                            expanded = true
                            focusManager.clearFocus()
                        }
                        .padding(16.dp)
                        .onGloballyPositioned { coordinates ->
                            rowReference.value = coordinates
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCategory.ifEmpty { "Choose Category" }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color.Gray
                    )
                }
            }
        }

        // Custom popup positioned exactly below the "Choose Category" field
        if (expanded) {
            val positionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    // Calculate the x position to center the dropdown
                    val x = (anchorBounds.left + anchorBounds.right - popupContentSize.width) / 2

                    // Position just below the "Choose Category" field
                    val y = anchorBounds.bottom + 5 // Small offset from the bottom of the anchor

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
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.95f) // Slightly transparent white
                ) {
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