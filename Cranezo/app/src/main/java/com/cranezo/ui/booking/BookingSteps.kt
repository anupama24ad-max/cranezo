package com.cranezo.ui.booking

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.booking.components.BottomButtons
import com.cranezo.ui.booking.components.SelectableCard
import com.cranezo.ui.booking.components.SelectedCheck
import com.cranezo.ui.booking.model.BookingChoice
import com.cranezo.ui.booking.model.BookingIntent
import com.cranezo.ui.booking.model.BookingOptions
import com.cranezo.ui.booking.model.BookingUiState
import com.cranezo.ui.booking.model.DurationTypeOption
import com.cranezo.ui.booking.model.StartTypeOption
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.GlassDateField
import com.cranezo.ui.components.GlassFieldBox
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.theme.*
import com.cranezo.utils.rupees
import java.util.Locale

private val previewOptions = BookingOptions(
    crane_types = listOf(
        BookingChoice(31, "Mobile Crane"), BookingChoice(32, "Tower Crane"),
        BookingChoice(33, "Crawler Crane"), BookingChoice(34, "Hydraulic Crane")
    ),
    capacities = listOf(
        BookingChoice(38, "20 Ton", "Suitable for small lifting tasks"),
        BookingChoice(39, "50 Ton", "Ideal for medium construction work")
    ),
    start_types = listOf(
        StartTypeOption("now", "Now", "Get a crane as soon as possible."),
        StartTypeOption("scheduled", "Schedule for later", "Pick a date and time that works for you.")
    ),
    duration_types = listOf(
        DurationTypeOption("hours", "Hours", "Select the number of hours (Minimum 4 hours)", min = 4, max = 12, default = 8),
        DurationTypeOption("full_day", "Full day", "Typically 8 hours per day", hours = 8),
        DurationTypeOption("days", "Multiple Days", "Select the number of days", min = 1, max = 30, default = 1)
    )
)

@Composable
private fun rememberState(vm: BookingViewModel?): BookingUiState =
    vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(options = previewOptions)

// ---------------------------------------------------------------------------------------------
// "Not sure?" row and sheet. There is no API for it yet.
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotSureRow() {
    var show by remember { mutableStateOf(false) }
    val context = LocalContext.current

    GlassCard(contentPadding = dimen_12, onClick = { show = true }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_20))
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(stringResource(R.string.bk_not_sure), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                Textview(stringResource(R.string.bk_not_sure_subtitle), size = font_11, color = appColors.textSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary)
        }
    }

    if (show) {
        val soon = { Toast.makeText(context, R.string.tab_coming_soon, Toast.LENGTH_SHORT).show() }
        ModalBottomSheet(
            onDismissRequest = { show = false },
            shape = RoundedCornerShape(topStart = dimen_28, topEnd = dimen_28),
            containerColor = appColors.screenBottom
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = dimen_20)
                    .padding(bottom = dimen_16),
                verticalArrangement = Arrangement.spacedBy(dimen_10)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.Help, accent = AmberPrimary, size = dimen_36, filled = true)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column {
                        Textview(stringResource(R.string.bk_sheet_title), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(stringResource(R.string.bk_sheet_subtitle), size = font_12, color = appColors.textSecondary)
                    }
                }
                SheetAction(Icons.Default.Chat, R.string.bk_sheet_chat, R.string.bk_sheet_chat_sub) { soon() }
                SheetAction(Icons.Default.Phone, R.string.bk_sheet_call, R.string.bk_sheet_call_sub) {
                    // No support number from the server yet: the dialer opens empty
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:")))
                }
                SheetAction(Icons.Default.ListAlt, R.string.bk_sheet_questions, R.string.bk_sheet_questions_sub) { soon() }
            }
        }
    }
}

@Composable
private fun SheetAction(icon: ImageVector, title: Int, subtitle: Int, onClick: () -> Unit) {
    GlassCard(contentPadding = dimen_12, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_20))
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(stringResource(title), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                Textview(stringResource(subtitle), size = font_11, color = appColors.textSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary)
        }
    }
}

@Composable
private fun StepTitle(title: Int, subtitle: Int) {
    Column(modifier = Modifier.padding(top = dimen_4)) {
        Textview(stringResource(title), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_4))
        Textview(stringResource(subtitle), size = font_13, color = appColors.textSecondary)
    }
}

