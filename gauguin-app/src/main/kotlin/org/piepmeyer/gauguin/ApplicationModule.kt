package org.piepmeyer.gauguin

import android.content.SharedPreferences
import org.koin.core.module.Module
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.piepmeyer.gauguin.preferences.ApplicationPreferences
import org.piepmeyer.gauguin.preferences.ApplicationPreferencesImpl
import org.piepmeyer.gauguin.preferences.StatisticsManager
import org.piepmeyer.gauguin.preferences.StatisticsManagerImpl
import org.piepmeyer.gauguin.ui.ActivityUtils
import java.io.File

class ApplicationModule(
    private val filesDir: File,
    private val statisticsPreferences: SharedPreferences,
    private val applicationPreferences: ApplicationPreferencesImpl,
) {
    fun module(): Module =
        module {
            single {
                applicationPreferences
            } withOptions { binds(listOf(ApplicationPreferences::class)) }
            single {
                StatisticsManagerImpl(
                    filesDir,
                    statisticsPreferences,
                )
            } withOptions {
                binds(listOf(StatisticsManager::class))
                createdAtStart()
            }
            single { ActivityUtils() }
        }
}
