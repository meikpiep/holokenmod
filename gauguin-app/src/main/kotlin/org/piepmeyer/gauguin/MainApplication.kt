package org.piepmeyer.gauguin

import android.app.Application
import androidx.preference.PreferenceManager
import org.piepmeyer.gauguin.options.ApplicationPreferences
import org.piepmeyer.gauguin.options.ApplicationPreferencesImpl
import org.piepmeyer.gauguin.ui.ActivityUtils
import org.piepmeyer.gauguin.ui.grid.GridCellSizeService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

class MainApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidLogger()
            androidContext(this@MainApplication)

            val appModule = module {
                single {
                    ApplicationPreferencesImpl(
                        PreferenceManager.getDefaultSharedPreferences(this@MainApplication)
                    )
                } withOptions { binds(listOf(ApplicationPreferences::class))}
                single {
                    GridCellSizeService()
                }
                single { ActivityUtils() }
            }

            modules(
                CoreModule(filesDir).module(),
                appModule
            )
        }
    }
}