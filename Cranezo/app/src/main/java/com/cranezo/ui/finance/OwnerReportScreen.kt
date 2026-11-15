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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDate

/**
 * Owner: detailed report for a period. What the customers paid, the platform fee, GST, what was paid to each operator
 * (and what is still to pay), and the owner's own share, then one line per job.
 */
@Composable
fun OwnerReportScreen(vm: FinanceViewModel, onBack: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    var period by rememberSaveable { mutableStateOf("all") }
    androidx.compose.runtime.LaunchedEffect(period) { vm.loadReport(period) }
    val r = state.report

    FinPage(stringResource(R.string.rep_title), onBack, subtitle = stringResource(R.string.rep_sub)) {
        // period: today, 6 months, 1 year or overall
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            listOf("today" to R.string.op_period_today, "6m" to R.string.op_period_6m, "1y" to R.string.op_period_1y, "all" to R.string.op_period_all).forEach { (key, label) ->
                val selected = period == key
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(50))
                        .background(if (selected) badgeFill(AmberPrimary) else appColors.subtleFill)
                        .clickable { period = key }
                        .padding(vertical = dimen_8),
                    contentAlignment = Alignment.Center
                ) {
                    Textview(stringResource(label), size = font_12, color = if (selected) Color.White else appColors.textSecondary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }

        FinSection(stringResource(R.string.rep_money)) {
            Textview(stringResource(R.string.op_n_jobs, r?.jobs ?: 0), size = font_12, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_6))
            MoneyRow(stringResource(R.string.rep_customers_paid), r?.customer_paid)
            MoneyRow(stringResource(R.string.rep_platform_fee, trimNumber(r?.platform_fee_percent ?: 0.0)), -(r?.platform_fee ?: 0.0))
            if ((r?.other_deductions ?: 0.0) > 0) MoneyRow(stringResource(R.string.rep_other_deductions), -(r?.other_deductions ?: 0.0))
            if ((r?.gst ?: 0.0) > 0) MoneyRow(stringResource(R.string.rep_gst), -(r?.gst ?: 0.0))
            MoneyRow(stringResource(R.string.rep_operators_total), -(r?.operator_commission?.total ?: 0.0))
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6).height(dimen_1).background(appColors.subtleBorder))
            MoneyRow(stringResource(R.string.rep_your_share), r?.your_share, bold = true, color = FinGreen)
        }

        // operator payments: paid, and still to pay (yellow)
        FinSection(stringResource(R.string.rep_operator_payments)) {
            MoneyRow(stringResource(R.string.rep_paid), r?.operator_commission?.paid, color = FinGreen)
            MoneyRow(stringResource(R.string.rep_to_pay), r?.operator_commission?.pending, color = AmberPrimary)
            val ops = r?.operators.orEmpty()
            if ("report" in state.loaded && ops.isEmpty()) EmptyText(stringResource(R.string.rep_no_operators))
            ops.forEach { o ->
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6).height(dimen_1).background(appColors.subtleBorder))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(o.operator?.name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Textview(stringResource(R.string.op_n_jobs, o.jobs ?: 0), size = font_11, color = appColors.textSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Textview(amountText(o.commission), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        if ((o.pending ?: 0.0) > 0) {
                            Textview(stringResource(R.string.rep_x_to_pay, amountText(o.pending)), size = font_11, color = AmberPrimary, fontWeight = FontWeight.SemiBold)
                        } else {
                            Textview(stringResource(R.string.rep_all_paid), size = font_11, color = FinGreen)
                        }
                    }
                }
            }
        }

        // one line per job
        FinSection(stringResource(R.string.rep_jobs)) {
            val jobs = r?.jobs_list.orEmpty()
            if ("report" in state.loaded && jobs.isEmpty()) EmptyText(stringResource(R.string.rep_no_jobs))
            jobs.forEachIndexed { i, j ->
                if (i > 0) Box(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6).height(dimen_1).background(appColors.subtleBorder))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(j.booking_code.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        Textview(formatServerDate(j.completed_at), size = font_11, color = appColors.textSecondary)
                        Textview(
                            stringResource(R.string.rep_job_split, amountText(j.platform_fee), amountText(j.operator_commission)),
                            size = font_11, color = if (j.operator_paid == true) appColors.textSecondary else AmberPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Textview(amountText(j.customer_paid), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(stringResource(R.string.rep_your_share_x, amountText(j.your_share)), size = font_11, color = FinGreen)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(dimen_1))
        Spacer(modifier = Modifier.height(dimen_16))
    }
}
