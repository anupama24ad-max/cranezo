package com.cranezo.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cranezo.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.cranezo.ui.theme.*

// Glass form components used by the role based profile form.
// They all sit on top of Modifier.glassEffect() so they follow light / dark mode.

/**
 * Scroll state for a screen with entry fields. While the keyboard is open it scrolls to the bottom,
 * so the action button under the field stays visible.
 *
 * Use it with the screen's root column, in this order (nav bar padding before IME padding, so the
 * two insets aren't counted twice):
 * ```
 * Column(
 *     Modifier.fillMaxSize()
 *         .statusBarsPadding()
 *         .navigationBarsPadding()
 *         .imePadding()
 *         .verticalScroll(rememberImeScrollState())
 * ) { ... Spacer(Modifier.weight(1f)) ... }
 * ```
 * `weight` still works inside the scrolling column: it fills the screen height when there is room
 * and collapses when the keyboard takes it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun rememberImeScrollState(): ScrollState {
    val scrollState = rememberScrollState()
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible, scrollState.maxValue) {
        if (isImeVisible) scrollState.animateScrollTo(scrollState.maxValue)
    }
    return scrollState
}

/** One entry of a dropdown, usually mapped from the dropdown API (id + display name). */
data class SelectOption(val id: Int, val name: String)

@Composable
private fun GlassFieldLabel(text: String, isRequired: Boolean) {
    Row(modifier = Modifier.padding(bottom = dimen_8)) {
        Textview(
            text = text,
            size = font_14,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
        if (isRequired) {
            Textview(text = " *", size = font_14, color = Color.Red)
        }
    }
}

/**
 * Glass container for a single form row. Use it to build custom fields that match the text field.
 */
@Composable
fun GlassFieldBox(
    modifier: Modifier = Modifier,
    minHeight: Dp = dimen_52,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .glassEffect(RoundedCornerShape(dimen_14))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = dimen_12),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Glass text input with a label, an optional leading icon and optional trailing content.
 * Pass `singleLine = false` and a taller [minHeight] for a multi-line text area.
 */
@Composable
fun GlassInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leftIcon: ImageVector? = null,
    isRequired: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minHeight: Dp = dimen_52,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    rightContent: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        GlassFieldLabel(label, isRequired)
        GlassFieldBox(minHeight = minHeight) {
            if (leftIcon != null) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = null,
                    tint = appColors.textHint,
                    modifier = Modifier.size(dimen_20)
                )
                Spacer(modifier = Modifier.width(dimen_12))
            }
            BasicTextField(
                value = value,
                onValueChange = { if (!readOnly) onValueChange(it) },
                readOnly = readOnly,
                singleLine = singleLine,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = if (singleLine) dimen_0 else dimen_12),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    capitalization = capitalization,
                    imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
                ),
                textStyle = TextStyle(
                    fontSize = font_15,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(AmberPrimary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Textview(text = placeholder, size = font_14, color = appColors.textHint)
                        }
                        innerTextField()
                    }
                }
            )
            if (rightContent != null) {
                Spacer(modifier = Modifier.width(dimen_8))
                rightContent()
            }
        }
    }
}

/**
 * Glass dropdown. Shows [value] (or [placeholder]) and opens a menu of [options].
 */
