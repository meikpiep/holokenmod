package org.piepmeyer.gauguin.ui

import android.app.Activity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.piepmeyer.gauguin.NightMode
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.Theme
import org.piepmeyer.gauguin.preferences.ApplicationPreferences

class ActivityUtils : KoinComponent {
    private val applicationPreferences: ApplicationPreferences by inject()

    @Suppress("DEPRECATION")
    fun configureFullscreen(activity: Activity) {
        if (!applicationPreferences.showFullscreen()) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    fun configureKeepScreenOn(activity: Activity) {
        if (applicationPreferences.keepScreenOn()) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun configureTheme(activity: Activity) {
        when (applicationPreferences.theme) {
            Theme.GAUGUIN -> {
                activity.setTheme(R.style.AppTheme)
            }
            Theme.DYNAMIC_COLORS -> {
                activity.setTheme(com.google.android.material.R.style.Theme_Material3_DynamicColors_DayNight_NoActionBar)
            }
            Theme.MONOCHROME -> {
                activity.setTheme(R.style.AppThemeMonochrome)
            }
        }

        when (applicationPreferences.nightMode) {
            NightMode.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            NightMode.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            NightMode.SYSTEM_DEFAULT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    fun reconfigureTheme(activity: Activity) {
        configureTheme(activity)

        activity.recreate()
    }
}
