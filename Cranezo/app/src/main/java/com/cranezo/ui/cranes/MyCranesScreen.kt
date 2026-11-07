package com.cranezo.ui.cranes

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneListCard
import com.cranezo.ui.cranes.model.ApprovalStatus
import com.cranezo.ui.cranes.model.Crane
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.model.MyCranesState
import com.cranezo.ui.theme.*

/**
 * My Cranes tab: every crane of the owner with its status. The list loads page by page; the next
 * page is requested when the owner scrolls close to the end and the server says there is more.
 */
@Composable
fun MyCranesScreen(
    state: MyCranesState,
    onAddCrane: () -> Unit,
    onOpenCrane: (Crane) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    // Close to the end of the loaded cranes: ask for the next page
    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(nearEnd, state.hasMore, state.items.size) {
        if (nearEnd && state.hasMore && state.items.isNotEmpty()) onLoadMore()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimen_20)
                .padding(top = dimen_16, bottom = dimen_12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Textview(
                    text = stringResource(R.string.tab_my_cranes),
                    size = font_24,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Textview(
                    text = stringResource(R.string.crane_my_subtitle),
                    size = font_11,
                    color = appColors.textSecondary
                )
            }
            CommonGradientButton(
                text = stringResource(R.string.home_add_crane),
                gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                textColor = Color.Black,
                textSize = font_13,
                height = dimen_40,
                leftIcon = Icons.Default.Add,
                iconSize = dimen_18,
                modifier = Modifier.fillMaxWidth(0.4f),
                onClick = onAddCrane
            )
        }

        when {
            // First load
            state.items.isEmpty() && (state.isLoading || !state.loaded) -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                com.cranezo.ui.components.CranezoLoader()
            }

            state.items.isEmpty() -> EmptyOrError(state.error, onAddCrane, onRefresh)

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = dimen_20, end = dimen_20, bottom = dimen_16
                ),
                verticalArrangement = Arrangement.spacedBy(dimen_12)
            ) {
                items(state.items, key = { it.id ?: it.crane_code ?: it.hashCode() }) { crane ->
                    CraneListCard(crane = crane, onClick = { onOpenCrane(crane) })
                }
                if (state.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(dimen_12),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_2, modifier = Modifier.size(dimen_24))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyOrError(error: String?, onAddCrane: () -> Unit, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimen_20),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = dimen_24) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                IconBadge(icon = Icons.Default.PrecisionManufacturing, accent = AmberPrimary, size = dimen_56)
                Spacer(modifier = Modifier.height(dimen_14))
                Textview(
                    text = stringResource(if (error != null) R.string.crane_list_failed else R.string.crane_empty_title),
                    size = font_18,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(dimen_4))
                Textview(
                    text = error ?: stringResource(R.string.crane_empty_message),
                    size = font_12,
                    color = appColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(dimen_16))
                CommonGradientButton(
                    text = stringResource(if (error != null) R.string.common_retry else R.string.home_add_crane),
                    gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                    textColor = Color.Black,
                    onClick = if (error != null) onRefresh else onAddCrane
                )
            }
        }
    }
}

@Preview(name = "My Cranes - Dark", showBackground = true, heightDp = 800)
@Composable
private fun MyCranesDarkPreview() {
    CranezoTheme(darkTheme = true) {
        ScreenBackground {
            MyCranesScreen(
                state = MyCranesState(
                    loaded = true,
                    items = listOf(
                        Crane(
                            id = "1", crane_code = "CRN001", crane_type_name = "Mobile Crane", capacity_name = "50 Ton",
                            location = "Chennai, TN", approval_status = ApprovalStatus.VERIFIED, approval_label = "Verified"
                        ),
                        Crane(
                            id = "2", crane_code = "CRN004", crane_type_name = "Truck Mounted Crane", capacity_name = "80 Ton",
                            location = "Coimbatore, TN", approval_status = ApprovalStatus.UNDER_REVIEW, approval_label = "Under Review"
                        )
                    )
                ),
                onAddCrane = {}, onOpenCrane = {}, onRefresh = {}, onLoadMore = {}
            )
        }
    }
}
