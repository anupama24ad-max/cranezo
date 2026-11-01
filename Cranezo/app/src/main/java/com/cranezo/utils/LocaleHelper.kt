package com.cranezo.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Languages the app is translated into. [id] is the language id of the backend
 * (1: English, 2: Telugu, 3: Hindi, see registerOrLogin), [tag] the matching locale.
 */
enum class AppLanguage(val id: Int, val tag: String) {
    ENGLISH(1, "en"),
    TELUGU(2, "te"),
    HINDI(3, "hi");

    companion object {
        fun fromId(id: Int): AppLanguage = entries.firstOrNull { it.id == id } ?: ENGLISH
    }
}

/**
 * Applies the language the user chose to the app's resources.
 *
 * The chosen language id is kept in the session (see ChooseLanguageViewModel). Every Activity wraps
 * its base context with [wrap], so `stringResource(...)` and `getString(...)` then return the text
 * of that language (res/values, values-te, values-hi).
 */
object LocaleHelper {

    fun currentLanguage(context: Context): AppLanguage {
        val prefs = context.applicationContext.getSharedPreferences(
            SessionManager.PREFER_NAME,
            SessionManager.PRIVATE_MODE
        )
        return AppLanguage.fromId(
            prefs.getInt(AppStrings.SessionValues.languageId, AppStrings.Constants.defaultLanguageId)
        )
    }

    /** [context] with the chosen language applied. */
    fun wrap(context: Context): Context {
        val locale = Locale.forLanguageTag(currentLanguage(context).tag)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}
