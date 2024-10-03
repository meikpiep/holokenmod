package org.piepmeyer.gauguin.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.piepmeyer.gauguin.NightMode
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.Theme
import org.piepmeyer.gauguin.databinding.FragmentThemeChooserBinding
import org.piepmeyer.gauguin.preferences.ApplicationPreferences
import org.piepmeyer.gauguin.ui.ActivityUtils
import org.piepmeyer.gauguin.ui.DynamicColorsPrecondition

class ThemeChooserFragment(
    private val mainActivity: Activity,
) : Fragment(R.layout.fragment_theme_chooser),
    KoinComponent {
    private val preferences: ApplicationPreferences by inject()
    private val activityUtils: ActivityUtils by inject()

    private lateinit var binding: FragmentThemeChooserBinding

    private var themeHasBeenAltered = false

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentThemeChooserBinding.inflate(inflater, parent, false)

        binding.nightModeLight.isChecked = preferences.nightMode == NightMode.LIGHT
        binding.nightModeLight.setOnClickListener {
            configureNightMode(NightMode.LIGHT)
        }

        binding.nightModeDark.isChecked = preferences.nightMode == NightMode.DARK
        binding.nightModeDark.setOnClickListener {
            configureNightMode(NightMode.DARK)
        }

        binding.nightModeSystemDefault.isChecked = preferences.nightMode == NightMode.SYSTEM_DEFAULT
        binding.nightModeSystemDefault.setOnClickListener {
            configureNightMode(NightMode.SYSTEM_DEFAULT)
        }

        binding.themeGauguin.isChecked = preferences.theme == Theme.GAUGUIN
        binding.themeGauguin.setOnClickListener {
            configureTheme(Theme.GAUGUIN)
        }

        if (DynamicColors.isDynamicColorAvailable()) {
            binding.themeDynamicColors.isChecked = preferences.theme == Theme.DYNAMIC_COLORS
            binding.themeDynamicColors.setOnClickListener {
                configureTheme(Theme.DYNAMIC_COLORS)
            }
        } else {
            binding.themeDynamicColors.visibility = View.GONE
        }

        binding.themeMonochrome.isChecked = preferences.theme == Theme.MONOCHROME
        binding.themeMonochrome.setOnClickListener {
            configureTheme(Theme.MONOCHROME)
        }

        return binding.root
    }

    private fun configureNightMode(nightMode: NightMode) {
        if (preferences.nightMode == nightMode) {
            return
        }

        preferences.nightMode = nightMode

        applyThemeAndNightModeChanges()
    }

    private fun configureTheme(theme: Theme) {
        if (preferences.theme == theme) {
            return
        }

        preferences.theme = theme

        applyThemeAndNightModeChanges()
    }

    private fun applyThemeAndNightModeChanges() {
        themeHasBeenAltered = true
        activityUtils.reconfigureTheme(mainActivity)

        val options =
            DynamicColorsOptions
                .Builder()
                .setThemeOverlay(R.style.AppTheme_Overlay)
                .setPrecondition(DynamicColorsPrecondition())
                .build()

        DynamicColors.applyToActivitiesIfAvailable(this.mainActivity.application, options)
    }

    fun themeHasBeenAltered(): Boolean = themeHasBeenAltered
}
