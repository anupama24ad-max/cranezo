package com.cranezo.ui.mainhome.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Work
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.cranezo.R
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.theme.AppThemeMode
import com.cranezo.ui.cranes.model.Crane

// ---------------------------------------------------------------------------------------------
// Navigation
// ---------------------------------------------------------------------------------------------

sealed class Dest(val route: String) {
    object Home : Dest("home")
    object MyCranes : Dest("my_cranes")
    object Jobs : Dest("jobs")
    object Earnings : Dest("earnings")
    object Help : Dest("help")
    object Hire : Dest("hire")
    object BookCrane : Dest("book_crane")
    object Profile : Dest("profile")
}

/**
 * One bottom bar entry. [isCenterAction] draws the item as the raised round button in the middle.
 */
data class TabItem(
    val dest: Dest,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val isCenterAction: Boolean = false
)

object MainHomeTabs {
    /** The bottom bar of every role. */
    fun forRole(role: UserRole): List<TabItem> = when (role) {
        UserRole.CRANE_OWNER -> listOf(
            TabItem(Dest.Home, R.string.tab_home, Icons.Default.Home),
            TabItem(Dest.MyCranes, R.string.tab_my_cranes, Icons.Default.PrecisionManufacturing),
            TabItem(Dest.Jobs, R.string.tab_jobs, Icons.Default.Work),
            TabItem(Dest.Earnings, R.string.tab_earnings, Icons.Default.BarChart),
            TabItem(Dest.Profile, R.string.tab_profile, Icons.Default.Person)
        )

        UserRole.CUSTOMER -> listOf(
            TabItem(Dest.Home, R.string.tab_home, Icons.Default.Home),
            TabItem(Dest.Jobs, R.string.tab_my_jobs, Icons.Default.Work),
            TabItem(Dest.BookCrane, R.string.tab_book_crane, Icons.Default.PrecisionManufacturing, isCenterAction = true),
            TabItem(Dest.Hire, R.string.tab_hire, Icons.Default.PersonSearch),
            TabItem(Dest.Profile, R.string.tab_profile, Icons.Default.Person)
        )

        UserRole.OPERATOR -> listOf(
            TabItem(Dest.Home, R.string.tab_home, Icons.Default.Home),
            TabItem(Dest.Jobs, R.string.tab_jobs, Icons.Default.Work),
            TabItem(Dest.Earnings, R.string.tab_earnings, Icons.Default.BarChart),
            TabItem(Dest.Help, R.string.tab_help, Icons.Default.SupportAgent),
            TabItem(Dest.Profile, R.string.tab_profile, Icons.Default.Person)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Home data
// There are no home / dashboard APIs yet, so these defaults are the sample values from the design.
// Replace them with API data in MainHomeViewModel when the endpoints exist.
// ---------------------------------------------------------------------------------------------

data class FleetOverview(
    val totalCranes: Int = 5,
    val availableCranes: Int = 3,
    val busyCranes: Int = 2,
    val pendingVerification: Int = 1
)

data class OwnerHomeData(
    val fleet: FleetOverview = FleetOverview(),
    val todaysJobs: Int = 3,
    val todaysEarnings: String = "₹24,500"
)

data class BookingItem(
    val bookingId: String,
    val craneName: String,
    val location: String,
    val date: String,
    val amount: String,
    val status: String
)

data class CustomerHomeData(
    val recentBooking: BookingItem? = BookingItem(
        bookingId = "CRN-100045",
        craneName = "50T Mobile Crane",
        location = "Construction Site, Kakinada",
        date = "05 Jun 2026",
        amount = "₹32,500",
        status = "Completed"
    )
)

data class OperatorHomeData(
    val craneName: String = "Tata 25 Ton",
    val craneNumber: String = "AP 05 XY 1234",
    val craneType: String = "Mobile Crane",
    val ownerName: String = "ABC Crane Services",
    val craneStatus: String = "Available",
    val jobClient: String = "Sai Constructions",
    val jobLocation: String = "Kakinada",
    val jobTime: String = "10:00 AM · 8 Hours",
    val jobDay: String = "10",
    val jobMonth: String = "JUN",
    val jobStatus: String = "Assigned",
    val todaysEarnings: String = "₹4,500",
    val documentStatus: String = "All documents verified"
)

// ---------------------------------------------------------------------------------------------
// MVI contract
// ---------------------------------------------------------------------------------------------

data class MainHomeUiState(
    val role: UserRole = UserRole.CRANE_OWNER,
    val userName: String = "",
    val memberId: String = "",
    val mobileNumber: String = "",
    val isOnline: Boolean = false,
    val owner: OwnerHomeData = OwnerHomeData(),
    val customer: CustomerHomeData = CustomerHomeData(),
    val operator: OperatorHomeData = OperatorHomeData()
)

/** The owner's My Cranes list, loaded page by page from POST /owner/getCranes. */
data class MyCranesState(
    val items: List<Crane> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    // min of the next page: the max of the page before
    val nextMin: Int = 0,
    val loaded: Boolean = false,
    val error: String? = null
)

sealed interface MainHomeIntent {
    data object RefreshCranes : MainHomeIntent
    data object RefreshProfile : MainHomeIntent
    data object LoadMoreCranes : MainHomeIntent
    data object ToggleOnline : MainHomeIntent
    data class ThemeSelected(val mode: AppThemeMode) : MainHomeIntent
    data object Logout : MainHomeIntent
}

sealed interface MainHomeEffect {
    data object NavigateToLogin : MainHomeEffect
    // The language was changed on another device and applied here: restart to show it
    data object LanguageChanged : MainHomeEffect
}
