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

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentThemeChooserBinding.inflate(inflater, parent, false)

        binding.navigationDrawerThemeLight.isChecked = preferences.theme == Theme.LIGHT
        binding.navigationDrawerThemeLight.setOnClickListener {
            switchToTheme(Theme.LIGHT)
        }

        binding.navigationDrawerThemeDark.isChecked = preferences.theme == Theme.DARK
        binding.navigationDrawerThemeDark.setOnClickListener {
            switchToTheme(Theme.DARK)
        }

        binding.navigationDrawerThemeAuto.isChecked = preferences.theme == Theme.SYSTEM_DEFAULT
        binding.navigationDrawerThemeAuto.setOnClickListener {
            switchToTheme(Theme.SYSTEM_DEFAULT)
        }

        binding.navigationDrawerThemeDynamicColors.isChecked = preferences.theme == Theme.DYNAMIC_COLORS
        binding.navigationDrawerThemeDynamicColors.setOnClickListener {
            switchToTheme(Theme.DYNAMIC_COLORS)
        }

        return binding.root
    }

    private fun switchToTheme(theme: Theme) {
        preferences.theme = theme

        activityUtils.configureTheme(mainActivity)
        activityUtils.configureNightMode()

        val options =
            DynamicColorsOptions
                .Builder()
                .setThemeOverlay(R.style.AppTheme_Overlay)
                .setPrecondition(DynamicColorsPrecondition())
                .build()

        DynamicColors.applyToActivitiesIfAvailable(this.mainActivity.application, options)
    }
}
