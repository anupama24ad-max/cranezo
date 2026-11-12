package com.cranezo.ui.ratings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.repository.Repository
import com.cranezo.ui.components.Avatar
import com.cranezo.ui.components.EmptyState
import com.cranezo.ui.components.RatingChip
import com.cranezo.ui.components.StarRow
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import com.cranezo.utils.formatServerDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/**
 * "My ratings" for an operator (the jobs they drove) and an owner (the jobs done with their cranes):
 * the average and how many of each star, then every customer's stars and written feedback, newest first.
 */

data class RatingSummary(
    val average: Double? = null,
    val total: Int? = null,
    // "1".."5" -> how many
    val breakdown: Map<String, Int>? = null
)

data class RatingItem(
    val id: Int? = null,
    val rating: Int? = null,
    val comment: String? = null,
    val created_at: String? = null,
    val booking_id: String? = null,
    val booking_code: String? = null,
    val job_date: String? = null,
    val customer_name: String? = null,
    val customer_photo: String? = null,
    val crane_code: String? = null,
    val crane_name: String? = null
)

data class RatingsData(
    val summary: RatingSummary? = null,
    val items: List<RatingItem>? = null
)

data class MyRatingsState(
    val summary: RatingSummary? = null,
    val items: List<RatingItem> = emptyList(),
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val stars: Int? = null,
    val error: String? = null
)

@HiltViewModel
class MyRatingsViewModel @Inject constructor(
    app: Application,
    private val repo: Repository,
    private val sm: SessionManager
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(MyRatingsState())
    val state: StateFlow<MyRatingsState> = _state.asStateFlow()
    private val page = 20

    private fun path() = if (sm.getUserRole() == "OPERATOR") "operator/ratings" else "owner/ratings"

    fun load(stars: Int? = _state.value.stars) {
        _state.update { it.copy(loading = true, stars = stars, error = null, items = emptyList()) }
        fetch(0, stars)
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || !s.hasMore) return
        _state.update { it.copy(loadingMore = true) }
        fetch(s.items.size, s.stars)
    }

    private fun fetch(from: Int, stars: Int?) {
        viewModelScope.launch {
            val body = JSONObject().put("min", from).put("max", from + page).apply { stars?.let { put("stars", it) } }
            repo.ratingsApi(path(), AppMethods.getToken(sm), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val got = r?.data?.items.orEmpty()
                    _state.update {
                        it.copy(
                            summary = r?.data?.summary ?: it.summary,
                            items = if (from == 0) got else it.items + got,
                            hasMore = r?.pagination?.has_more == true,
                            loading = false,
                            loadingMore = false
                        )
                    }
                },
                failure = { _, _, message -> _state.update { it.copy(loading = false, loadingMore = false, error = message?.toString()) } }
            )
        }
    }

    companion object {
        fun of(context: android.content.Context): MyRatingsViewModel? =
            (context as? androidx.activity.ComponentActivity)?.let { ViewModelProvider(it)[MyRatingsViewModel::class.java] }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRatingsSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val vm = remember { MyRatingsViewModel.of(context) } ?: return
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.load(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = dimen_28, topEnd = dimen_28),
        containerColor = appColors.screenBottom
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.92f).navigationBarsPadding().padding(horizontal = dimen_20)) {
            Textview(stringResource(R.string.rt_title), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_4))
            Textview(stringResource(R.string.rt_subtitle), size = font_13, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_12))

            when {
                state.loading && state.items.isEmpty() && state.summary == null ->
                    Box(modifier = Modifier.fillMaxWidth().height(dimen_140), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_3)
                    }
                state.error != null && state.items.isEmpty() ->
                    EmptyState(Icons.Default.Star, stringResource(R.string.crane_msg_generic_error), actionText = stringResource(R.string.common_retry), onAction = { vm.load() })
                else -> RatingsList(state, onStars = { vm.load(it) }, onMore = { vm.loadMore() })
            }
        }
    }
}

