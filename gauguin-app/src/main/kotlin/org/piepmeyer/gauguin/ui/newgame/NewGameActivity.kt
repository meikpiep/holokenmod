package org.piepmeyer.gauguin.ui.newgame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
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
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        val binding = ActivityNewgameBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        activityUtils.configureTheme(this)
        activityUtils.configureFullscreen(this)

        val startNewGameButton = binding.startnewgame
        startNewGameButton.setOnClickListener { startNewGame() }

        viewModel = ViewModelProvider(this).get(NewGameViewModel::class.java)

        supportFragmentManager.commit {
            replace(R.id.newGameOptions, GridCellOptionsFragment())
            replace(R.id.newGameGridShapeOptions, GridShapeOptionsFragment())
        }

        binding.sideSheet?.let {
            val sideSheetBehavior = SideSheetBehavior.from(it)
            sideSheetBehavior.state = SideSheetBehavior.STATE_EXPANDED
        }

        binding.bottomSheet?.let {
            val bottomSheetBehavior = BottomSheetBehavior.from(it)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
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
