package com.cranezo.ui.support

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.booking.BookingActivity
import com.cranezo.ui.operator.OperatorActivity
import com.cranezo.ui.ownerjobs.OwnerJobActivity
import com.cranezo.ui.support.model.SupportEffect
import com.cranezo.ui.support.model.SupportIntent
import com.cranezo.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint

object SupportRoutes {
    const val NOTIFICATIONS = "notifications"
    const val HELP = "help"
    const val FAQ = "faq"
    const val CREATE = "create"
    const val TICKETS = "tickets"
    const val TICKET = "ticket"
    const val CHAT = "chat"
    const val ANNOUNCEMENT = "announcement"
}

/** Hosts Notifications and Help & Support (FAQs, requests, chat, emergency). The same for every role. */
@AndroidEntryPoint
class SupportActivity :
    BaseVMComposeActivity<SupportViewModel>(SupportViewModel::class.java) {

    companion object {
        private const val EXTRA_ROUTE = "route"

        fun intent(context: Context, route: String): Intent =
            Intent(context, SupportActivity::class.java).putExtra(EXTRA_ROUTE, route)
    }

    @Composable
    override fun Content(vm: SupportViewModel) {
        val nav = rememberNavController()
        val start = intent.getStringExtra(EXTRA_ROUTE) ?: SupportRoutes.HELP
        var faqCategory by rememberSaveable { mutableStateOf<String?>(null) }
        var faqTitle by rememberSaveable { mutableStateOf("") }
        var createCategory by rememberSaveable { mutableStateOf("issue") }
        val role = SessionManager(this).getUserRole()

        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    is SupportEffect.Dial -> dialNumber(this@SupportActivity, effect.number)
                    is SupportEffect.Go -> nav.navigate(effect.route)
                    // The notification's own screen, by role
                    is SupportEffect.OpenTarget -> {
                        val target = effect.item.target
                        val bookingId = target?.booking_id
                        when (target?.screen) {
                            "booking", "track", "receipt", "rate" -> bookingId?.let { id ->
                                startActivity(
                                    when (role) {
                                        "CRANE_OWNER" -> OwnerJobActivity.intent(this@SupportActivity, id)
                                        "OPERATOR" -> OperatorActivity.intent(this@SupportActivity, id)
                                        else -> BookingActivity.bookingIntent(this@SupportActivity, id)
                                    }
                                )
                            }
                            "owner_job" -> bookingId?.let { startActivity(OwnerJobActivity.intent(this@SupportActivity, it)) }
                            "operator_job" -> bookingId?.let { startActivity(OperatorActivity.intent(this@SupportActivity, it)) }
                            // The request popup and tab live on the home screen
                            "owner_request" -> finish()
                            "owner_crane_requests" -> startActivity(
                                com.cranezo.ui.finance.FinanceActivity.intent(this@SupportActivity, com.cranezo.ui.finance.FinanceRoutes.CRANE_REQUESTS)
                            )
                            "crane_request" -> startActivity(
                                BookingActivity.intent(this@SupportActivity, com.cranezo.ui.booking.BookingRoutes.REQUEST)
                            )
                            "announcement" -> nav.navigate(SupportRoutes.ANNOUNCEMENT)
                            "support_ticket" -> target.ticket_id?.let {
                                vm.onIntent(SupportIntent.OpenTicket(it))
                                nav.navigate(SupportRoutes.TICKET)
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }

        val leave: () -> Unit = { if (!nav.popBackStack()) finish() }
        BackHandler { leave() }

        NavHost(navController = nav, startDestination = start) {
            composable(SupportRoutes.NOTIFICATIONS) { NotificationsScreen(vm, leave) }
            composable(SupportRoutes.ANNOUNCEMENT) { AnnouncementScreen(vm, leave) }
            composable(SupportRoutes.HELP) {
                HelpSupportHomeScreen(
                    vm,
                    onBack = leave,
                    onSearch = {
                        faqCategory = null
                        faqTitle = ""
                        nav.navigate(SupportRoutes.FAQ)
                    },
                    onTile = { tile ->
                        when (tile.action) {
                            "chat" -> {
                                vm.onIntent(SupportIntent.OpenChat)
                                nav.navigate(SupportRoutes.CHAT)
                            }
                            "category" -> {
                                faqCategory = tile.category
                                faqTitle = tile.title.orEmpty()
                                createCategory = tile.category ?: "general"
                                nav.navigate(SupportRoutes.FAQ)
                            }
                            "ticket" -> {
                                createCategory = tile.category ?: "issue"
                                vm.onIntent(SupportIntent.ClearAttachments)
                                nav.navigate(SupportRoutes.CREATE)
                            }
                            "faq" -> {
                                faqCategory = null
                                faqTitle = tile.title.orEmpty()
                                nav.navigate(SupportRoutes.FAQ)
                            }
                        }
                    },
                    onMyRequests = { nav.navigate(SupportRoutes.TICKETS) }
                )
            }
            composable(SupportRoutes.FAQ) {
                FaqScreen(
                    vm, category = faqCategory, title = faqTitle, onBack = leave,
                    onContact = {
                        if (faqCategory == null) createCategory = "general"
                        vm.onIntent(SupportIntent.ClearAttachments)
                        nav.navigate(SupportRoutes.CREATE)
                    }
                )
            }
            composable(SupportRoutes.CREATE) {
                CreateTicketScreen(
                    vm, createCategory, onBack = leave,
                    onViewRequest = { id ->
                        vm.onIntent(SupportIntent.OpenTicket(id))
                        nav.navigate(SupportRoutes.TICKET) { popUpTo(SupportRoutes.CREATE) { inclusive = true } }
                    }
                )
            }
            composable(SupportRoutes.TICKETS) {
                TicketsScreen(vm, onBack = leave, onOpen = { id ->
                    vm.onIntent(SupportIntent.OpenTicket(id))
                    nav.navigate(SupportRoutes.TICKET)
                })
            }
            composable(SupportRoutes.TICKET) {
                ConversationScreen(vm, chat = false, onBack = leave, onNewRequest = {
                    createCategory = "general"
                    nav.navigate(SupportRoutes.CREATE)
                })
            }
            composable(SupportRoutes.CHAT) { ConversationScreen(vm, chat = true, onBack = leave) }
        }
    }
}
