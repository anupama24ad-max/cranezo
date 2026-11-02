package com.cranezo.ui.myprofile

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.myprofile.model.MyProfileEffect
import dagger.hilt.android.AndroidEntryPoint

/** Screens of My Profile, used as the route of [MyProfileActivity]. */
object ProfileRoutes {
    const val PERSONAL = "personal"
    const val LANGUAGE = "language"
    const val APPEARANCE = "appearance"
    const val NOTIFICATIONS = "notifications"
    const val TERMS = "terms"
    const val PRIVACY = "privacy"
    const val HELP = "help"
    const val DOCUMENTS = "documents"
    const val CRANE = "crane/{id}"

    fun crane(id: String) = "crane/$id"
}

/**
 * Hosts the screens opened from the Profile tab: Personal Information, Language, Appearance,
 * Notification Settings, Terms & Conditions, Privacy Policy, Help & Support and, for operators,
 * Documents and the assigned crane.
 *
 * Result: RESULT_OK means something was saved. [EXTRA_LANGUAGE_CHANGED] says the language changed, so
 * the screen behind restarts to show it.
 */
@AndroidEntryPoint
class MyProfileActivity :
    BaseVMComposeActivity<MyProfileViewModel>(MyProfileViewModel::class.java) {

    companion object {
        private const val EXTRA_ROUTE = "route"
        const val EXTRA_LANGUAGE_CHANGED = "language_changed"

        fun intent(context: Context, route: String): Intent =
            Intent(context, MyProfileActivity::class.java).putExtra(EXTRA_ROUTE, route)
    }

    @Composable
    override fun Content(vm: MyProfileViewModel) {
        val nav = rememberNavController()
        val start = intent.getStringExtra(EXTRA_ROUTE) ?: ProfileRoutes.PERSONAL

        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    MyProfileEffect.Saved -> {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }

                    MyProfileEffect.LanguageSaved -> {
                        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_LANGUAGE_CHANGED, true))
                        finish()
                    }
                }
            }
        }

        // Leaving the first screen closes the activity
        val leave: () -> Unit = { if (!nav.popBackStack()) finish() }
        BackHandler { leave() }

        NavHost(navController = nav, startDestination = start) {
            composable(ProfileRoutes.PERSONAL) { PersonalInfoScreen(vm, leave) }
            composable(ProfileRoutes.LANGUAGE) { ProfileLanguageScreen(vm, leave) }
            composable(ProfileRoutes.APPEARANCE) { AppearanceScreen(vm, leave) }
            composable(ProfileRoutes.NOTIFICATIONS) { NotificationSettingsScreen(vm, leave) }
            composable(ProfileRoutes.TERMS) { TermsScreen(vm, leave) }
            composable(ProfileRoutes.PRIVACY) { PrivacyScreen(vm, leave) }
            composable(ProfileRoutes.HELP) {
                // Help & Support has its own screens now
                LaunchedEffect(Unit) {
                    startActivity(com.cranezo.ui.support.SupportActivity.intent(this@MyProfileActivity, com.cranezo.ui.support.SupportRoutes.HELP))
                    finish()
                }
            }
            composable(ProfileRoutes.DOCUMENTS) { OperatorDocumentsScreen(vm, leave) }
            composable(
                ProfileRoutes.CRANE,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                AssignedCraneScreen(entry.arguments?.getString("id").orEmpty(), vm, leave)
            }
        }
    }
}
