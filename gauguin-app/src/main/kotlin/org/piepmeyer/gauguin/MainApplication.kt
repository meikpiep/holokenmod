package org.piepmeyer.gauguin

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.piepmeyer.gauguin.preferences.ApplicationPreferencesImpl
import org.piepmeyer.gauguin.ui.ActivityUtils
import org.piepmeyer.gauguin.ui.DynamicColorsPrecondition

private val logger = KotlinLogging.logger {}

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        logger.info { "Starting application Gauguin..." }

        val applicationPreferences = ApplicationPreferencesImpl(this)

        val koinApplication =
            startKoin {
                androidLogger()
                androidContext(this@MainApplication)

                val statisticsPreferences = getSharedPreferences("stats", Context.MODE_PRIVATE)

                applicationPreferences.migrateGridSizeFromTwoToThree()

                modules(
                    CoreModule(filesDir).module(),
                    ApplicationModule(filesDir, statisticsPreferences, applicationPreferences).module(),
                )
            }

        val options =
            DynamicColorsOptions
                .Builder()
                .setThemeOverlay(R.style.AppTheme_Overlay)
                .setPrecondition(DynamicColorsPrecondition())
                .build()

        DynamicColors.applyToActivitiesIfAvailable(this, options)

        val activityUtils = get<ActivityUtils>()
        activityUtils.configureNightMode()

        logger.info {
            "Gauguin application started successfully, " +
                "version ${resources.getString(R.string.versionName)}, " +
                "debug flag ${resources.getBoolean(R.bool.debuggable)}."
        }
    }
}
