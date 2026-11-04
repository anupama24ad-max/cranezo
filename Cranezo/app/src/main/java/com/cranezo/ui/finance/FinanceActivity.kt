package com.cranezo.ui.finance

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cranezo.core.composecore.BaseVMComposeActivity
import dagger.hilt.android.AndroidEntryPoint

object FinanceRoutes {
    const val HUB = "hub"
    // owner
    const val BILLING = "billing"
    const val COMMISSION = "commission"
    const val OPERATOR_DUES = "operator_dues"
    const val PAY_OPERATOR = "pay_operator"
    const val PLATFORM = "platform"
    const val JOB_REQUESTS = "job_requests"
    // Rapido-style customer requests near the owner's cranes
    const val CRANE_REQUESTS = "crane_requests"
    // owner and customer
    const val TRANSACTIONS = "transactions"
    const val COMPLAINTS = "complaints"
    const val COMPLAINT = "complaint"
    const val COMPLAINT_NEW = "complaint_new"
    // operator
    const val FIND_JOBS = "find_jobs"
    const val MY_REQUESTS = "my_requests"
    const val MY_PAYMENTS = "my_payments"
}

/**
 * Payments, commission, settlements, operator job requests and complaints for every role.
 * Plain pages with sections and buttons; nothing opens as a popup.
 */
@AndroidEntryPoint
class FinanceActivity : BaseVMComposeActivity<FinanceViewModel>(FinanceViewModel::class.java) {

    companion object {
        private const val EXTRA_ROUTE = "route"
        private const val EXTRA_ID = "id"

        /** [id] is the booking id for COMMISSION and COMPLAINT_NEW, the complaint id for COMPLAINT */
        fun intent(context: Context, route: String = FinanceRoutes.HUB, id: String? = null): Intent =
            Intent(context, FinanceActivity::class.java).putExtra(EXTRA_ROUTE, route).putExtra(EXTRA_ID, id)
    }

    @Composable
    override fun Content(vm: FinanceViewModel) {
        val nav = rememberNavController()
        val start = intent.getStringExtra(EXTRA_ROUTE) ?: FinanceRoutes.HUB
        val startId = intent.getStringExtra(EXTRA_ID)
        var operatorId by rememberSaveable { mutableStateOf("") }
        var operatorName by rememberSaveable { mutableStateOf("") }
        var complaintId by rememberSaveable { mutableStateOf(if (start == FinanceRoutes.COMPLAINT) startId.orEmpty() else "") }
        val role = vm.role
        val isOwner = role == "CRANE_OWNER"
        val dial = rememberDialer()

        val leave: () -> Unit = {
            vm.clearNotice()
            if (!nav.popBackStack()) finish()
        }
        BackHandler { leave() }
        val open: (String) -> Unit = { route ->
            vm.clearNotice()
            nav.navigate(route)
        }

        NavHost(navController = nav, startDestination = start) {
            composable(FinanceRoutes.HUB) { FinanceHubScreen(role, leave, open) }

            composable(FinanceRoutes.BILLING) { OwnerBillingScreen(vm, leave) }
            composable(FinanceRoutes.COMMISSION) { OwnerCommissionScreen(vm, startId.orEmpty(), leave) }
            composable(FinanceRoutes.OPERATOR_DUES) {
                OperatorDuesScreen(vm, leave) { id, name ->
                    operatorId = id
                    operatorName = name
                    open(FinanceRoutes.PAY_OPERATOR)
                }
            }
            composable(FinanceRoutes.PAY_OPERATOR) {
                // after paying, go back to the list, which shows the "payment recorded" notice
                PayOperatorScreen(vm, operatorId, operatorName, leave, onPaid = { nav.popBackStack() })
            }
            composable(FinanceRoutes.PLATFORM) { OwnerPlatformScreen(vm, leave) }
            composable(FinanceRoutes.JOB_REQUESTS) { OwnerJobRequestsScreen(vm, leave, dial) }
            composable(FinanceRoutes.CRANE_REQUESTS) {
                val requestVm = androidx.compose.runtime.remember {
                    androidx.lifecycle.ViewModelProvider(this@FinanceActivity)[com.cranezo.ui.booking.request.RequestViewModel::class.java]
                }
                com.cranezo.ui.booking.request.OwnerCraneRequestsScreen(
                    requestVm, leave,
                    // the job is mine: open it (it waits for the customer's advance)
                    onAccepted = { bookingId ->
                        startActivity(com.cranezo.ui.ownerjobs.OwnerJobActivity.intent(this@FinanceActivity, bookingId))
                    }
                )
            }

            composable(FinanceRoutes.TRANSACTIONS) { TransactionsScreen(vm, leave) }
            composable(FinanceRoutes.COMPLAINTS) {
                ComplaintsScreen(
                    vm, leave,
                    onOpen = { id ->
                        complaintId = id
                        open(FinanceRoutes.COMPLAINT)
                    },
                    onNew = if (isOwner) null else ({ open(FinanceRoutes.COMPLAINT_NEW) })
                )
            }
            composable(FinanceRoutes.COMPLAINT) { ComplaintDetailScreen(vm, complaintId, isOwner, leave) }
            composable(FinanceRoutes.COMPLAINT_NEW) {
                NewComplaintScreen(
                    vm, presetBookingId = if (start == FinanceRoutes.COMPLAINT_NEW) startId else null, onBack = leave,
                    onCreated = { id ->
                        complaintId = id
                        nav.navigate(FinanceRoutes.COMPLAINT) { popUpTo(FinanceRoutes.COMPLAINT_NEW) { inclusive = true } }
                    }
                )
            }

            composable(FinanceRoutes.FIND_JOBS) { FindJobsScreen(vm, leave) }
            composable(FinanceRoutes.MY_REQUESTS) { MyRequestsScreen(vm, leave) }
            composable(FinanceRoutes.MY_PAYMENTS) { MyPaymentsScreen(vm, leave) }
        }
    }
}
