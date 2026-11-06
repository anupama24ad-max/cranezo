package com.cranezo.ui.mainhome.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.ui.platform.LocalContext
import com.cranezo.ui.finance.FinanceActivity
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.ui.components.ShimmerImage
import com.cranezo.R
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.cranes.components.CraneSmallButton
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.SectionTitle
import com.cranezo.ui.mainhome.components.StatusChip
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.mainhome.model.MainHomeIntent
import com.cranezo.ui.mainhome.model.MainHomeUiState
import com.cranezo.ui.myprofile.ProfileRoutes
import com.cranezo.ui.myprofile.components.GroupLabel
import com.cranezo.ui.myprofile.components.LogoutConfirmSheet
import com.cranezo.ui.myprofile.components.MenuGroup
import com.cranezo.ui.myprofile.components.MenuRow
import com.cranezo.ui.myprofile.components.StatusBadge
import com.cranezo.ui.myprofile.documentSubtitle
import com.cranezo.ui.myprofile.model.AssignedCrane
import com.cranezo.ui.myprofile.model.OperatorBlock
import com.cranezo.ui.myprofile.model.OperatorDocument
import com.cranezo.ui.myprofile.model.ProfileData
import com.cranezo.ui.theme.*
import com.cranezo.utils.fileUrl

@Composable
private fun appearanceName(mode: AppThemeMode): String = stringResource(
    when (mode) {
        AppThemeMode.LIGHT -> R.string.mp_theme_light
        AppThemeMode.DARK -> R.string.mp_theme_dark
        AppThemeMode.SYSTEM -> R.string.mp_theme_system
    }
)

