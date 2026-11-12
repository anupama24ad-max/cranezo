package com.cranezo.ui.mainhome

import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
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
import androidx.compose.runtime.LaunchedEffect
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
import com.cranezo.ui.booking.BookingActivity
import com.cranezo.ui.mainhome.components.OwnerRequestHost
import com.cranezo.ui.booking.BookingRoutes
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.mainhome.model.Dest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.cranezo.ui.cranes.CranesActivity
import com.cranezo.ui.cranes.MyCranesScreen
import com.cranezo.ui.myprofile.MyProfileActivity
import com.cranezo.ui.myprofile.ProfileRoutes
import com.cranezo.ui.operator.OperatorActivity
import com.cranezo.ui.ownerjobs.OwnerEarningsTab
import com.cranezo.ui.ownerjobs.OwnerEarningsViewModel
import com.cranezo.ui.ownerjobs.OwnerJobActivity
import com.cranezo.ui.ownerjobs.OwnerJobsTab
import com.cranezo.ui.ownerjobs.OwnerJobsViewModel
import com.cranezo.ui.operator.OperatorEarningsTab
import com.cranezo.ui.operator.OperatorHomeTab
import com.cranezo.ui.operator.OperatorJobsTab
import com.cranezo.ui.operator.OperatorTabsViewModel
import com.cranezo.ui.operator.model.OperatorTabsIntent
import com.cranezo.ui.ownerjobs.model.OwnerEarningsIntent
import com.cranezo.ui.ownerjobs.model.OwnerJobsIntent
import com.cranezo.ui.mainhome.model.MyCranesState
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
    val cranes = vm?.cranesState?.collectAsStateWithLifecycle()?.value ?: MyCranesState()
    val profile = vm?.profile?.collectAsStateWithLifecycle()?.value
    // owner home numbers (today's jobs, requests, jobs needing an operator) update when a push arrives
    // Every push moves what is on screen: the screens that show the thing the push is about reload by its type
    com.cranezo.push.OnPush { data ->
        val type = data["type"].orEmpty()
        if (state.role == com.cranezo.ui.chooserole.UserRole.CRANE_OWNER) onIntent(MainHomeIntent.RefreshOwnerHome)
        // the admin approved or rejected a crane: My Cranes and its numbers change
        if (type in setOf("crane_approved", "crane_rejected", "operator_declined", "job_accepted")) onIntent(MainHomeIntent.RefreshCranes)
        // documents or the account were reviewed: Profile and the verified badges change
        if (type in setOf("document_verified", "document_rejected", "operator_verified", "account_status", "commission_updated", "subscription_due", "subscription_paid")) {
            onIntent(MainHomeIntent.RefreshProfile)
        }
    }

    // Coming back to the app: reload the tab on view, so nothing shows old numbers until the app is closed
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        if (state.role == com.cranezo.ui.chooserole.UserRole.CRANE_OWNER) onIntent(MainHomeIntent.RefreshOwnerHome)
        onIntent(MainHomeIntent.RefreshProfile)
    }

    // Add Crane and Crane Details open in their own screen; reload the list if something changed there
    val context = LocalContext.current
    val cranesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) onIntent(MainHomeIntent.RefreshCranes)
    }
    val addCrane: () -> Unit = { cranesLauncher.launch(CranesActivity.addIntent(context)) }

    // My Profile screens. A language change restarts this screen so it is shown in the new language.
    val profileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.data?.getBooleanExtra(MyProfileActivity.EXTRA_LANGUAGE_CHANGED, false) == true) {
            (context as? Activity)?.recreate()
        }
    }
    val openProfile: (String) -> Unit = { route -> profileLauncher.launch(MyProfileActivity.intent(context, route)) }

    // Operator: Home, Jobs and Earnings come from the Operator Jobs APIs; one job opens in its own screen
    val opVm: OperatorTabsViewModel? = (context as? androidx.activity.ComponentActivity)
        ?.takeIf { state.role == UserRole.OPERATOR }
        ?.let { activity -> remember { androidx.lifecycle.ViewModelProvider(activity)[OperatorTabsViewModel::class.java] } }
    val openJob: (String) -> Unit = { id -> context.startActivity(OperatorActivity.intent(context, id)) }

    // Owner: Jobs and Earnings tabs; a job opens in its own screen (details or live location)
    val ownerActivity = (context as? androidx.activity.ComponentActivity)?.takeIf { state.role == UserRole.CRANE_OWNER }
    val ownerJobsVm: OwnerJobsViewModel? = ownerActivity?.let { a -> remember { androidx.lifecycle.ViewModelProvider(a)[OwnerJobsViewModel::class.java] } }
    val ownerEarningsVm: OwnerEarningsViewModel? = ownerActivity?.let { a -> remember { androidx.lifecycle.ViewModelProvider(a)[OwnerEarningsViewModel::class.java] } }
    // Customer: Home is driven by POST /home/customer
    val customerHomeVm: com.cranezo.ui.customerhome.CustomerHomeViewModel? = (context as? androidx.activity.ComponentActivity)
        ?.takeIf { state.role == UserRole.CUSTOMER }
        ?.let { a -> remember { androidx.lifecycle.ViewModelProvider(a)[com.cranezo.ui.customerhome.CustomerHomeViewModel::class.java] } }
    val openOwnerJob: (String) -> Unit = { id -> context.startActivity(OwnerJobActivity.intent(context, id)) }
    val openOwnerLive: (String) -> Unit = { id -> context.startActivity(OwnerJobActivity.intent(context, id, live = true)) }
    // A new job for the operator shows as a card at the top of the Home tab (OperatorHomeTab), not as a popup

    // The bottom bar (and the screens behind it) depend on the user's role
    val tabs = MainHomeTabs.forRole(state.role)

    val navigateTo: (String) -> Unit = { route ->
        if (state.role == UserRole.CUSTOMER && route == Dest.BookCrane.route) {
            context.startActivity(BookingActivity.intent(context))
        } else if (route == Dest.Help.route) {
            context.startActivity(com.cranezo.ui.support.SupportActivity.intent(context, com.cranezo.ui.support.SupportRoutes.HELP))
        } else if (state.role == UserRole.CUSTOMER && route == Dest.Track.route) {
            context.startActivity(BookingActivity.intent(context, BookingRoutes.ACTIVE_TRACKING))
        } else if (state.role == UserRole.CUSTOMER && route == Dest.Jobs.route) {
            context.startActivity(BookingActivity.intent(context, BookingRoutes.BOOKINGS))
        } else if (tabs.any { it.dest.route == route }) {
            nav.navigate(route) {
                popUpTo(nav.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Owner: "New Crane Request" popup and "Booking Accepted"
    OwnerRequestHost(
        request = state.request,
        accepted = state.requestAccepted,
        onAccept = { onIntent(MainHomeIntent.AcceptRequest) },
        onDecline = { onIntent(MainHomeIntent.DeclineRequest) },
        onDismiss = { onIntent(MainHomeIntent.DismissRequest) }
    )

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
            // Home draws its navy header behind the status bar, so it skips the top inset
            val layoutDir = androidx.compose.ui.platform.LocalLayoutDirection.current
            val contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = inner.calculateStartPadding(layoutDir),
                end = inner.calculateEndPadding(layoutDir),
                top = if (currentRoute == Dest.Home.route) androidx.compose.ui.unit.Dp(0f) else inner.calculateTopPadding(),
                bottom = inner.calculateBottomPadding()
            )
            NavHost(
                modifier = Modifier.padding(contentPadding),
                navController = nav,
                startDestination = Dest.Home.route
            ) {
                tabs.forEach { tab ->
                    composable(tab.dest.route) {
                        // Fresh data every time a tab is opened (not only when the app starts)
                        LaunchedEffect(Unit) {
                            when (tab.dest) {
                                Dest.Home -> when {
                                    customerHomeVm != null -> customerHomeVm.refresh()
                                    opVm == null && state.role == UserRole.CRANE_OWNER -> onIntent(MainHomeIntent.RefreshOwnerHome)
                                    else -> Unit
                                }
                                Dest.MyCranes -> onIntent(MainHomeIntent.RefreshCranes)
                                // Jobs, Earnings and Profile used to keep what they loaded first until the app was closed
                                Dest.Jobs -> when {
                                    opVm != null -> opVm.onIntent(OperatorTabsIntent.LoadJobs(opVm.uiState.value.jobsTab))
                                    ownerJobsVm != null -> ownerJobsVm.onIntent(OwnerJobsIntent.Refresh)
                                    else -> Unit
                                }
                                Dest.Earnings -> when {
                                    opVm != null -> opVm.onIntent(OperatorTabsIntent.LoadEarnings)
                                    ownerEarningsVm != null -> ownerEarningsVm.onIntent(OwnerEarningsIntent.Load)
                                    else -> Unit
                                }
                                Dest.Profile -> onIntent(MainHomeIntent.RefreshProfile)
                                else -> Unit
                            }
                        }
                        when (tab.dest) {
                            Dest.Home -> if (opVm != null) OperatorHomeTab(
                                vm = opVm,
                                onOpenJob = openJob,
                                onOpenJobs = { navigateTo(Dest.Jobs.route) },
                                onOpenEarnings = { navigateTo(Dest.Earnings.route) },
                                onOpenDocuments = { openProfile(ProfileRoutes.DOCUMENTS) },
                                onOpenCrane = { id -> openProfile(ProfileRoutes.crane(id)) }
                            ) else if (customerHomeVm != null) com.cranezo.ui.customerhome.CustomerHomeTab(customerHomeVm, onProfile = { navigateTo(Dest.Profile.route) })
                            else RoleHomeScreen(state, navigateTo, onIntent, addCrane)
                            Dest.Jobs -> if (opVm != null) OperatorJobsTab(opVm, openJob)
                            else if (ownerJobsVm != null) OwnerJobsTab(ownerJobsVm, state.userName, openOwnerJob, openOwnerLive)
                            else TabPlaceholderScreen(stringResource(tab.titleRes), tab.icon)
                            Dest.Earnings -> if (opVm != null) OperatorEarningsTab(opVm, openJob)
                            else if (ownerEarningsVm != null) OwnerEarningsTab(ownerEarningsVm, state.userName, openOwnerJob)
                            else TabPlaceholderScreen(stringResource(tab.titleRes), tab.icon)
                            Dest.MyCranes -> MyCranesScreen(
                                state = cranes,
                                onAddCrane = addCrane,
                                onOpenCrane = { crane ->
                                    crane.id?.let { cranesLauncher.launch(CranesActivity.detailsIntent(context, it)) }
                                },
                                onRefresh = { onIntent(MainHomeIntent.RefreshCranes) },
                                onLoadMore = { onIntent(MainHomeIntent.LoadMoreCranes) }
                            )
                            Dest.Profile -> HomeProfileScreen(state, profile, onIntent, openProfile)
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
    onIntent: (MainHomeIntent) -> Unit,
    onAddCrane: () -> Unit
) {
    when (state.role) {
        UserRole.CRANE_OWNER -> OwnerHomeScreen(state, onNavigate, onAddCrane)
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
            .padding(horizontal = dimen_20, vertical = dimen_12)
    ) {
        // Glass is a separate layer so the raised center button can overflow the bar
        Box(
            modifier = Modifier
                .matchParentSize()
                .glassEffect(RoundedCornerShape(dimen_26), inset = true)
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
