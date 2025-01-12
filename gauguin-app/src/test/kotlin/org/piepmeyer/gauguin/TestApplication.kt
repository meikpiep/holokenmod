package org.piepmeyer.gauguin

import android.app.Application
import io.mockk.every
import io.mockk.mockk
import org.koin.core.context.startKoin
import org.piepmeyer.gauguin.preferences.ApplicationPreferencesImpl
import kotlin.io.path.createTempDirectory

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val mockedPreferences =
            mockk<ApplicationPreferencesImpl> {
                every { theme } returns Theme.LIGHT
            }

        startKoin {
            modules(
                CoreModule(createTempDirectory().toFile()).module(),
                ApplicationModule(createTempDirectory().toFile(), mockk(), mockedPreferences).module(),
            )
        }
    }
}