/** Round profile photo with a camera badge; the initial of the name while there is no photo. */
@Composable
private fun ProfileAvatar(profile: ProfileData?, fallbackName: String, onClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val photo = fileUrl(profile?.profile_image)
    Box(modifier = Modifier.size(dimen_70)) {
        Box(
            modifier = Modifier
                .size(dimen_66)
                .clip(CircleShape)
                .background(AmberPrimary.copy(alpha = 0.18f))
                .border(dimen_1, AmberPrimary, CircleShape)
                // a photo opens full screen; no photo opens the profile to add one (camera badge: always edit)
                .clickable {
                    if (photo != null) context.startActivity(com.cranezo.ui.preview.FilePreviewActivity.intent(context, photo, profile?.full_name))
                    else onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            val url = fileUrl(profile?.profile_image)
            val name = profile?.full_name?.ifBlank { null } ?: fallbackName
            Textview(
                text = name.take(1).uppercase().ifEmpty { "C" },
                size = font_22,
                color = AmberPrimary,
                fontWeight = FontWeight.Bold
            )
            if (url != null) {
                ShimmerImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(dimen_24)
                .clip(CircleShape)
                .background(appColors.screenBottom)
                .border(dimen_1, AmberPrimary, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = AmberPrimary,
                modifier = Modifier.size(dimen_14)
            )
        }
    }
}

/**
 * Profile tab, shared by every role. Customers and owners get the My Profile menu, operators get
 * their details, the assigned crane and the documents. Each row opens a screen of
 * [com.cranezo.ui.myprofile.MyProfileActivity] through [onOpen] with one of the [ProfileRoutes].
 */
@Preview(name = "Profile tab", showBackground = true, heightDp = 900)
@Composable
fun HomeProfileScreen(
    state: MainHomeUiState = MainHomeUiState(),
    profile: ProfileData? = null,
    onIntent: (MainHomeIntent) -> Unit = {},
    onOpen: (String) -> Unit = {}
) {
    // Fresh data every time the tab opens
    LaunchedEffect(Unit) { onIntent(MainHomeIntent.RefreshProfile) }

    val themeMode by AppThemeState.currentThemeMode
    var showLogout by remember { mutableStateOf(false) }
    if (showLogout) {
        LogoutConfirmSheet(
            onCancel = { showLogout = false },
            onConfirm = {
                showLogout = false
                onIntent(MainHomeIntent.Logout)
            }
        )
    }

    val role = UserRole.fromId(profile?.role_id) ?: state.role
    val context = LocalContext.current
    val name = profile?.full_name?.ifBlank { null } ?: state.userName
    val languageName = profile?.language?.name

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_12)
    ) {
        Textview(
            text = stringResource(if (role == UserRole.OPERATOR) R.string.tab_profile else R.string.mp_my_profile),
            size = font_24,
            color = appColors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        // Header card
        GlassCard(contentPadding = dimen_14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(profile, name) { onOpen(ProfileRoutes.PERSONAL) }
                Spacer(modifier = Modifier.width(dimen_14))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(
                        text = name.ifBlank { stringResource(R.string.home_profile_default_name) },
                        size = font_18,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    val designation = profile?.designation
                    Textview(
                        text = designation ?: stringResource(role.titleRes),
                        size = font_12,
                        color = appColors.textSecondary,
                        maxLines = 1
                    )
                    if (!profile?.company_name.isNullOrBlank()) {
                        Textview(text = profile?.company_name.orEmpty(), size = font_12, color = appColors.textSecondary, maxLines = 1)
                    }
                    val contact = profile?.contact?.ifBlank { null } ?: state.mobileNumber
                    if (contact.isNotBlank()) {
                        Spacer(modifier = Modifier.height(dimen_4))
                        Textview(text = contact, size = font_13, color = appColors.textPrimary)
                    }
                    if (role == UserRole.OPERATOR) {
                        Textview(
                            text = stringResource(R.string.mp_mobile_cannot_change),
                            size = font_10,
                            color = appColors.textHint
                        )
                    }
                    val memberId = profile?.member_id?.ifBlank { null } ?: state.memberId
                    if (memberId.isNotBlank()) {
                        Textview(
                            text = stringResource(R.string.home_profile_id, memberId),
                            size = font_11,
                            color = appColors.textSecondary
                        )
                    }
                    if (profile?.is_mobile_verified == true) {
                        Spacer(modifier = Modifier.height(dimen_4))
                        StatusChip(text = stringResource(R.string.common_verified), color = SuccessGreen)
                    }
                }
            }
        }

        if (role == UserRole.OPERATOR) {
            AssignedCraneCard(profile?.operator, onOpen)
            DocumentsCard(profile?.operator?.documents.orEmpty(), onOpen)

            MenuGroup {
                // find open jobs, job requests and the payments the owner made
                MenuRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = stringResource(R.string.fin_my_payments),
                    subtitle = stringResource(R.string.fin_my_payments_sub),
                    onClick = { context.startActivity(FinanceActivity.intent(context)) }
                )
                MenuRow(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.mp_language),
                    subtitle = stringResource(R.string.mp_language_row_subtitle),
                    trailingText = languageName,
                    onClick = { onOpen(ProfileRoutes.LANGUAGE) }
                )
                MenuRow(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.mp_appearance),
                    subtitle = stringResource(R.string.mp_appearance_row_subtitle),
                    trailingText = appearanceName(themeMode),
                    onClick = { onOpen(ProfileRoutes.APPEARANCE) }
                )
            }
        } else {
            GroupLabel(stringResource(R.string.mp_group_account))
            MenuGroup {
                // owner: job requests, operator pay, settlement, GST and charges, complaints
                // customer: payment history and complaints
                MenuRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = stringResource(if (role == UserRole.CRANE_OWNER) R.string.fin_entry_owner else R.string.fin_entry_customer),
                    subtitle = stringResource(if (role == UserRole.CRANE_OWNER) R.string.fin_platform_sub else R.string.fin_complaints_customer_sub),
                    onClick = { context.startActivity(FinanceActivity.intent(context)) }
                )
                MenuRow(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.mp_personal_info),
                    subtitle = stringResource(R.string.mp_personal_info_row_subtitle),
                    onClick = { onOpen(ProfileRoutes.PERSONAL) }
                )
                MenuRow(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.mp_language),
                    subtitle = languageName ?: stringResource(R.string.mp_language_row_subtitle),
                    onClick = { onOpen(ProfileRoutes.LANGUAGE) }
                )
                // Not in the design yet: kept right below Language until the placement is confirmed
                MenuRow(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.mp_appearance),
                    subtitle = appearanceName(themeMode),
                    onClick = { onOpen(ProfileRoutes.APPEARANCE) }
                )
            }

            GroupLabel(stringResource(R.string.mp_group_support))
            MenuGroup {
                // notification settings removed (every notification is always on): the user's support requests instead
                val supportContext = androidx.compose.ui.platform.LocalContext.current
                MenuRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.sp_my_requests),
                    subtitle = stringResource(R.string.mp_my_requests_row_subtitle),
                    onClick = {
                        supportContext.startActivity(
                            com.cranezo.ui.support.SupportActivity.intent(supportContext, com.cranezo.ui.support.SupportRoutes.TICKETS)
                        )
                    }
                )
                MenuRow(
                    icon = Icons.Default.SupportAgent,
                    title = stringResource(R.string.mp_help_support),
                    subtitle = stringResource(R.string.mp_help_row_subtitle),
                    onClick = { onOpen(ProfileRoutes.HELP) }
                )
                MenuRow(
                    icon = Icons.Default.Gavel,
                    title = stringResource(R.string.mp_terms),
                    subtitle = stringResource(R.string.mp_terms_row_subtitle),
                    onClick = { onOpen(ProfileRoutes.TERMS) }
                )
                MenuRow(
                    icon = Icons.Default.PrivacyTip,
                    title = stringResource(R.string.mp_privacy),
                    subtitle = stringResource(R.string.mp_privacy_row_subtitle),
                    onClick = { onOpen(ProfileRoutes.PRIVACY) }
                )
            }
        }

        CommonGradientButton(
            text = stringResource(R.string.home_logout),
            gradientColors = listOf(Red, DarkRed, Maroon),
            textColor = Color.White,
            onClick = { showLogout = true }
        )
    }
}

