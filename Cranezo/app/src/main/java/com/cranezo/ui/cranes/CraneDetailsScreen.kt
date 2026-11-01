package com.cranezo.ui.cranes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.cranes.components.CraneScreenHeader
import com.cranezo.ui.cranes.components.CraneSmallButton
import com.cranezo.ui.cranes.components.CraneStatusBadge
import com.cranezo.ui.cranes.components.CraneStatusBanner
import com.cranezo.ui.cranes.components.approvalColor
import com.cranezo.ui.cranes.components.approvalLabel
import com.cranezo.ui.cranes.model.ApprovalStatus
import com.cranezo.ui.cranes.model.Crane
import com.cranezo.ui.cranes.model.CraneDocument
import com.cranezo.ui.cranes.model.CranesIntent
import com.cranezo.ui.cranes.model.CranesUiState
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.theme.*
import com.cranezo.utils.fileUrl
import com.cranezo.utils.formatAmount
import com.cranezo.utils.formatDisplayDate
import com.cranezo.utils.isDateExpired
import kotlinx.coroutines.launch

/** Opens a document or image url in whatever app can show it. */
fun openFile(context: Context, path: String?) {
    val url = fileUrl(path)
    if (url == null) {
        Toast.makeText(context, R.string.crane_msg_no_file, Toast.LENGTH_SHORT).show()
        return
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(context, R.string.crane_msg_no_file_app, Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CraneDetailsScreen(
    craneId: String,
    state: CranesUiState,
    onIntent: (CranesIntent) -> Unit,
    onBack: () -> Unit,
    onEdit: (Crane) -> Unit,
    onManageDocuments: () -> Unit
) {
    // Every time the screen opens, e.g. after an edit, the crane is fetched again
    LaunchedEffect(craneId) { onIntent(CranesIntent.LoadDetail(craneId)) }

    val crane = state.detail?.takeIf { it.id == craneId }

    ScreenBackground(darkImageRes = R.drawable.app_bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            CraneScreenHeader(
                title = stringResource(R.string.crane_details_title),
                onBack = onBack,
                action = {
                    if (crane != null) {
                        CraneSmallButton(
                            text = stringResource(R.string.common_edit),
                            icon = Icons.Default.Edit,
                            onClick = { onEdit(crane) }
                        )
                    }
                }
            )

            when {
                crane != null -> DetailsContent(crane, onManageDocuments)

                state.detailLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AmberPrimary)
                }

                else -> Column(
                    modifier = Modifier.fillMaxSize().padding(dimen_24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Textview(
                        text = state.detailError ?: stringResource(R.string.crane_msg_generic_error),
                        size = font_14,
                        color = appColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(dimen_16))
                    CommonGradientButton(
                        text = stringResource(R.string.common_retry),
                        gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                        textColor = Color.Black,
                        modifier = Modifier.width(dimen_150),
                        onClick = { onIntent(CranesIntent.LoadDetail(craneId)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailsContent(crane: Crane, onManageDocuments: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_12)
    ) {
        Gallery(crane)

        // Title and price. The rating and jobs completed are not in the API yet, so they are left out.
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Textview(
                    text = crane.crane_type_name ?: crane.model_name.orEmpty(),
                    size = font_22,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (!crane.model_name.isNullOrBlank() && crane.crane_type_name != null) {
                    Textview(text = crane.model_name, size = font_13, color = appColors.textSecondary, maxLines = 1)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Textview(
                    text = stringResource(R.string.crane_price, formatAmount(crane.hourly_rate)),
                    size = font_20,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Textview(text = stringResource(R.string.crane_per_hour), size = font_11, color = appColors.textSecondary)
            }
        }

        // Status banner for Under Review / Rejected (with the rejection reason)
        CraneStatusBanner(crane)

        // Four quick facts
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            InfoTile(Icons.Default.PrecisionManufacturing, stringResource(R.string.crane_tile_capacity), crane.capacity_name, Modifier.weight(1f))
            InfoTile(Icons.Default.LocationOn, stringResource(R.string.crane_tile_location), crane.location, Modifier.weight(1f))
            InfoTile(Icons.Default.CheckCircle, stringResource(R.string.crane_tile_availability), crane.availability_label, Modifier.weight(1f))
            InfoTile(
                Icons.Default.VerifiedUser, stringResource(R.string.crane_tile_status), approvalLabel(crane), Modifier.weight(1f),
                valueColor = approvalColor(crane.approval_status)
            )
        }

        // Specifications
        Textview(
            text = stringResource(R.string.crane_specifications),
            size = font_15,
            color = appColors.textPrimary,
            fontWeight = FontWeight.Bold
        )
        GlassCard(shape = RoundedCornerShape(dimen_16), contentPadding = dimen_14) {
            val notSet = stringResource(R.string.crane_not_set)
            val specs = listOf(
                stringResource(R.string.crane_spec_type) to (crane.crane_type_name ?: notSet),
                stringResource(R.string.crane_spec_capacity) to (crane.capacity_name ?: notSet),
                stringResource(R.string.crane_spec_min_hours) to (crane.minimum_hours?.let { stringResource(R.string.crane_hours_value, it) } ?: notSet),
                stringResource(R.string.crane_spec_registration) to (crane.registration_number ?: notSet),
                stringResource(R.string.crane_spec_fuel) to (crane.fuel_type_name ?: notSet),
                stringResource(R.string.crane_spec_rate) to stringResource(R.string.crane_price, formatAmount(crane.hourly_rate))
            )
            specs.chunked(2).forEachIndexed { index, pair ->
                if (index > 0) Spacer(modifier = Modifier.height(dimen_12))
                Row {
                    pair.forEach { (label, value) ->
                        Column(modifier = Modifier.weight(1f)) {
                            Textview(text = label, size = font_11, color = appColors.textSecondary)
                            Textview(
                                text = value,
                                size = font_14,
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        OperatorCard(crane)

        // Documents
        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(
                text = stringResource(R.string.crane_documents),
                size = font_15,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Textview(
                text = stringResource(R.string.common_view_all),
                size = font_12,
                color = AmberPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onManageDocuments() }
            )
        }
        val documents = crane.documents.orEmpty()
        if (documents.isEmpty()) {
            Textview(text = stringResource(R.string.crane_no_documents), size = font_12, color = appColors.textSecondary)
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(dimen_8)
            ) {
                documents.forEach { document ->
                    DocumentChip(document, onClick = { openFile(context, document.file_url) })
                }
            }
        }

        Spacer(modifier = Modifier.height(dimen_16))
    }
}

/** Photo pager with the status and crane code on top, a counter, and a strip of thumbnails. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Gallery(crane: Crane) {
    val photos = crane.photoUrls
    val pageCount = photos.size.coerceAtLeast(1)
    val pager = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxWidth().height(dimen_220).clip(RoundedCornerShape(dimen_16))) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            CraneImage(
                path = photos.getOrNull(page),
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(dimen_0)
            )
        }
        CraneStatusBadge(
            status = crane.approval_status,
            label = approvalLabel(crane),
            modifier = Modifier.align(Alignment.TopStart).padding(dimen_10)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(dimen_10)
                .clip(RoundedCornerShape(dimen_10))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = dimen_8, vertical = dimen_3)
        ) {
            Textview(text = crane.crane_code.orEmpty(), size = font_11, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(dimen_10)
                .clip(RoundedCornerShape(dimen_10))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = dimen_8, vertical = dimen_3)
        ) {
            Textview(text = "${pager.currentPage + 1}/$pageCount", size = font_11, color = Color.White)
        }
    }

    if (photos.size > 1) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(dimen_8)
        ) {
            photos.forEachIndexed { index, path ->
                val selected = index == pager.currentPage
                CraneImage(
                    path = path,
                    modifier = Modifier
                        .size(dimen_56)
                        .border(
                            width = if (selected) dimen_2 else dimen_1,
                            color = if (selected) AmberPrimary else appColors.subtleBorder,
                            shape = RoundedCornerShape(dimen_10)
                        )
                        .clickable { scope.launch { pager.animateScrollToPage(index) } },
                    shape = RoundedCornerShape(dimen_10)
                )
            }
        }
    }
}

@Composable
private fun InfoTile(
    icon: ImageVector,
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified
) {
    Column(
        modifier = modifier
            .glassEffect(RoundedCornerShape(dimen_12), inset = true)
            .padding(horizontal = dimen_6, vertical = dimen_10),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = AmberPrimary, modifier = Modifier.size(dimen_18))
        Spacer(modifier = Modifier.height(dimen_4))
        Textview(
            text = value?.takeIf { it.isNotBlank() } ?: "-",
            size = font_11,
            color = if (valueColor == Color.Unspecified) appColors.textPrimary else valueColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Textview(text = label, size = font_10, color = appColors.textSecondary, maxLines = 1)
    }
}

@Composable
private fun OperatorCard(crane: Crane) {
    val operator = crane.operator
    GlassCard(shape = RoundedCornerShape(dimen_16), contentPadding = dimen_12) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(dimen_44)
                    .clip(CircleShape)
                    .border(dimen_1, AmberPrimary, CircleShape)
                    .background(AmberPrimary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = AmberPrimary, modifier = Modifier.size(dimen_24))
            }
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(
                    text = operator?.full_name ?: stringResource(R.string.crane_operator_not_assigned),
                    size = font_14,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (operator != null) {
                    val line = listOfNotNull(
                        operator.member_id,
                        operator.mobile_number?.let { "${operator.country_code.orEmpty()} $it".trim() }
                    ).joinToString("  |  ")
                    Textview(text = line, size = font_11, color = appColors.textSecondary, maxLines = 1)
                } else {
                    Textview(
                        text = stringResource(R.string.crane_operator_assign_hint),
                        size = font_11,
                        color = appColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentChip(document: CraneDocument, onClick: () -> Unit) {
    val expired = isDateExpired(document.expiry_date)
    Column(
        modifier = Modifier
            .width(dimen_110)
            .glassEffect(
                shape = RoundedCornerShape(dimen_12),
                borderColor = if (expired) Red.copy(alpha = 0.8f) else Color.Unspecified,
                inset = true
            )
            .clickable { onClick() }
            .padding(dimen_10)
    ) {
        Icon(Icons.Default.Description, null, tint = AmberPrimary, modifier = Modifier.size(dimen_20))
        Spacer(modifier = Modifier.height(dimen_6))
        Textview(
            text = document.document_type_name.orEmpty(),
            size = font_12,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
        Textview(
            text = when {
                expired -> stringResource(R.string.crane_doc_expired, formatDisplayDate(document.expiry_date))
                !document.expiry_date.isNullOrBlank() -> stringResource(R.string.crane_doc_expires, formatDisplayDate(document.expiry_date))
                else -> stringResource(R.string.crane_doc_uploaded)
            },
            size = font_10,
            color = if (expired) Red else SuccessGreen,
            maxLines = 2
        )
    }
}