@Composable
private fun RatingsList(state: MyRatingsState, onStars: (Int?) -> Unit, onMore: () -> Unit) {
    val summary = state.summary
    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimen_10), modifier = Modifier.fillMaxWidth()) {
        item { SummaryCard(summary) }
        item {
            // All, then 5 stars down to 1 star
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
                StarFilter(stringResource(R.string.jf_all), state.stars == null) { onStars(null) }
                for (n in 5 downTo 1) StarFilter("$n ★", state.stars == n) { onStars(n) }
            }
        }
        if (state.items.isEmpty() && !state.loading) {
            item {
                EmptyState(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.rt_empty_title),
                    body = stringResource(R.string.rt_empty_body)
                )
            }
        }
        items(state.items, key = { it.id ?: it.hashCode() }) { FeedbackCard(it) }
        if (state.hasMore) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_8), contentAlignment = Alignment.Center) {
                    if (state.loadingMore) CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_2, modifier = Modifier.size(dimen_24))
                    else Textview(stringResource(R.string.rt_load_more), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onMore() }.padding(dimen_8))
                }
            }
        }
        item { Spacer(modifier = Modifier.height(dimen_16)) }
    }
}

/** The big average, the stars under it and one bar per star count */
@Composable
private fun SummaryCard(summary: RatingSummary?) {
    val total = summary?.total ?: 0
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_16)).background(appColors.subtleFill).padding(dimen_16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(dimen_100)) {
            Textview(summary?.average?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "–", size = font_32, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            StarRow(Math.round(summary?.average ?: 0.0).toInt(), size = dimen_14)
            Spacer(modifier = Modifier.height(dimen_4))
            Textview(stringResource(R.string.rt_n_ratings, total), size = font_12, color = appColors.textSecondary, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.width(dimen_16))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimen_6)) {
            for (n in 5 downTo 1) {
                val count = summary?.breakdown?.get(n.toString()) ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview("$n", size = font_12, color = appColors.textSecondary, modifier = Modifier.width(dimen_12))
                    Box(modifier = Modifier.weight(1f).height(dimen_6).clip(RoundedCornerShape(50)).background(appColors.subtleFill)) {
                        if (total > 0 && count > 0) {
                            Box(modifier = Modifier.fillMaxWidth(count.toFloat() / total).height(dimen_6).clip(RoundedCornerShape(50)).background(AmberPrimary))
                        }
                    }
                    Textview("$count", size = font_12, color = appColors.textSecondary, textAlign = TextAlign.End, modifier = Modifier.width(dimen_28))
                }
            }
        }
    }
}

@Composable
private fun StarFilter(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) AmberPrimary.copy(alpha = 0.18f) else appColors.subtleFill)
            .clickable(onClick = onClick)
            .padding(horizontal = dimen_14, vertical = dimen_6),
        contentAlignment = Alignment.Center
    ) {
        Textview(text, size = font_13, color = if (selected) appColors.primaryText else appColors.textPrimary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
    }
}

/** One customer's feedback: who, the stars, when, the words, and which job it was */
@Composable
private fun FeedbackCard(item: RatingItem) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_16)).background(appColors.subtleFill).padding(dimen_14)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(item.customer_photo, item.customer_name, size = dimen_40)
            Spacer(modifier = Modifier.width(dimen_10))
            Column(modifier = Modifier.weight(1f)) {
                Textview(item.customer_name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                Textview(formatServerDate(item.created_at), size = font_12, color = appColors.textSecondary, maxLines = 1)
            }
            StarRow(item.rating ?: 0, size = dimen_16)
        }
        if (!item.comment.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(dimen_10))
            Textview(item.comment, size = font_14, color = appColors.textPrimary)
        }
        val job = listOfNotNull(item.booking_code, item.crane_code, item.crane_name).joinToString(" · ")
        if (job.isNotBlank()) {
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(job, size = font_12, color = appColors.textSecondary, maxLines = 1)
        }
    }
}
