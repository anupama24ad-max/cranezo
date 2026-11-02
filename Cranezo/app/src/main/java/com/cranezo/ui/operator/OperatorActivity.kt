package com.cranezo.ui.operator

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.cranezo.core.composecore.BaseVMComposeActivity
import dagger.hilt.android.AndroidEntryPoint

/** Hosts one job of the operator, from "New Job Request" to "Job Completed". */
@AndroidEntryPoint
class OperatorActivity :
    BaseVMComposeActivity<OperatorJobViewModel>(OperatorJobViewModel::class.java) {

    companion object {
        private const val EXTRA_BOOKING_ID = "booking_id"

        fun intent(context: Context, bookingId: String): Intent =
            Intent(context, OperatorActivity::class.java).putExtra(EXTRA_BOOKING_ID, bookingId)
    }

    @Composable
    override fun Content(vm: OperatorJobViewModel) {
        OperatorJobFlow(
            vm = vm,
            bookingId = intent.getStringExtra(EXTRA_BOOKING_ID).orEmpty(),
            onClose = { finish() }
        )
    }
}
