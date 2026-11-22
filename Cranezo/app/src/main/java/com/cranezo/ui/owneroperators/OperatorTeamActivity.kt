package com.cranezo.ui.owneroperators

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.Textview
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.myprofile.components.MyProfileScaffold
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatAmount
import dagger.hilt.android.AndroidEntryPoint

/**
 * My Operators (owner): the team, how each one is paid, requests sent to registered operators, adding a new operator.
 * Opened from My Profile.
 */
@AndroidEntryPoint
class OperatorTeamActivity : BaseVMComposeActivity<OperatorTeamViewModel>(OperatorTeamViewModel::class.java) {

    @Composable
    override fun Content(vm: OperatorTeamViewModel) {
        val state by vm.state.collectAsStateWithLifecycle()
        // list, new, find or edit
        var screen by rememberSaveable { mutableStateOf("list") }
        var editing by remember { mutableStateOf<TeamOperator?>(null) }
        LaunchedEffect(Unit) { vm.load() }
        // an add, a request or a pay change went through: back to the list
        LaunchedEffect(state.doneTick) {
            if (state.doneTick > 0) {
                screen = "list"
                editing = null
            }
        }
        BackHandler(enabled = screen != "list") {
            screen = "list"
            editing = null
            vm.clearFound()
        }
        val back: () -> Unit = {
            if (screen != "list") {
                screen = "list"
                editing = null
                vm.clearFound()
            } else finish()
        }
        when {
            screen == "new" -> NewOperatorScreen(state.busy, back) { n, m, e, t -> vm.addNew(n, m, e, t) }
            screen == "find" -> FindOperatorScreen(state, back, onSearch = { vm.search(it) }) { id, t, msg -> vm.sendRequest(id, t, msg) }
            screen == "edit" && editing != null -> EditTermsScreen(editing!!, state.busy, back) { t -> vm.saveTerms(editing!!.id.orEmpty(), t) }
            else -> TeamListScreen(
                state = state, onBack = back,
                onNew = { screen = "new" }, onFind = { screen = "find" },
                onEdit = { editing = it; screen = "edit" },
                onCancel = { vm.cancelRequest(it) }
            )
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, OperatorTeamActivity::class.java)
    }
}

// ---------------------------------------------------------------------------------------------
// Pay terms: how the owner pays an operator
// ---------------------------------------------------------------------------------------------

/** "Rs 25,000 / month", "Rs 900 / day" or "Per job", with "+ Rs 200 batta" when there is a daily allowance */
@Composable
fun termsText(type: String?, monthly: Double?, daily: Double?, batta: Double?): String {
    val base = when (type) {
        "daily" -> "₹${formatAmount(daily ?: 0.0)} ${stringResource(R.string.ot_per_day)}"
        "per_job" -> stringResource(R.string.ot_by_job)
        else -> if ((monthly ?: 0.0) > 0) "₹${formatAmount(monthly)} ${stringResource(R.string.ot_per_month)}" else stringResource(R.string.ot_by_job)
    }
    return if ((batta ?: 0.0) > 0 && type != "per_job") "$base + ₹${formatAmount(batta)} ${stringResource(R.string.ot_batta_word)}" else base
}

/** The three ways to pay, as tap-to-choose labels (no dropdown), and the amount boxes that go with the choice */
@Composable
private fun PayTermsForm(initial: PayTerms, onChange: (PayTerms) -> Unit) {
    var type by rememberSaveable { mutableStateOf(initial.salary_type) }
    var monthly by rememberSaveable { mutableStateOf(initial.monthly_salary?.takeIf { it > 0 }?.let { formatPlain(it) }.orEmpty()) }
    var daily by rememberSaveable { mutableStateOf(initial.daily_wage?.takeIf { it > 0 }?.let { formatPlain(it) }.orEmpty()) }
    var batta by rememberSaveable { mutableStateOf(initial.batta?.takeIf { it > 0 }?.let { formatPlain(it) }.orEmpty()) }
    fun push() = onChange(PayTerms(type, monthly.toDoubleOrNull(), daily.toDoubleOrNull(), batta.toDoubleOrNull()))
    LaunchedEffect(type, monthly, daily, batta) { push() }

    Textview(stringResource(R.string.ot_pay_title), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(dimen_8))
    Row(horizontalArrangement = Arrangement.spacedBy(dimen_8), modifier = Modifier.fillMaxWidth()) {
        listOf("monthly" to R.string.ot_pay_monthly, "daily" to R.string.ot_pay_daily, "per_job" to R.string.ot_pay_job).forEach { (key, label) ->
            val on = type == key
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(dimen_12))
                    .background(if (on) AmberPrimary.copy(alpha = 0.10f) else appColors.subtleFill)
                    .border(dimen_1, if (on) AmberPrimary.copy(alpha = 0.7f) else Color.Transparent, RoundedCornerShape(dimen_12))
                    .clickable { type = key }.padding(vertical = dimen_10, horizontal = dimen_4),
                contentAlignment = Alignment.Center
            ) {
                Textview(stringResource(label), size = font_12, color = if (on) appColors.primaryText else appColors.textPrimary, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
            }
        }
    }
    Spacer(modifier = Modifier.height(dimen_10))
    when (type) {
        "monthly" -> GlassInputField(
            label = stringResource(R.string.ot_monthly_label), value = monthly,
            onValueChange = { monthly = it.filter { c -> c.isDigit() || c == '.' }.take(9) }, placeholder = "25000",
            keyboardType = KeyboardType.Decimal, groupDigits = true, isRequired = true
        )
        "daily" -> GlassInputField(
            label = stringResource(R.string.ot_daily_label), value = daily,
            onValueChange = { daily = it.filter { c -> c.isDigit() || c == '.' }.take(9) }, placeholder = "900",
            keyboardType = KeyboardType.Decimal, groupDigits = true, isRequired = true
        )
        else -> Textview(stringResource(R.string.ot_pay_job_note), size = font_12, color = appColors.textSecondary)
    }
    if (type != "per_job") {
        Spacer(modifier = Modifier.height(dimen_10))
        GlassInputField(
            label = stringResource(R.string.ot_batta_label), value = batta,
            onValueChange = { batta = it.filter { c -> c.isDigit() || c == '.' }.take(9) }, placeholder = "200",
            keyboardType = KeyboardType.Decimal, groupDigits = true
        )
    }
}

