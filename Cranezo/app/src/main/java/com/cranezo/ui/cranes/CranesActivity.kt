package com.cranezo.ui.cranes

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.cranes.model.CranesEffect
import com.cranezo.ui.cranes.model.CranesIntent
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hosts the crane screens of the owner: Add Crane, Crane Submitted, Crane Details, Edit Crane and
 * Upload Document. Opened from My Cranes, either to add a crane or to show one.
 *
 * Result: RESULT_OK means a crane was added or changed, so the list behind it should reload.
 */
@AndroidEntryPoint
class CranesActivity :
    BaseVMComposeActivity<CranesViewModel>(CranesViewModel::class.java) {

    companion object {
        private const val EXTRA_CRANE_ID = "crane_id"

        /** Opens the Add Crane form. */
        fun addIntent(context: Context) = Intent(context, CranesActivity::class.java)

        /** Opens the details of one crane. */
        fun detailsIntent(context: Context, craneId: String) =
            Intent(context, CranesActivity::class.java).putExtra(EXTRA_CRANE_ID, craneId)

        private const val ADD = "add"
        private const val SUBMITTED = "submitted"
        private const val DETAILS = "details/{id}"
        private const val EDIT = "edit/{id}"
        private const val DOCUMENTS = "documents/{id}"
        private fun details(id: String) = "details/$id"
        private fun edit(id: String) = "edit/$id"
        private fun documents(id: String) = "documents/$id"
    }

    @Composable
    override fun Content(vm: CranesViewModel) {
        val nav = rememberNavController()
        val state by vm.uiState.collectAsStateWithLifecycle()
        val startCraneId = remember { intent.getStringExtra(EXTRA_CRANE_ID) }
        var operatorAdded by remember { mutableIntStateOf(0) }

        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    CranesEffect.NavigateToSubmitted -> {
                        // The list behind this screen has a new crane
                        setResult(Activity.RESULT_OK)
                        nav.navigate(SUBMITTED) { popUpTo(ADD) { inclusive = true } }
                    }

                    CranesEffect.NavigateBackToDetails -> {
                        setResult(Activity.RESULT_OK)
                        nav.popBackStack(DETAILS, inclusive = false)
                    }

                    CranesEffect.OperatorAdded -> operatorAdded++
                }
            }
        }

        // Leaving the first screen closes the activity
        val leave: () -> Unit = { if (!nav.popBackStack()) finish() }
        BackHandler { leave() }

        NavHost(
            navController = nav,
            startDestination = if (startCraneId != null) details(startCraneId) else ADD
        ) {
            composable(ADD) {
                LaunchedEffect(Unit) { vm.onIntent(CranesIntent.StartAdd) }
                CraneFormScreen(state, vm::onIntent, onBack = leave, operatorAddedSignal = operatorAdded)
            }

            composable(SUBMITTED) {
                CraneSubmittedScreen(
                    state = state,
                    onViewDetails = {
                        state.submitted?.id?.let { id ->
                            nav.navigate(details(id)) { popUpTo(SUBMITTED) { inclusive = true } }
                        }
                    },
                    onBackToList = { finish() }
                )
            }

            composable(DETAILS, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                CraneDetailsScreen(
                    craneId = id,
                    state = state,
                    onIntent = vm::onIntent,
                    onBack = { finish() },
                    onEdit = { crane ->
                        vm.onIntent(CranesIntent.StartEdit(crane))
                        nav.navigate(edit(id))
                    },
                    onManageDocuments = { nav.navigate(documents(id)) }
                )
            }

            composable(EDIT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                CraneFormScreen(
                    state = state,
                    onIntent = vm::onIntent,
                    onBack = leave,
                    onManageDocuments = { nav.navigate(documents(id)) },
                    operatorAddedSignal = operatorAdded
                )
            }

            composable(DOCUMENTS, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                CraneDocumentsScreen(state = state, onIntent = vm::onIntent, onBack = leave)
            }
        }
    }
}