@Composable
private fun AssignedCraneCard(operator: OperatorBlock?, onOpen: (String) -> Unit) {
    val crane: AssignedCrane? = operator?.assigned_crane
    GlassCard(contentPadding = dimen_14) {
        SectionTitle(
            text = stringResource(R.string.mp_assigned_crane),
            icon = Icons.Default.Description,
            actionText = if (crane?.id != null) stringResource(R.string.mp_view_details) else null,
            onActionClick = { crane?.id?.let { onOpen(ProfileRoutes.crane(it)) } }
        )
        Spacer(modifier = Modifier.height(dimen_10))
        if (crane == null) {
            Textview(
                text = stringResource(R.string.mp_no_crane_assigned),
                size = font_13,
                color = appColors.textSecondary
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CraneImage(path = crane.primary_photo, modifier = Modifier.size(dimen_80))
                Spacer(modifier = Modifier.width(dimen_12))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(
                        text = crane.title ?: crane.model_name.orEmpty(),
                        size = font_15,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    if (!crane.model_name.isNullOrBlank() && crane.model_name != crane.title) {
                        Textview(text = crane.model_name, size = font_12, color = appColors.textSecondary, maxLines = 1)
                    }
                    if (!crane.crane_code.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(dimen_4))
                        StatusChip(text = crane.crane_code, color = AmberPrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimen_10))
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                listOf(crane.capacity_name, crane.crane_type_name, crane.fuel_type_name)
                    .filter { !it.isNullOrBlank() }
                    .forEach { Textview(text = it.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1) }
            }
        }
    }
}

@Composable
private fun DocumentsCard(documents: List<OperatorDocument>, onOpen: (String) -> Unit) {
    GlassCard(contentPadding = dimen_14) {
        SectionTitle(
            text = stringResource(R.string.mp_documents),
            icon = Icons.Default.Description,
            actionText = stringResource(R.string.common_view_all),
            onActionClick = { onOpen(ProfileRoutes.DOCUMENTS) }
        )
        documents.forEach { doc ->
            val (subtitle, expired) = documentSubtitle(doc)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimen_12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(icon = Icons.Default.Description, accent = AmberPrimary, size = dimen_32)
                Spacer(modifier = Modifier.width(dimen_10))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(
                        text = doc.label.orEmpty(),
                        size = font_13,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Textview(
                            text = subtitle,
                            size = font_11,
                            color = if (expired) Red else appColors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.width(dimen_8))
                StatusBadge(doc.status, serverLabel = doc.status_label)
            }
        }
    }
}