private fun formatPlain(v: Double): String = if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()

// ---------------------------------------------------------------------------------------------
// The team and the requests
// ---------------------------------------------------------------------------------------------

@Composable
private fun TeamListScreen(
    state: OperatorTeamState, onBack: () -> Unit, onNew: () -> Unit, onFind: () -> Unit,
    onEdit: (TeamOperator) -> Unit, onCancel: (String) -> Unit
) {
    MyProfileScaffold(
        title = stringResource(R.string.ot_title), subtitle = stringResource(R.string.ot_subtitle), onBack = onBack,
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                CommonGradientButton(
                    text = stringResource(R.string.ot_find), modifier = Modifier.weight(1f), onClick = onFind,
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                )
                CommonGradientButton(text = stringResource(R.string.ot_add_new), modifier = Modifier.weight(1f), onClick = onNew)
            }
        }
    ) {
        // requests that are still waiting for the operator's answer, and the last few answers
        val requests = state.sent.filter { it.status == 0 } + state.sent.filter { it.status == 2 }.take(3)
        if (requests.isNotEmpty()) {
            Textview(stringResource(R.string.ot_requests), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            requests.forEach { r ->
                GlassCard(contentPadding = dimen_12) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Textview(r.operator?.full_name.orEmpty().ifBlank { r.operator?.member_id.orEmpty() }, size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                            Textview(r.operator?.member_id.orEmpty(), size = font_11, color = appColors.textSecondary)
                        }
                        BookingTag(
                            r.status_label.orEmpty(),
                            if (r.status == 2) Red else AmberPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(dimen_6))
                    Textview(termsText(r.salary_type, r.monthly_salary, r.daily_wage, r.batta), size = font_12, color = appColors.textSecondary)
                    if (r.status == 0) {
                        Spacer(modifier = Modifier.height(dimen_6))
                        Textview(
                            stringResource(R.string.ot_cancel), size = font_12, color = Red, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { r.id?.let(onCancel) }.padding(vertical = dimen_4)
                        )
                    }
                }
            }
        }

        Textview(stringResource(R.string.ot_team), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        if (state.loaded && state.team.isEmpty()) {
            Textview(stringResource(R.string.ot_empty), size = font_13, color = appColors.textSecondary)
        }
        state.team.forEach { op ->
            GlassCard(contentPadding = dimen_12, onClick = { onEdit(op) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(dimen_40).clip(CircleShape).background(AmberPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) { Textview(op.full_name.orEmpty().take(1).uppercase(), size = font_16, color = AmberPrimary, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(op.full_name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                        Textview("${op.member_id.orEmpty()} · ${op.country_code.orEmpty()} ${op.mobile_number.orEmpty()}".trim(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                    }
                    Box(modifier = Modifier.size(dimen_10).clip(CircleShape).background(if (op.is_online == true) SuccessGreen else appColors.textHint))
                }
                Spacer(modifier = Modifier.height(dimen_8))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(termsText(op.salary_type, op.monthly_salary, op.daily_wage, op.batta), size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Textview(stringResource(R.string.ot_edit_pay), size = font_12, color = AmberPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// Add a new operator the owner types in
// ---------------------------------------------------------------------------------------------

@Composable
private fun NewOperatorScreen(busy: Boolean, onBack: () -> Unit, onAdd: (String, String, String, PayTerms) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var terms by remember { mutableStateOf(PayTerms()) }
    MyProfileScaffold(
        title = stringResource(R.string.ot_new_title), subtitle = stringResource(R.string.ot_new_sub), onBack = onBack,
        bottom = {
            CommonGradientButton(
                text = stringResource(R.string.ot_add_new), enabled = !busy && name.isNotBlank() && mobile.length >= 6,
                onClick = { onAdd(name, mobile, email, terms) }
            )
        }
    ) {
        GlassCard(contentPadding = dimen_14) {
            GlassInputField(label = stringResource(R.string.ot_name), value = name, onValueChange = { name = it }, placeholder = stringResource(R.string.ot_name), isRequired = true)
            Spacer(modifier = Modifier.height(dimen_12))
            GlassInputField(
                label = stringResource(R.string.ot_mobile), value = mobile, onValueChange = { mobile = it.filter { c -> c.isDigit() }.take(15) },
                placeholder = "9876543210", keyboardType = KeyboardType.Number, isRequired = true
            )
            Spacer(modifier = Modifier.height(dimen_12))
            GlassInputField(label = stringResource(R.string.ot_email), value = email, onValueChange = { email = it }, placeholder = "name@example.com", keyboardType = KeyboardType.Email)
        }
        GlassCard(contentPadding = dimen_14) { PayTermsForm(PayTerms(), onChange = { terms = it }) }
    }
}

// ---------------------------------------------------------------------------------------------
// Find a registered operator and send a request
// ---------------------------------------------------------------------------------------------

@Composable
private fun FindOperatorScreen(
    state: OperatorTeamState, onBack: () -> Unit, onSearch: (String) -> Unit,
    onSend: (String, PayTerms, String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var terms by remember { mutableStateOf(PayTerms()) }
    val found = state.found
    val canAsk = found != null && found.status == "free"
    MyProfileScaffold(
        title = stringResource(R.string.ot_find_title), subtitle = stringResource(R.string.ot_find_sub), onBack = onBack,
        bottom = {
            if (canAsk) CommonGradientButton(
                text = stringResource(R.string.ot_send_request), enabled = !state.busy,
                onClick = { onSend(found?.id.orEmpty(), terms, message) }
            )
        }
    ) {
        GlassCard(contentPadding = dimen_14) {
            GlassInputField(
                label = stringResource(R.string.ot_query_label), value = query, onValueChange = { query = it.take(20) },
                placeholder = stringResource(R.string.ot_query_hint)
            )
            Spacer(modifier = Modifier.height(dimen_10))
            CommonGradientButton(text = stringResource(R.string.ot_search), enabled = !state.busy && query.trim().length >= 4, onClick = { onSearch(query) })
        }
        if (found != null) {
            GlassCard(contentPadding = dimen_14, borderColor = if (canAsk) AmberPrimary.copy(alpha = 0.55f) else Color.Unspecified) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(dimen_44).clip(CircleShape).background(AmberPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) { Textview(found.full_name.orEmpty().take(1).uppercase(), size = font_18, color = AmberPrimary, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column {
                        Textview(found.full_name.orEmpty(), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(found.member_id.orEmpty(), size = font_12, color = appColors.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(dimen_8))
                val note = when (found.status) {
                    "yours" -> R.string.ot_st_yours
                    "other_owner" -> R.string.ot_st_other
                    "requested" -> R.string.ot_st_requested
                    "blocked" -> R.string.ot_st_blocked
                    else -> null
                }
                if (note != null) Textview(stringResource(note), size = font_13, color = Red, fontWeight = FontWeight.SemiBold)
            }
            if (canAsk) {
                GlassCard(contentPadding = dimen_14) { PayTermsForm(PayTerms(), onChange = { terms = it }) }
                GlassCard(contentPadding = dimen_14) {
                    GlassInputField(
                        label = stringResource(R.string.ot_message), value = message, onValueChange = { message = it.take(300) },
                        placeholder = stringResource(R.string.ot_message)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Change how a team member is paid
// ---------------------------------------------------------------------------------------------

@Composable
private fun EditTermsScreen(op: TeamOperator, busy: Boolean, onBack: () -> Unit, onSave: (PayTerms) -> Unit) {
    var terms by remember(op.id) { mutableStateOf(PayTerms(op.salary_type ?: "monthly", op.monthly_salary, op.daily_wage, op.batta)) }
    MyProfileScaffold(
        title = op.full_name.orEmpty(), subtitle = op.member_id, onBack = onBack,
        bottom = { CommonGradientButton(text = stringResource(R.string.ot_save), enabled = !busy, onClick = { onSave(terms) }) }
    ) {
        GlassCard(contentPadding = dimen_14) {
            PayTermsForm(PayTerms(op.salary_type ?: "monthly", op.monthly_salary, op.daily_wage, op.batta), onChange = { terms = it })
        }
    }
}
