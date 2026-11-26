package com.cranezo.ui.owneroperators

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.cranezo.R
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.myprofile.components.MyProfileScaffold
import com.cranezo.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * Owner requests (operator): owners asking this operator to join their team, with the pay they offer.
 * Accepting puts the operator under that owner; they then see the owner on their profile.
 */
@AndroidEntryPoint
class OperatorInvitesActivity : BaseVMComposeActivity<OperatorTeamViewModel>(OperatorTeamViewModel::class.java) {

    @Composable
    override fun Content(vm: OperatorTeamViewModel) {
        val state by vm.state.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { vm.loadReceived() }

        MyProfileScaffold(
            title = stringResource(R.string.ot_invites_title), subtitle = stringResource(R.string.ot_invites_sub), onBack = { finish() }
        ) {
            if (state.loaded && state.received.isEmpty()) {
                Textview(stringResource(R.string.ot_no_invites), size = font_14, color = appColors.textSecondary)
            }
            state.received.forEach { r ->
                val waiting = r.status == 0
                GlassCard(contentPadding = dimen_14, borderColor = if (waiting) AmberPrimary.copy(alpha = 0.55f) else androidx.compose.ui.graphics.Color.Unspecified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Textview(
                                r.owner?.company_name.orEmpty().ifBlank { r.owner?.full_name.orEmpty() },
                                size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1
                            )
                            Textview("${stringResource(R.string.ot_owner)} · ${r.owner?.member_id.orEmpty()}", size = font_11, color = appColors.textSecondary)
                        }
                        if (!waiting) BookingTag(r.status_label.orEmpty(), if (r.status == 1) com.cranezo.ui.mainhome.components.SuccessGreen else appColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(dimen_8))
                    Textview(termsText(r.salary_type, r.monthly_salary, r.daily_wage, r.batta), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.SemiBold)
                    if (!r.message.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(dimen_4))
                        Textview(r.message.orEmpty(), size = font_12, color = appColors.textSecondary)
                    }
                    if (waiting) {
                        Spacer(modifier = Modifier.height(dimen_10))
                        Row(horizontalArrangement = Arrangement.spacedBy(dimen_12), modifier = Modifier.fillMaxWidth()) {
                            CommonGradientButton(
                                text = stringResource(R.string.ot_decline), modifier = Modifier.weight(1f), enabled = !state.busy,
                                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary, height = dimen_48,
                                onClick = { r.id?.let { vm.respond(it, false) } }
                            )
                            CommonGradientButton(
                                text = stringResource(R.string.ot_accept), modifier = Modifier.weight(1f), enabled = !state.busy, height = dimen_48,
                                onClick = { r.id?.let { vm.respond(it, true) } }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(dimen_8))
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, OperatorInvitesActivity::class.java)
    }
}