@Composable
fun GlassSelectField(
    label: String,
    value: String,
    placeholder: String,
    options: List<SelectOption>,
    onSelected: (SelectOption) -> Unit,
    modifier: Modifier = Modifier,
    leftIcon: ImageVector? = null,
    isRequired: Boolean = false,
    // Called each time the menu opens, e.g. to reload options that failed to load
    onOpen: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        GlassFieldLabel(label, isRequired)
        Box {
            GlassFieldBox(onClick = {
                expanded = true
                onOpen()
            }) {
                if (leftIcon != null) {
                    Icon(
                        imageVector = leftIcon,
                        contentDescription = null,
                        tint = appColors.textHint,
                        modifier = Modifier.size(dimen_20)
                    )
                    Spacer(modifier = Modifier.width(dimen_12))
                }
                Textview(
                    text = value.ifEmpty { placeholder },
                    size = if (value.isEmpty()) font_14 else font_15,
                    color = if (value.isEmpty()) appColors.textHint else appColors.textPrimary,
                    fontWeight = if (value.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(dimen_20)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (options.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.common_no_options)) },
                        onClick = { expanded = false }
                    )
                }
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/** "2031-05-20" -> "20-05-2031". Anything that isn't a yyyy-MM-dd date is shown as it is. */
private fun String.toDisplayDate(): String {
    val parts = split("-")
    return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else this
}

/**
 * Glass date field. Opens the system date picker. The value is a `yyyy-MM-dd` date (the format the
 * API uses) and is shown to the user as `dd-MM-yyyy`.
 */
@Composable
fun GlassDateField(
    label: String,
    value: String,
    placeholder: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    allowPastDates: Boolean = true,
    allowFutureDates: Boolean = true
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        GlassFieldLabel(label, isRequired)
        GlassFieldBox(
            onClick = {
                val now = java.util.Calendar.getInstance()
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        onDateSelected(
                            String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                        )
                    },
                    now.get(java.util.Calendar.YEAR),
                    now.get(java.util.Calendar.MONTH),
                    now.get(java.util.Calendar.DAY_OF_MONTH)
                ).apply {
                    if (!allowPastDates) datePicker.minDate = System.currentTimeMillis() - 1000
                    if (!allowFutureDates) datePicker.maxDate = System.currentTimeMillis()
                }.show()
            }
        ) {
            Textview(
                text = value.toDisplayDate().ifEmpty { placeholder },
                size = if (value.isEmpty()) font_14 else font_15,
                color = if (value.isEmpty()) appColors.textHint else appColors.textPrimary,
                fontWeight = if (value.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = appColors.textSecondary,
                modifier = Modifier.size(dimen_20)
            )
        }
    }
}

/**
 * Row of radio options, e.g. "Individual User" / "Business User".
 */
@Composable
fun GlassRadioGroup(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    // Text shown for an option, when it differs from the value (e.g. a translation)
    displayName: @Composable (String) -> String = { it }
) {
    Column(modifier = modifier.fillMaxWidth()) {
        GlassFieldLabel(label, isRequired = false)
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_24)) {
            options.forEach { option ->
                val isSelected = option == selected
                Row(
                    modifier = Modifier.clickable { onSelect(option) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimen_18)
                            .border(
                                width = dimen_1 * 1.5f,
                                color = if (isSelected) AmberPrimary else appColors.textHint,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(dimen_10)
                                    .clip(CircleShape)
                                    .background(AmberPrimary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(dimen_8))
                    Textview(
                        text = displayName(option),
                        size = font_14,
                        color = if (isSelected) appColors.textPrimary else appColors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * Section title with a leading amber icon, e.g. "License & Experience Details".
 */
@Composable
fun GlassSectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AmberPrimary,
                modifier = Modifier.size(dimen_18)
            )
            Spacer(modifier = Modifier.width(dimen_8))
            Textview(
                text = title,
                size = font_16,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(dimen_4))
            Textview(text = subtitle, size = font_12, color = appColors.textSecondary)
        }
    }
}

/**
 * Small tile to pick a document, with a tick once it has been uploaded.
 */
@Composable
fun DocumentUploadTile(
    title: String,
    subtitle: String,
    isUploaded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUploading: Boolean = false
) {
    Column(
        modifier = modifier
            .glassEffect(
                shape = RoundedCornerShape(dimen_12),
                borderColor = if (isUploaded) AmberPrimary else Color.Unspecified
            )
            .clickable(enabled = !isUploading) { onClick() }
            .padding(vertical = dimen_12, horizontal = dimen_6),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(dimen_32)
                .clip(CircleShape)
                .background(if (isUploaded) AmberPrimary else appColors.subtleFill),
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    color = AmberPrimary,
                    strokeWidth = dimen_2,
                    modifier = Modifier.size(dimen_18)
                )
            } else {
                Icon(
                    imageVector = if (isUploaded) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isUploaded) Color.Black else appColors.textSecondary,
                    modifier = Modifier.size(dimen_18)
                )
            }
        }
        Spacer(modifier = Modifier.height(dimen_6))
        Textview(
            text = title,
            size = font_11,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Textview(
            text = subtitle,
            size = font_11,
            color = appColors.textHint,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Small amber pill button, e.g. "Verify" inside a field.
 */
@Composable
fun ActionPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(dimen_12))
            .background(Brush.horizontalGradient(listOf(LightYellow, DarkYellow)))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = dimen_10, vertical = dimen_4),
        contentAlignment = Alignment.Center
    ) {
        Textview(text = text, size = font_11, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}
