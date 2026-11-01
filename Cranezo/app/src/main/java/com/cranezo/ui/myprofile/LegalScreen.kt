package com.cranezo.ui.myprofile

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.myprofile.components.MyProfileScaffold
import com.cranezo.ui.myprofile.model.LegalContent
import com.cranezo.ui.myprofile.model.LegalSection
import com.cranezo.ui.myprofile.model.MyProfileIntent
import com.cranezo.ui.myprofile.model.MyProfileUiState
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatLongDate

/** Amber number in a circle in front of a section. */
@Composable
private fun NumberBadge(number: Int?) {
    Box(
        modifier = Modifier
            .size(dimen_26)
            .clip(CircleShape)
            .background(AmberPrimary.copy(alpha = 0.14f))
            .border(dimen_1, AmberPrimary.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Textview(
            text = number?.toString().orEmpty(),
            size = font_12,
            color = AmberPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Card on top of both screens: the intro and the "Last Updated" date. */
@Composable
private fun LegalHeaderCard(content: LegalContent, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    GlassCard(contentPadding = dimen_14) {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(icon = icon, accent = AmberPrimary, size = dimen_40)
            Spacer(modifier = Modifier.width(dimen_12))
            Column {
                if (!content.intro.isNullOrBlank()) {
                    Textview(text = content.intro, size = font_13, color = appColors.textPrimary)
                    Spacer(modifier = Modifier.height(dimen_8))
                }
                Textview(
                    text = stringResource(R.string.mp_last_updated).uppercase(),
                    size = font_10,
                    color = appColors.textSecondary
                )
                Textview(
                    text = formatLongDate(content.last_updated),
                    size = font_13,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** [type] is "terms" (numbered list) or "privacy" (accordion). */
@Composable
private fun LegalScreen(
    type: String,
    title: String,
    vm: MyProfileViewModel?,
    state: MyProfileUiState,
    onBack: () -> Unit
) {
    LaunchedEffect(type) { vm?.onIntent(MyProfileIntent.LoadLegal(type)) }

    MyProfileScaffold(title = title, onBack = onBack) {
        val content = state.legal
        if (content == null) return@MyProfileScaffold

        val isPrivacy = type == "privacy"
        LegalHeaderCard(content, if (isPrivacy) Icons.Default.Shield else Icons.Default.Description)

        // The first row of the privacy policy is open when the screen opens
        var expanded by remember(content) { mutableStateOf(if (isPrivacy) 0 else -1) }

        Column(verticalArrangement = Arrangement.spacedBy(dimen_10)) {
            content.sections.orEmpty().forEachIndexed { index, section ->
                if (isPrivacy) {
                    AccordionItem(
                        section = section,
                        expanded = expanded == index,
                        onToggle = { expanded = if (expanded == index) -1 else index }
                    )
                } else {
                    TermsItem(section)
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

@Composable
private fun TermsItem(section: LegalSection) {
    Row(modifier = Modifier.fillMaxWidth()) {
        NumberBadge(section.number)
        Spacer(modifier = Modifier.width(dimen_12))
        Column {
            Textview(
                text = section.title.orEmpty(),
                size = font_14,
                color = appColors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(dimen_2))
            Textview(text = section.body.orEmpty(), size = font_12, color = appColors.textSecondary)
        }
    }
}

@Composable
private fun AccordionItem(section: LegalSection, expanded: Boolean, onToggle: () -> Unit) {
    GlassCard(contentPadding = dimen_12, onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(section.number)
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(
                    text = section.title.orEmpty(),
                    size = font_14,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (!section.subtitle.isNullOrBlank()) {
                    Textview(text = section.subtitle, size = font_11, color = appColors.textSecondary, maxLines = 2)
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = appColors.textSecondary,
                modifier = Modifier.size(dimen_22)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(dimen_10))
                Textview(text = section.body.orEmpty(), size = font_12, color = appColors.textSecondary)
            }
        }
    }
}

private val previewLegal = LegalContent(
    type = "privacy",
    title = "Privacy Policy",
    intro = "We are committed to protecting your personal information.",
    last_updated = "2025-09-15",
    sections = listOf(
        LegalSection(1, "Information We Collect", "Types of information we collect", "Depending on how you use Cranezo..."),
        LegalSection(2, "How We Use Your Information", "Ways we use your data", "We use it to run the app.")
    )
)

@Preview(showBackground = true, heightDp = 800)
@Composable
fun TermsScreen(vm: MyProfileViewModel? = null, onBack: () -> Unit = {}) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value
        ?: MyProfileUiState(legal = previewLegal.copy(type = "terms"))
    LegalScreen("terms", stringResource(R.string.mp_terms), vm, state, onBack)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun PrivacyScreen(vm: MyProfileViewModel? = null, onBack: () -> Unit = {}) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: MyProfileUiState(legal = previewLegal)
    LegalScreen("privacy", stringResource(R.string.mp_privacy), vm, state, onBack)
}
