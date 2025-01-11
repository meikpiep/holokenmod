package org.piepmeyer.gauguin.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.android.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.Theme

class SettingsActivity : AppCompatActivity() {
    private val activityUtils: ActivityUtils by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            val settings = SettingsFragment()

            supportFragmentManager.commit {
                replace(R.id.settings, settings)
            }
        }
        val actionBar = supportActionBar

        activityUtils.configureFullscreen(this)

        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            if (!DynamicColors.isDynamicColorAvailable()) {
                val themePreference = findPreference<ListPreference>("theme")!!

                val reducedEntryValues = themePreference.entryValues.toMutableList()
                reducedEntryValues.removeAt(Theme.DYNAMIC_COLORS.ordinal)

                val reducedEntries = themePreference.entries.toMutableList()
                reducedEntries.removeAt(Theme.DYNAMIC_COLORS.ordinal)

                themePreference.entryValues = reducedEntryValues.toTypedArray()
                themePreference.entries = reducedEntries.toTypedArray()
            }
        }
    }
}
