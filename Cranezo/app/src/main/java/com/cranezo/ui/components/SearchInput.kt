package com.cranezo.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.cranezo.ui.theme.*

/**
 * The search field of the whole app: glass, 44 high, radius 14, a constant 1.5 border whose colour
 * turns amber on focus. Width comes from the parent. The clear button takes the place of the spinner
 * while loading, and the field height never changes.
 */
@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onSearch: ((String) -> Unit)? = null
) {
    var focused by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val shape = RoundedCornerShape(dimen_14)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimen_44)
            .glassEffect(shape, borderColor = if (focused) AmberPrimary else androidx.compose.ui.graphics.Color.Unspecified, borderWidth = 1.5.dp)
            .padding(start = dimen_16, end = dimen_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_20))
        Spacer(modifier = Modifier.width(dimen_12))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused },
            textStyle = TextStyle(fontSize = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Medium),
            cursorBrush = SolidColor(AmberPrimary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, autoCorrect = false, capitalization = KeyboardCapitalization.None),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch?.invoke(value)
                keyboard?.hide()
            }),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Textview(placeholder, size = font_14, color = appColors.textHint, maxLines = 1)
                    inner()
                }
            }
        )
        Spacer(modifier = Modifier.width(dimen_8))
        when {
            isLoading -> CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_2, modifier = Modifier.size(dimen_16))
            value.isNotEmpty() -> Icon(
                Icons.Default.Close, contentDescription = null, tint = appColors.textSecondary,
                modifier = Modifier.size(dimen_20).clickable { onValueChange("") }
            )
        }
    }
}
