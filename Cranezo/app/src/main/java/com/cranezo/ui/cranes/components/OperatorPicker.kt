package com.cranezo.ui.cranes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.model.Operator
import com.cranezo.ui.mainhome.components.StatusChip
import com.cranezo.ui.theme.*
import androidx.compose.ui.graphics.Color

/**
 * Bottom sheet to choose the operator of a crane. It searches the owner's operators
 * (POST /owner/operators { search }) and can add a new one (POST /owner/operators/add).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorPickerSheet(
    operators: List<Operator>,
    selectedId: String?,
    isLoading: Boolean,
    isAdding: Boolean,
    onSearch: (String) -> Unit,
    onSelect: (Operator?) -> Unit,
    onAdd: (name: String, mobile: String, email: String) -> Unit,
    onDismiss: () -> Unit,
    // Set to true by the screen when the operator was added, which closes the add form
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    var adding by rememberSaveable { mutableStateOf(false) }

    // Fresh list every time the sheet opens
    LaunchedEffect(Unit) { onSearch("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = dimen_20, vertical = dimen_8)
        ) {
            if (adding) {
                AddOperatorForm(
                    isAdding = isAdding,
                    onBack = { adding = false },
                    onAdd = onAdd
                )
            } else {
                Textview(
                    text = stringResource(R.string.crane_operator_sheet_title),
                    size = font_18,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimen_12))

                GlassInputField(
                    label = stringResource(R.string.crane_operator_search_label),
                    value = query,
                    onValueChange = {
                        query = it
                        onSearch(it)
                    },
                    placeholder = stringResource(R.string.crane_operator_search_hint),
                    leftIcon = Icons.Default.Search
                )
                Spacer(modifier = Modifier.height(dimen_12))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = dimen_320),
                    verticalArrangement = Arrangement.spacedBy(dimen_8)
                ) {
                    item {
                        OperatorRow(
                            title = stringResource(R.string.crane_operator_none),
                            subtitle = null,
                            source = null,
                            selected = selectedId == null,
                            onClick = { onSelect(null) }
                        )
                    }
                    items(operators, key = { it.id ?: it.member_id ?: it.hashCode() }) { operator ->
                        OperatorRow(
                            title = operator.full_name.orEmpty(),
                            subtitle = listOfNotNull(operator.member_id, operator.mobile_number)
                                .filter { it.isNotBlank() }.joinToString("  |  "),
                            source = operator.source,
                            selected = operator.id == selectedId,
                            onClick = { onSelect(operator) }
                        )
                    }
                    if (operators.isEmpty() && !isLoading) {
                        item {
                            Textview(
                                text = stringResource(R.string.crane_operators_empty),
                                size = font_12,
                                color = appColors.textSecondary,
                                modifier = Modifier.padding(vertical = dimen_8)
                            )
                        }
                    }
                    if (isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(dimen_8), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    color = AmberPrimary,
                                    strokeWidth = dimen_2,
                                    modifier = Modifier.size(dimen_20)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimen_12))
                CommonGradientButton(
                    text = stringResource(R.string.crane_operator_add_new),
                    gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                    textColor = Color.Black,
                    leftIcon = Icons.Default.Add,
                    onClick = { adding = true }
                )
                Spacer(modifier = Modifier.height(dimen_16))
            }
        }
    }
}

@Composable
private fun OperatorRow(
    title: String,
    subtitle: String?,
    source: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(dimen_12)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) AmberPrimary.copy(alpha = 0.12f) else appColors.subtleFill)
            .border(dimen_1, if (selected) AmberPrimary else appColors.subtleBorder, shape)
            .clickable { onClick() }
            .padding(dimen_10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimen_36)
                .clip(CircleShape)
                .background(AmberPrimary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            if (subtitle == null) {
                Icon(Icons.Default.Person, null, tint = AmberPrimary, modifier = Modifier.size(dimen_18))
            } else {
                Textview(
                    text = title.take(1).uppercase(),
                    size = font_14,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(dimen_10))
        Column(modifier = Modifier.weight(1f)) {
            Textview(
                text = title,
                size = font_14,
                color = appColors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            if (!subtitle.isNullOrBlank()) {
                Textview(text = subtitle, size = font_11, color = appColors.textSecondary, maxLines = 1)
            }
        }
        if (source != null) {
            StatusChip(
                text = stringResource(
                    if (source == "owner_added") R.string.crane_operator_owner_added else R.string.crane_operator_registered
                ),
                color = if (source == "owner_added") AmberPrimary else appColors.textSecondary
            )
            Spacer(modifier = Modifier.width(dimen_6))
        }
        if (selected) {
            Icon(Icons.Default.Check, null, tint = AmberPrimary, modifier = Modifier.size(dimen_20))
        }
    }
}

@Composable
private fun AddOperatorForm(
    isAdding: Boolean,
    onBack: () -> Unit,
    onAdd: (name: String, mobile: String, email: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    Textview(
        text = stringResource(R.string.crane_operator_add_title),
        size = font_18,
        color = appColors.textPrimary,
        fontWeight = FontWeight.Bold
    )
    Textview(
        text = stringResource(R.string.crane_operator_add_subtitle),
        size = font_12,
        color = appColors.textSecondary
    )
    Spacer(modifier = Modifier.height(dimen_14))

    Column(verticalArrangement = Arrangement.spacedBy(dimen_12)) {
        GlassInputField(
            label = stringResource(R.string.field_full_name),
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.field_full_name_hint),
            leftIcon = Icons.Default.Person,
            isRequired = true,
            capitalization = KeyboardCapitalization.Words
        )
        GlassInputField(
            label = stringResource(R.string.crane_operator_mobile),
            value = mobile,
            onValueChange = { mobile = it.filter { c -> c.isDigit() }.take(15) },
            placeholder = stringResource(R.string.crane_operator_mobile_hint),
            leftIcon = Icons.Default.Phone,
            isRequired = true,
            keyboardType = KeyboardType.Number
        )
        GlassInputField(
            label = stringResource(R.string.crane_operator_email),
            value = email,
            onValueChange = { email = it },
            placeholder = stringResource(R.string.crane_operator_email_hint),
            keyboardType = KeyboardType.Email
        )
    }

    Spacer(modifier = Modifier.height(dimen_16))
    CommonGradientButton(
        text = stringResource(R.string.crane_operator_save),
        gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
        textColor = Color.Black,
        enabled = !isAdding,
        onClick = { onAdd(name, mobile, email) }
    )
    Spacer(modifier = Modifier.height(dimen_8))
    CommonGradientButton(
        text = stringResource(R.string.common_back),
        gradientColors = listOf(appColors.subtleFill, appColors.subtleFill),
        textColor = appColors.textPrimary,
        onClick = onBack
    )
    Spacer(modifier = Modifier.height(dimen_16))
}
