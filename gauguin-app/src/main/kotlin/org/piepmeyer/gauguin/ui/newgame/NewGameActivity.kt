package org.piepmeyer.gauguin.ui.newgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.sidesheet.SideSheetBehavior
import org.koin.android.ext.android.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.databinding.ActivityNewgameBinding
import org.piepmeyer.gauguin.ui.ActivityUtils

class NewGameActivity : AppCompatActivity() {
    private val activityUtils: ActivityUtils by inject()
    private lateinit var viewModel: NewGameViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        activityUtils.configureTheme(this)

        super.onCreate(savedInstanceState)

        val binding = ActivityNewgameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityUtils.configureFullscreen(this)

        val startNewGameButton = binding.startnewgame
        startNewGameButton.setOnClickListener { startNewGame() }

        viewModel = ViewModelProvider(this).get(NewGameViewModel::class.java)

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.newGameOptions, GridCellOptionsFragment())
        ft.commit()

        binding.sideSheet?.let {
            val sideSheetBehavior = SideSheetBehavior.from(it)
            sideSheetBehavior.state = SideSheetBehavior.STATE_EXPANDED
        }

        binding.bottomSheet?.let {
            val bottomSheetBehavior = BottomSheetBehavior.from(it)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        val ft2 = supportFragmentManager.beginTransaction()
        ft2.replace(R.id.newGameGridShapeOptions, GridShapeOptionsFragment())
        ft2.commit()
    }

    private fun startNewGame() {
        val gridAlreadyCalculated = viewModel.startNewGame()

        if (gridAlreadyCalculated) {
            finishAfterTransition()
        } else {
            finish()
        }
    }
}
