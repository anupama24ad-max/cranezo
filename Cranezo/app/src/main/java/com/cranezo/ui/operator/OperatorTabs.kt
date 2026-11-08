package com.cranezo.ui.operator

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.cranes.components.CraneSmallButton
import com.cranezo.ui.mainhome.components.HomeHeader
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SectionTitle
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.operator.model.DocumentStatus
import com.cranezo.ui.operator.model.EarningItem
import com.cranezo.ui.operator.model.EarningsSummary
import com.cranezo.ui.operator.model.HomeCrane
import com.cranezo.ui.operator.model.JobListItem
import com.cranezo.ui.operator.model.OperatorHome
import com.cranezo.ui.operator.model.OperatorTabsIntent
import com.cranezo.ui.operator.model.OperatorTabsState
import com.cranezo.ui.operator.model.TodayJobs
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.rupees
import com.cranezo.utils.serverDay
import com.cranezo.utils.serverMonth
import com.cranezo.utils.serverYear

private val previewJob = JobListItem(
    booking_id = "1", status = 1, tab_label = "Assigned", stage = "awaiting_operator", customer_name = "Sai Constructions",
    location = "Kakinada", start_at = "2026-09-10T04:30:00.000Z", crane_label = "Tata 25 Ton (AP XX 1234)",
    duration_label = "8 Hours", estimated_earnings = 12000.0
)

@Composable
private fun tabTagColor(tab: String?): Color = when (tab) {
    "Completed" -> SuccessGreen
    "In Progress" -> AmberPrimary
    else -> InfoBlue
}

/** Short label of a job status, translated for the known tabs. */
@Composable
private fun tabLabel(tab: String?): String = when (tab) {
    "Assigned" -> stringResource(R.string.op_tab_assigned)
    "In Progress" -> stringResource(R.string.op_tab_in_progress)
    "Completed" -> stringResource(R.string.op_tab_completed)
    else -> tab.orEmpty()
}