// ---------------------------------------------------------------------------------------------
// Step 1: crane type
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun CraneTypeStep(vm: BookingViewModel? = null, onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    val state = rememberState(vm)
    LaunchedEffect(Unit) { if (state.options == null) vm?.onIntent(BookingIntent.LoadOptions) }

    BookingScaffold(
        onBack = onBack,
        step = 1,
        bottom = {
            BottomButtons(
                mainText = stringResource(R.string.common_continue),
                onMain = onNext,
                enabled = state.craneType != null
            )
        }
    ) {
        StepTitle(R.string.bk_type_title, R.string.bk_type_subtitle)
        state.options?.crane_types.orEmpty().chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                row.forEach { type ->
                    val selected = type.id == state.craneType
                    SelectableCard(
                        selected = selected,
                        onClick = { type.id?.let { vm?.onIntent(BookingIntent.SelectCraneType(it)) } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column {
                            CraneImage(
                                path = type.image,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimen_100),
                                shape = RoundedCornerShape(topStart = dimen_16, topEnd = dimen_16)
                            )
                            Textview(
                                text = type.name.orEmpty(),
                                size = font_13,
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.padding(dimen_10)
                            )
                        }
                        if (selected) SelectedCheck(Modifier.align(Alignment.TopEnd).padding(dimen_8))
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
        NotSureRow()
    }
}

// ---------------------------------------------------------------------------------------------
// Step 2: capacity
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun CapacityStep(vm: BookingViewModel? = null, onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    val state = rememberState(vm)

    BookingScaffold(
        onBack = onBack,
        step = 2,
        bottom = {
            BottomButtons(
                mainText = stringResource(R.string.common_continue),
                onMain = onNext,
                onBack = onBack,
                enabled = state.capacity != null
            )
        }
    ) {
        StepTitle(R.string.bk_capacity_title, R.string.bk_capacity_subtitle)
        state.options?.capacities.orEmpty().chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                row.forEach { capacity ->
                    val selected = capacity.id == state.capacity
                    SelectableCard(
                        selected = selected,
                        onClick = { capacity.id?.let { vm?.onIntent(BookingIntent.SelectCapacity(it)) } },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = dimen_100)
                    ) {
                        Column(modifier = Modifier.padding(dimen_14)) {
                            Textview(
                                text = capacity.name.orEmpty(),
                                size = font_18,
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(dimen_6))
                            Textview(text = capacity.description.orEmpty(), size = font_11, color = appColors.textSecondary)
                        }
                        if (selected) SelectedCheck(Modifier.align(Alignment.TopEnd).padding(dimen_8))
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
        NotSureRow()
    }
}

// ---------------------------------------------------------------------------------------------
// Step 3: location (phone GPS, or a typed address)
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun LocationStep(vm: BookingViewModel? = null, onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    val state = rememberState(vm)
    var query by remember { mutableStateOf("") }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm?.onIntent(BookingIntent.LocateMe)
    }

    BookingScaffold(
        onBack = onBack,
        step = 3,
        bottom = {
            BottomButtons(
                mainText = stringResource(R.string.common_continue),
                onMain = onNext,
                onBack = onBack,
                enabled = state.latitude != null && state.siteAddress.isNotBlank()
            )
        }
    ) {
        StepTitle(R.string.bk_location_title, R.string.bk_location_subtitle)

        GlassInputField(
            label = stringResource(R.string.bk_search_location),
            value = query,
            onValueChange = { query = it },
            placeholder = stringResource(R.string.bk_search_location),
            leftIcon = Icons.Default.Search,
            rightContent = {
                if (state.isLocating) {
                    CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_2, modifier = Modifier.size(dimen_18))
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = AmberPrimary,
                        modifier = Modifier
                            .size(dimen_22)
                            .clickable { vm?.onIntent(BookingIntent.SearchAddress(query)) }
                    )
                }
            }
        )

        GlassCard(
            contentPadding = dimen_12,
            onClick = { permission.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.MyLocation, accent = AmberPrimary, size = dimen_36)
                Spacer(modifier = Modifier.width(dimen_12))
                Textview(
                    text = stringResource(R.string.bk_use_current_location),
                    size = font_14,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        GlassInputField(
            label = stringResource(R.string.bk_site_name),
            value = state.siteName,
            onValueChange = { vm?.onIntent(BookingIntent.SiteNameChanged(it)) },
            placeholder = stringResource(R.string.bk_site_name_hint),
            leftIcon = Icons.Default.LocationOn
        )
        GlassInputField(
            label = stringResource(R.string.bk_site_address),
            value = state.siteAddress,
            onValueChange = { vm?.onIntent(BookingIntent.AddressChanged(it)) },
            placeholder = stringResource(R.string.bk_site_address_hint),
            singleLine = false,
            minHeight = dimen_80
        )
        if (state.latitude != null) {
            Textview(
                text = String.format(Locale.US, "%.5f, %.5f", state.latitude, state.longitude ?: 0.0),
                size = font_11,
                color = appColors.textHint
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Step 4: date and time
// ---------------------------------------------------------------------------------------------

@Composable
private fun StartOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    SelectableCard(selected = selected, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(dimen_14), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, accent = AmberPrimary, size = dimen_36)
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(title, size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Textview(subtitle, size = font_11, color = appColors.textSecondary)
            }
            RadioDot(selected)
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(dimen_20)
            .clip(CircleShape)
            .background(if (selected) AmberPrimary.copy(alpha = 0.25f) else Color.Transparent)
            .glassEffect(CircleShape, borderColor = if (selected) AmberPrimary else Color.Unspecified),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Box(modifier = Modifier.size(dimen_10).clip(CircleShape).background(AmberPrimary))
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun DateTimeStep(vm: BookingViewModel? = null, onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    val state = rememberState(vm)
    val context = LocalContext.current
    val scheduled = state.startType == "scheduled"

    BookingScaffold(
        onBack = onBack,
        step = 4,
        bottom = {
            BottomButtons(
                mainText = stringResource(R.string.common_continue),
                onMain = onNext,
                onBack = onBack,
                enabled = !scheduled || (state.date.isNotEmpty() && state.time.isNotEmpty())
            )
        }
    ) {
        StepTitle(R.string.bk_when_title, R.string.bk_when_subtitle)

        state.options?.start_types.orEmpty().forEach { type ->
            StartOptionCard(
                icon = if (type.id == "now") Icons.Default.Bolt else Icons.Default.CalendarMonth,
                title = type.name.orEmpty(),
                subtitle = type.description.orEmpty(),
                selected = type.id == state.startType,
                onClick = { type.id?.let { vm?.onIntent(BookingIntent.StartTypeChanged(it)) } }
            )
        }

        if (scheduled) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                GlassDateField(
                    label = stringResource(R.string.bk_date),
                    value = state.date,
                    placeholder = stringResource(R.string.bk_select_date),
                    onDateSelected = { vm?.onIntent(BookingIntent.DateChanged(it)) },
                    modifier = Modifier.weight(1f),
                    allowPastDates = false
                )
                Column(modifier = Modifier.weight(1f)) {
                    Textview(
                        stringResource(R.string.bk_time),
                        size = font_14,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = dimen_8)
                    )
                    GlassFieldBox(onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                vm?.onIntent(BookingIntent.TimeChanged(String.format(Locale.US, "%02d:%02d", hour, minute)))
                            },
                            9, 0, false
                        ).show()
                    }) {
                        Textview(
                            text = state.time.ifEmpty { stringResource(R.string.bk_select_time) },
                            size = font_14,
                            color = if (state.time.isEmpty()) appColors.textHint else appColors.textPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_20))
                    }
                }
            }
            Textview(stringResource(R.string.bk_schedule_rule), size = font_11, color = appColors.textHint)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Step 5: duration
// ---------------------------------------------------------------------------------------------

@Composable
private fun Stepper(value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .glassEffect(RoundedCornerShape(dimen_20))
            .padding(horizontal = dimen_8, vertical = dimen_4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Remove, contentDescription = null, tint = appColors.textPrimary,
            modifier = Modifier.size(dimen_20).clickable(enabled = value > min) { onChange(value - 1) }
        )
        Textview(
            text = value.toString(),
            size = font_14,
            color = appColors.textPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(dimen_32),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Icon(
            Icons.Default.Add, contentDescription = null, tint = appColors.textPrimary,
            modifier = Modifier.size(dimen_20).clickable(enabled = value < max) { onChange(value + 1) }
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun DurationStep(vm: BookingViewModel? = null, onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    val state = rememberState(vm)

    // The estimate follows the choice: a silent search shows the price of the cheapest crane
    LaunchedEffect(state.durationType, state.hours, state.days) {
        vm?.onIntent(BookingIntent.EstimatePrice)
    }

    BookingScaffold(
        onBack = onBack,
        step = 5,
        bottom = {
            BottomButtons(
                mainText = stringResource(R.string.bk_find_cranes),
                onMain = onNext,
                onBack = onBack
            )
        }
    ) {
        StepTitle(R.string.bk_duration_title, R.string.bk_duration_subtitle)

        state.options?.duration_types.orEmpty().forEach { type ->
            val selected = type.id == state.durationType
            SelectableCard(
                selected = selected,
                onClick = { type.id?.let { vm?.onIntent(BookingIntent.DurationTypeChanged(it)) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(dimen_14), verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(
                        icon = if (type.id == "hours") Icons.Default.AccessTime else Icons.Default.Today,
                        accent = AmberPrimary,
                        size = dimen_36
                    )
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(type.name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(type.description.orEmpty(), size = font_11, color = appColors.textSecondary)
                    }
                    when (type.id) {
                        "hours" -> Stepper(state.hours, type.min ?: 4, type.max ?: 12) { vm?.onIntent(BookingIntent.HoursChanged(it)) }
                        "days" -> Stepper(state.days, type.min ?: 1, type.max ?: 30) { vm?.onIntent(BookingIntent.DaysChanged(it)) }
                        else -> RadioDot(selected)
                    }
                }
            }
        }

        // Only when a crane was found: the server calculated this price
        state.estimate?.price?.let { price ->
            GlassCard(contentPadding = dimen_14) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.CurrencyRupee, accent = AmberPrimary, size = dimen_36, filled = true)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.bk_estimated_price), size = font_11, color = appColors.textSecondary)
                        Textview(
                            text = stringResource(R.string.bk_from_price, rupees(price.total_amount)),
                            size = font_20,
                            color = AmberPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Textview(
                            text = stringResource(R.string.bk_per_hour, rupees(price.hourly_rate)),
                            size = font_11,
                            color = appColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
