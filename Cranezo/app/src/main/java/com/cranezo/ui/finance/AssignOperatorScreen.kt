package com.cranezo.ui.finance

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.Textview
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.theme.*
import com.cranezo.utils.rupees

/**
 * Assign an operator in ONE screen: who does the job, and what they earn for it. One button does both
 * (the operator is told at once, with the amount). Then a short confirmation page.
 * Opened from the job and from the "Assign an Operator" notice that comes when the customer has paid the advance.
 */
@Composable
fun AssignOperatorScreen(vm: FinanceViewModel, bookingId: String, onBack: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(bookingId) {
        vm.loadJobMoney(bookingId)
        vm.loadOperators()
    }
    val job = state.jobMoney?.takeIf { it.booking_id == bookingId }
    val commission = job?.operator_commission

    // the choice: one of the owner's operators (by id), or any operator by their ID code
    var selected by rememberSaveable { mutableStateOf("") }
    var selectedName by rememberSaveable { mutableStateOf("") }
    var otherCode by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("percent") }
    var value by rememberSaveable { mutableStateOf("") }
    var filled by rememberSaveable { mutableStateOf(false) }
    var done by rememberSaveable { mutableStateOf(false) }
    var doneName by rememberSaveable { mutableStateOf("") }
    var doneAmount by rememberSaveable { mutableStateOf(0.0) }

    // start from what is already known: the crane's usual operator (or the one already chosen) and an earlier commission
    LaunchedEffect(job) {
        if (job != null && !filled) {
            filled = true
            val current = job.assigned_operator ?: job.suggested_operator?.takeIf { it.is_online == true }?.let { com.cranezo.ui.finance.model.AssignedOperatorRef(it.id, it.full_name) }
            if (selected.isEmpty() && current?.id != null) {
                selected = current.id
                selectedName = current.full_name.orEmpty()
            }
            if (commission?.isSet == true && commission.type != null && commission.type != "per_km") {
                type = commission.type
                value = commission.value?.let { trimNumber(it) }.orEmpty()
            }
        }
    }

    val entered = value.toDoubleOrNull()
    val earns = when {
        entered == null -> null
        type == "percent" -> commission?.job_value?.let { Math.round(it * entered) / 100.0 }
        else -> entered
    }
    val byCode = selected.isEmpty()
    val operatorPicked = selected.isNotEmpty() || otherCode.length >= 4
    val valueOk = entered != null && entered > 0 && (type != "percent" || entered <= 100)
    val canAssign = operatorPicked && valueOk && !state.saving

    if (done) {
        FinPage(stringResource(R.string.as_done_title), onBack, subtitle = job?.booking_code) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_24), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FinGreen, modifier = Modifier.size(dimen_70))
                Spacer(modifier = Modifier.height(dimen_12))
                Textview(stringResource(R.string.as_done_title), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_8))
                Textview(
                    stringResource(R.string.as_done_body, doneName, rupees(doneAmount)),
                    size = font_14, color = appColors.textSecondary, textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(dimen_16))
            CommonGradientButton(text = stringResource(R.string.as_done_back), onClick = onBack)
        }
        return
    }

    FinPage(stringResource(R.string.as_title), onBack, subtitle = job?.booking_code) {
        if (job == null) {
            if ("job_money" in state.loaded) EmptyText(stringResource(R.string.fin_not_found))
            return@FinPage
        }

        // 1. who does the job
        FinSection(stringResource(R.string.as_choose)) {
            val suggestedId = job.suggested_operator?.id
            // (id, name, code) to usual?, online?  -- operators who are online come first
            val list = buildList {
                job.suggested_operator?.let { s -> s.id?.let { add(Triple(it, s.full_name.orEmpty(), s.member_id.orEmpty()) to Pair(true, s.is_online == true)) } }
                state.operators.filter { it.id != suggestedId }.forEach { o -> o.id?.let { add(Triple(it, o.full_name.orEmpty(), o.member_id.orEmpty()) to Pair(false, o.is_online == true)) } }
            }.sortedByDescending { it.second.second }
            if (list.isNotEmpty() && list.none { it.second.second }) {
                Textview(stringResource(R.string.as_all_offline), size = font_12, color = appColors.textSecondary)
            }
            if ("operators" in state.loaded && list.isEmpty()) {
                Textview(stringResource(R.string.as_no_operators), size = font_12, color = appColors.textSecondary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                list.forEach { (o, flags) ->
                    val (usual, online) = flags
                    val on = selected == o.first && online
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_12))
                            .background(if (on) AmberPrimary.copy(alpha = 0.18f) else appColors.subtleFill)
                            .then(if (online) Modifier.clickable {
                                selected = o.first
                                selectedName = o.second
                                otherCode = ""
                            } else Modifier)
                            .padding(dimen_10),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(icon = Icons.Default.Person, accent = if (online) AmberPrimary else appColors.textHint, size = dimen_36)
                        Spacer(modifier = Modifier.width(dimen_10))
                        Column(modifier = Modifier.weight(1f).then(if (online) Modifier else Modifier.alpha(0.55f))) {
                            Textview(o.second, size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                            Textview(o.third, size = font_11, color = appColors.textSecondary)
                        }
                        if (!online) BookingTag(stringResource(R.string.as_offline), Color(0xFF9CA3AF))
                        else if (usual) BookingTag(stringResource(R.string.rq_usual_operator), SuccessGreen)
                        Spacer(modifier = Modifier.width(dimen_8))
                        // the round choice mark: a ring, with a dot inside when chosen
                        if (online) Box(modifier = Modifier.size(dimen_20).clip(CircleShape).background(if (on) AmberPrimary else appColors.subtleBorder), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(dimen_10).clip(CircleShape).background(if (on) Color.White else Color.Transparent))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(stringResource(R.string.as_other), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
            GlassInputField(
                label = "", value = otherCode,
                onValueChange = {
                    otherCode = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(14)
                    // typing an ID replaces the choice from the list
                    if (otherCode.isNotEmpty()) {
                        selected = ""
                        selectedName = ""
                    }
                },
                placeholder = "OP497309"
            )
            Textview(stringResource(R.string.as_other_hint), size = font_11, color = appColors.textSecondary)
        }

        // 2. what the operator earns for this job
        FinSection(stringResource(R.string.as_earn)) {
            ChoiceChips(commissionOptions(commission?.types), type) { type = it }
            Spacer(modifier = Modifier.height(dimen_8))
            GlassInputField(
                label = commissionValueLabel(type), value = value,
                onValueChange = { value = it.filter { ch -> ch.isDigit() || ch == '.' } },
                placeholder = "0", keyboardType = KeyboardType.Decimal
            )
            if (type == "percent" && commission?.job_value != null) {
                Spacer(modifier = Modifier.height(dimen_6))
                TextRow(stringResource(R.string.cm_job_value), amountText(commission.job_value))
            }
            // one-tap usual values, so most owners never type
            if (type == "percent") {
                Row(modifier = Modifier.padding(top = dimen_8), horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
                    listOf("8", "10", "12").forEach { q ->
                        val on = value == q
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(dimen_20))
                                .background(if (on) AmberPrimary.copy(alpha = 0.22f) else appColors.subtleFill)
                                .clickable { value = q }
                                .padding(horizontal = dimen_16, vertical = dimen_8),
                            contentAlignment = Alignment.Center
                        ) { Textview("$q%", size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            // updates while typing
            MoneyRow(stringResource(R.string.cm_operator_will_get), earns, bold = true, color = FinGreen)
            Textview(stringResource(R.string.as_change_later), size = font_11, color = appColors.textHint, modifier = Modifier.padding(top = dimen_4))
        }

        // one button does both
        CommonGradientButton(
            text = if (state.saving) stringResource(R.string.fin_saving)
            else if (!operatorPicked) stringResource(R.string.as_pick_first)
            else stringResource(R.string.as_assign_btn),
            enabled = canAssign,
            onClick = {
                val id = if (byCode) otherCode else selected
                val name = if (byCode) otherCode else selectedName
                vm.assignOperator(bookingId, id, byCode, type, value) {
                    doneName = name
                    doneAmount = earns ?: 0.0
                    done = true
                }
            }
        )
        Spacer(modifier = Modifier.height(dimen_16))
    }
}