// ---------------------------------------------------------------------------------------------
// Home
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
fun OperatorHomeTab(
    vm: OperatorTabsViewModel? = null,
    onOpenJob: (String) -> Unit = {},
    onOpenJobs: () -> Unit = {},
    onOpenEarnings: () -> Unit = {},
    onOpenDocuments: () -> Unit = {},
    onOpenCrane: (String) -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: OperatorTabsState(
        home = OperatorHome(
            greeting_name = "Ravi Kumar", online = false, online_label = "Offline", online_message = "You are currently offline",
            assigned_crane = HomeCrane(id = "1", model_name = "Tata 25 Ton", registration_number = "AP XX 1234", crane_type_name = "Mobile Crane", owner_name = "ABC Crane Services", availability_label = "Available"),
            today_jobs = TodayJobs(1, listOf(previewJob)), today_earnings = 4500.0,
            document_status = DocumentStatus(all_verified = true, label = "All documents verified")
        )
    )
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) { vm?.onIntent(OperatorTabsIntent.LoadHome) }
    // a push about a job, request or payment: show the change right away
    com.cranezo.push.OnPush { vm?.onIntent(OperatorTabsIntent.LoadHome) }
    val home = state.home

    com.cranezo.ui.mainhome.components.HomeScreenColumn(
        spacing = dimen_12,
        hero = { HomeHeader(name = home?.greeting_name.orEmpty(), subtitle = stringResource(R.string.op_home_subtitle)) }
    ) {

        // A job the owner just gave: shown here on the page (sound + vibration), not as a popup
        state.incomingJob?.let { id ->
            NewJobAlert(
                onView = {
                    vm?.onIntent(OperatorTabsIntent.DismissIncoming)
                    onOpenJob(id)
                },
                onLater = { vm?.onIntent(OperatorTabsIntent.DismissIncoming) }
            )
        }

        // Resume a job that is on the road or in progress
        home?.active_job?.booking_id?.let { id ->
            GlassCard(contentPadding = dimen_12, borderColor = AmberPrimary, onClick = { onOpenJob(id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.op_resume_job), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                        Textview(home.active_job.booking_code.orEmpty(), size = font_11, color = appColors.textSecondary)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AmberPrimary)
                }
            }
        }

        // Owners give a job by this ID (one job at a time, then the operator is free for other work)
        val context = androidx.compose.ui.platform.LocalContext.current
        val memberId = androidx.compose.runtime.remember {
            com.cranezo.utils.SessionManager(context).getData(com.cranezo.utils.AppStrings.SessionValues.memberId, "")
        }
        if (memberId.isNotBlank()) {
            GlassCard(contentPadding = dimen_12) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.Work, accent = SuccessGreen, size = dimen_40)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.rq_your_operator_id), size = font_12, color = appColors.textSecondary)
                        Textview(memberId, size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(stringResource(R.string.rq_your_operator_id_sub), size = font_11, color = appColors.textSecondary)
                    }
                }
            }
        }

        // not verified yet: nothing can be done until the admin approves the documents
        val notVerified = home != null && home.verified == false
        if (notVerified) {
            GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.Work, accent = AmberPrimary, size = dimen_40)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.op_not_verified_title), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(stringResource(R.string.op_not_verified_sub), size = font_12, color = appColors.textSecondary)
                    }
                }
            }
        }

        // Online / offline
        GlassCard(contentPadding = dimen_12) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.Work, accent = if (home?.online == true) SuccessGreen else AmberPrimary, size = dimen_40)
                Spacer(modifier = Modifier.width(dimen_12))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(
                        text = stringResource(if (home?.online == true) R.string.op_online else R.string.op_offline),
                        size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold
                    )
                    Textview(
                        text = stringResource(if (home?.online == true) R.string.op_online_message else R.string.op_offline_message),
                        size = font_11, color = appColors.textSecondary
                    )
                }
                androidx.compose.material3.Switch(
                    checked = home?.online == true,
                    // going online needs verified documents (going offline is always allowed)
                    enabled = !notVerified || home?.online == true,
                    onCheckedChange = { vm?.onIntent(OperatorTabsIntent.ToggleOnline) },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessGreen,
                        checkedBorderColor = SuccessGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = appColors.textHint,
                        uncheckedBorderColor = appColors.textHint
                    )
                )
            }
        }

        // Assigned crane
        GlassCard(contentPadding = dimen_14) {
            SectionTitle(
                text = stringResource(R.string.mp_assigned_crane),
                actionText = if (home?.assigned_crane?.id != null) stringResource(R.string.mp_view_details) else null,
                onActionClick = { home?.assigned_crane?.id?.let(onOpenCrane) }
            )
            Spacer(modifier = Modifier.height(dimen_10))
            val crane = home?.assigned_crane
            if (crane == null) {
                Textview(stringResource(R.string.mp_no_crane_assigned), size = font_13, color = appColors.textSecondary)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CraneImage(path = crane.primary_photo, modifier = Modifier.size(dimen_70))
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        // name on the left, availability tag pinned top-right
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Textview(
                                crane.model_name ?: crane.title.orEmpty(), size = font_15, color = appColors.textPrimary,
                                fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f)
                            )
                            crane.availability_label?.let {
                                Spacer(modifier = Modifier.width(dimen_8))
                                BookingTag(it, SuccessGreen)
                            }
                        }
                        Spacer(modifier = Modifier.height(dimen_2))
                        Textview(
                            listOf(crane.registration_number, crane.crane_type_name).filter { !it.isNullOrBlank() }.joinToString(" · "),
                            size = font_11, color = appColors.textSecondary, maxLines = 1
                        )
                        if (!crane.owner_name.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(dimen_6))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Textview(stringResource(R.string.op_assigned_owner) + ": ", size = font_11, color = appColors.textSecondary)
                                Textview(crane.owner_name, size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                            }
                        }
                    }
                }
            }
        }

        // Today's jobs
        GlassCard(contentPadding = dimen_14) {
            SectionTitle(
                text = stringResource(R.string.op_todays_jobs),
                actionText = stringResource(R.string.common_view_all),
                onActionClick = onOpenJobs
            )
            val items = home?.today_jobs?.items.orEmpty()
            if (items.isEmpty()) {
                Spacer(modifier = Modifier.height(dimen_8))
                Textview(stringResource(R.string.op_no_jobs_today), size = font_13, color = appColors.textSecondary)
            }
            items.forEach { job ->
                Spacer(modifier = Modifier.height(dimen_10))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DateBlock(job.start_at)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(job.customer_name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                        Textview(job.location.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                        Textview("${formatServerDateTime(job.start_at)} · ${job.duration_label.orEmpty()}", size = font_10, color = appColors.textSecondary, maxLines = 1)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        BookingTag(tabLabel(job.tab_label), tabTagColor(job.tab_label))
                        Spacer(modifier = Modifier.height(dimen_6))
                        CraneSmallButton(text = stringResource(R.string.op_view_job), onClick = { job.booking_id?.let(onOpenJob) })
                    }
                }
            }
        }

        // Today's earnings
        GlassCard(contentPadding = dimen_14, onClick = onOpenEarnings) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.Payments, accent = AmberPrimary, size = dimen_32)
                Spacer(modifier = Modifier.width(dimen_10))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.op_todays_earnings), size = font_12, color = appColors.textSecondary)
                    Textview(rupees(home?.today_earnings ?: 0.0), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                }
                Textview(stringResource(R.string.op_view_earnings), size = font_11, color = appColors.primaryText, fontWeight = FontWeight.SemiBold)
            }
        }

        // Document status
        GlassCard(contentPadding = dimen_14, onClick = onOpenDocuments) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.Description, accent = AmberPrimary, size = dimen_32)
                Spacer(modifier = Modifier.width(dimen_10))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.op_document_status), size = font_12, color = appColors.textSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val ok = home?.document_status?.all_verified == true
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (ok) SuccessGreen else AmberPrimary, modifier = Modifier.size(dimen_16))
                        Spacer(modifier = Modifier.width(dimen_6))
                        Textview(
                            text = if (ok) stringResource(R.string.op_docs_verified) else home?.document_status?.label.orEmpty(),
                            size = font_13, color = if (ok) SuccessGreen else AmberPrimary, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Textview(stringResource(R.string.op_view_documents), size = font_11, color = appColors.primaryText, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

@Composable
private fun DateBlock(startAt: String?) {
    Column(
        modifier = Modifier
            .size(dimen_48)
            .clip(RoundedCornerShape(dimen_10))
            .background(appColors.subtleFill),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Textview(serverDay(startAt), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Textview(serverMonth(startAt), size = font_10, color = appColors.textSecondary)
        Textview(serverYear(startAt), size = font_10, color = appColors.textHint)
    }
}

// ---------------------------------------------------------------------------------------------
// Jobs
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
fun OperatorJobsTab(
    vm: OperatorTabsViewModel? = null,
    onOpenJob: (String) -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: OperatorTabsState(jobs = listOf(previewJob), jobsLoaded = true)
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) { vm?.onIntent(OperatorTabsIntent.LoadJobs(state.jobsTab)) }
    // a push about a job, request or payment: show the change right away
    com.cranezo.push.OnPush { vm?.onIntent(OperatorTabsIntent.LoadJobs(state.jobsTab)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_12)
    ) {
        Textview(stringResource(R.string.op_jobs_title), size = font_24, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Textview(stringResource(R.string.op_jobs_subtitle), size = font_12, color = appColors.textSecondary)

        Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            listOf(
                "all" to R.string.op_tab_all, "assigned" to R.string.op_tab_assigned,
                "in_progress" to R.string.op_tab_in_progress, "completed" to R.string.op_tab_completed
            ).forEach { (key, label) ->
                val selected = key == state.jobsTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimen_16))
                        .background(if (selected) AmberPrimary else appColors.subtleFill)
                        .clickableTab { vm?.onIntent(OperatorTabsIntent.LoadJobs(key)) }
                        .padding(vertical = dimen_8),
                    contentAlignment = Alignment.Center
                ) {
                    Textview(
                        stringResource(label), size = font_11, maxLines = 1,
                        color = if (selected) Color.Black else appColors.textPrimary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        if (state.jobsLoaded && state.jobs.isEmpty()) {
            Textview(stringResource(R.string.op_no_jobs), size = font_14, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_40).fillMaxWidth(), textAlign = TextAlign.Center)
        }

        state.jobs.forEach { job ->
            GlassCard(contentPadding = dimen_12) {
                Row(verticalAlignment = Alignment.Top) {
                    DateBlock(job.start_at)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Textview(job.customer_name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                            BookingTag(tabLabel(job.tab_label), tabTagColor(job.tab_label))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_12))
                            Textview(job.location.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                        }
                        Textview(job.crane_label.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                        Textview("${formatServerDateTime(job.start_at)} · ${job.duration_label.orEmpty()}", size = font_11, color = appColors.textSecondary, maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.height(dimen_10))
                CommonGradientButton(
                    text = stringResource(R.string.op_view_job),
                    onClick = { job.booking_id?.let(onOpenJob) },
                    height = dimen_40,
                    textSize = font_12,
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill),
                    textColor = AmberPrimary
                )
            }
        }

        if (state.jobsHasMore) {
            LaunchedEffect(state.jobs.size) { vm?.onIntent(OperatorTabsIntent.LoadMoreJobs) }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

private fun Modifier.clickableTab(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable { onClick() }
)

// ---------------------------------------------------------------------------------------------
// Earnings
// ---------------------------------------------------------------------------------------------

@Composable
private fun EarningTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier, tint: Color = AmberPrimary) {
    GlassCard(modifier = modifier.fillMaxHeight(), contentPadding = dimen_12, minHeight = dimen_96) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, accent = tint, size = dimen_36)
            Spacer(modifier = Modifier.width(dimen_10))
            Column {
                Textview(label, size = font_11, color = appColors.textSecondary, maxLines = 1)
                Textview(value, size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun OperatorEarningsTab(
    vm: OperatorTabsViewModel? = null,
    onOpenJob: (String) -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: OperatorTabsState(
        earnings = EarningsSummary(1, 50000.0, 50000.0, 0.0, 3),
        earningItems = listOf(EarningItem("1", "CRZ-1", "Sai Constructions", "OMR, Chennai", "2025-09-15T09:00:00.000Z", null, "50 Ton Mobile Crane", 50000.0, "pending", "Pending Settlement")),
        earningsTotal = 1, earningsLoaded = true
    )
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) { vm?.onIntent(OperatorTabsIntent.LoadEarnings) }
    // a push about a job, request or payment: show the change right away
    com.cranezo.push.OnPush { vm?.onIntent(OperatorTabsIntent.LoadEarnings) }
    val summary = state.earnings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_12)
    ) {
        Textview(stringResource(R.string.op_earnings_title), size = font_24, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
            EarningTile(Icons.Default.Work, stringResource(R.string.op_todays_jobs), (summary?.today_jobs ?: 0).toString(), Modifier.weight(1f))
            EarningTile(Icons.Default.AccountBalanceWallet, stringResource(R.string.op_todays_earnings), rupees(summary?.today_earnings ?: 0.0), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
            EarningTile(Icons.Default.Schedule, stringResource(R.string.op_pending_settlement), rupees(summary?.pending_settlement ?: 0.0), Modifier.weight(1f))
            EarningTile(Icons.Default.CheckCircle, stringResource(R.string.op_completed_settlement), rupees(summary?.completed_settlement ?: 0.0), Modifier.weight(1f), SuccessGreen)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(stringResource(R.string.op_jobs_earnings), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Textview(stringResource(R.string.op_n_jobs, state.earningsTotal), size = font_11, color = appColors.textSecondary)
        }

        state.earningItems.forEach { item ->
            val settled = item.settlement_status == "settled"
            GlassCard(contentPadding = dimen_12, minHeight = dimen_88, onClick = { item.booking_id?.let(onOpenJob) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CraneImage(path = null, modifier = Modifier.size(dimen_56))
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(item.customer_name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                        Textview(item.crane_label.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                        Textview(formatServerDate(item.completed_at ?: item.start_at), size = font_11, color = appColors.textSecondary)
                        Textview(item.location.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Textview(rupees(item.amount), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(dimen_4))
                        BookingTag(
                            stringResource(if (settled) R.string.op_settled else R.string.op_pending_settlement_tag),
                            if (settled) SuccessGreen else AmberPrimary
                        )
                    }
                }
            }
        }

        if (state.earningsHasMore) {
            LaunchedEffect(state.earningItems.size) { vm?.onIntent(OperatorTabsIntent.LoadMoreEarnings) }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

/** "New job for you": a sound and a vibration, then a card at the top of Home with Open job / Later. No popup. */
@Composable
fun NewJobAlert(onView: () -> Unit, onLater: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        runCatching {
            android.media.RingtoneManager.getRingtone(context, android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)).play()
            @Suppress("DEPRECATION")
            (context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator)?.vibrate(600)
        }
    }
    GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = Icons.Default.Work, accent = AmberPrimary, size = dimen_40)
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(stringResource(R.string.fin_new_job_banner), size = font_16, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                Textview(stringResource(R.string.fin_new_job_banner_sub), size = font_12, color = appColors.textSecondary)
            }
        }
        Spacer(modifier = Modifier.height(dimen_10))
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_10)) {
            CommonGradientButton(
                text = stringResource(R.string.op_later), modifier = Modifier.weight(1f), height = dimen_44, onClick = onLater,
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
            )
            CommonGradientButton(text = stringResource(R.string.fin_open_job), modifier = Modifier.weight(1f), height = dimen_44, onClick = onView)
        }
    }
}
