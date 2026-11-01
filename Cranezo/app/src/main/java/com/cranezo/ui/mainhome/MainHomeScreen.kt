package com.cranezo.ui.mainhome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.mainhome.model.Dest
import com.cranezo.ui.mainhome.model.MainHomeIntent
import com.cranezo.ui.mainhome.model.MainHomeTabs
import com.cranezo.ui.mainhome.model.MainHomeUiState
import com.cranezo.ui.mainhome.model.TabItem
import com.cranezo.ui.mainhome.tabs.CustomerHomeScreen
import com.cranezo.ui.mainhome.tabs.HomeProfileScreen
import com.cranezo.ui.mainhome.tabs.OperatorHomeScreen
import com.cranezo.ui.mainhome.tabs.OwnerHomeScreen
import com.cranezo.ui.mainhome.tabs.TabPlaceholderScreen
import com.cranezo.ui.theme.*

@Preview(showBackground = true)
@Composable
fun MainHomeScreen(vm: MainHomeViewModel? = null) {
    val nav = rememberNavController()
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: MainHomeUiState()
    val onIntent: (MainHomeIntent) -> Unit = { vm?.onIntent(it) }

    // The bottom bar (and the screens behind it) depend on the user's role
    val tabs = MainHomeTabs.forRole(state.role)

    val navigateTo: (String) -> Unit = { route ->
        if (tabs.any { it.dest.route == route }) {
            nav.navigate(route) {
                popUpTo(nav.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    ScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                CustomBottomBar(
                    tabs = tabs,
                    currentRoute = currentRoute,
                    onItemClick = navigateTo
                )
            }
        ) { inner ->
            NavHost(
                modifier = Modifier.padding(inner),
                navController = nav,
                startDestination = Dest.Home.route
            ) {
                tabs.forEach { tab ->
                    composable(tab.dest.route) {
                        when (tab.dest) {
                            Dest.Home -> RoleHomeScreen(state, navigateTo, onIntent)
                            Dest.Profile -> HomeProfileScreen(state, onIntent)
                            else -> TabPlaceholderScreen(stringResource(tab.titleRes), tab.icon)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleHomeScreen(
    state: MainHomeUiState,
    onNavigate: (String) -> Unit,
    onIntent: (MainHomeIntent) -> Unit
) {
    when (state.role) {
        UserRole.CRANE_OWNER -> OwnerHomeScreen(state, onNavigate)
        UserRole.CUSTOMER -> CustomerHomeScreen(state, onNavigate)
        UserRole.OPERATOR -> OperatorHomeScreen(state, onNavigate, onIntent)
    }
}

/**
 * Glass bottom bar. Tabs come from [MainHomeTabs] so every role gets its own set; a tab flagged
 * `isCenterAction` is drawn as the raised round button.
 */
@Composable
fun CustomBottomBar(
    tabs: List<TabItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = dimen_16, vertical = dimen_8)
    ) {
        // Glass is a separate layer so the raised center button can overflow the bar
        Box(
            modifier = Modifier
                .matchParentSize()
                .glassEffect(RoundedCornerShape(dimen_26))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimen_6, vertical = dimen_10),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                BottomBarItem(
                    tab = tab,
                    selected = currentRoute == tab.dest.route,
                    onClick = { onItemClick(tab.dest.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    tab: TabItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected || tab.isCenterAction) AmberPrimary else appColors.textSecondary

    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(dimen_28), contentAlignment = Alignment.Center) {
            if (tab.isCenterAction) {
                Box(
                    modifier = Modifier
                        .requiredSize(dimen_52)
                        .offset(y = -dimen_14)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(LightYellow, DarkYellow))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.titleRes),
                        tint = Color.Black,
                        modifier = Modifier.size(dimen_26)
                    )
                }
            } else {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = stringResource(tab.titleRes),
                    tint = tint,
                    modifier = Modifier.size(dimen_24)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimen_2))

        Textview(
            text = stringResource(tab.titleRes),
            size = font_11,
            color = tint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(dimen_4))

        // Selected indicator
        Box(
            modifier = Modifier
                .width(dimen_16)
                .height(dimen_2)
                .clip(CircleShape)
                .background(if (selected && !tab.isCenterAction) AmberPrimary else Color.Transparent)
        )
    }
}

@Preview(name = "Owner - Dark", showBackground = true, heightDp = 800)
@Composable
private fun MainHomeOwnerDarkPreview() {
    CranezoTheme(darkTheme = true) { MainHomeScreen() }
}

@Preview(name = "Bottom bar (customer) - Light", showBackground = true, heightDp = 160)
@Composable
private fun CustomerBottomBarLightPreview() {
    CranezoTheme(darkTheme = false) {
        ScreenBackground {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                CustomBottomBar(
                    tabs = MainHomeTabs.forRole(UserRole.CUSTOMER),
                    currentRoute = Dest.Home.route,
                    onItemClick = {}
                )
            }
        }
    }
}
