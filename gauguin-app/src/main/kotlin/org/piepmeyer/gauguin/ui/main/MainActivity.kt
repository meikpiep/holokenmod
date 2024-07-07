package org.piepmeyer.gauguin.ui.main

import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.databinding.ActivityMainBinding
import org.piepmeyer.gauguin.game.Game
import org.piepmeyer.gauguin.game.GameLifecycle
import org.piepmeyer.gauguin.options.NumeralSystem
import org.piepmeyer.gauguin.preferences.ApplicationPreferences
import org.piepmeyer.gauguin.ui.ActivityUtils
import org.piepmeyer.gauguin.ui.MainDialogs
import org.piepmeyer.gauguin.ui.newgame.NewGameActivity

private val logger = KotlinLogging.logger {}

class MainActivity :
    AppCompatActivity(),
    GridCreationListener,
    GameSolvedListener {
    private val game: Game by inject()
    private val gameLifecycle: GameLifecycle by inject()
    private val calculationService: GridCalculationService by inject()
    private val applicationPreferences: ApplicationPreferences by inject()
    private val activityUtils: ActivityUtils by inject()

    private lateinit var binding: ActivityMainBinding
    private lateinit var topFragment: GameTopFragment
    private lateinit var bottomAppBarService: MainBottomAppBarService
    lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

        gameLifecycle.setCoroutineScope(this.lifecycleScope)

        barcodeLauncher =
            registerForActivityResult(
                ScanContract(),
            ) {
                if (it.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    Toast
                        .makeText(
                            this,
                            "Scanned: " + it.contents,
                            Toast.LENGTH_LONG,
                        ).show()
                }
            }

        game.gridUI = binding.gridview
        binding.gridview.setOnLongClickListener {
            game.longClickOnSelectedCell()
        }

        binding.gridview.grid = game.grid

        val ft = supportFragmentManager.beginTransaction()
        topFragment = GameTopFragment()
        ft.replace(R.id.keypadFrame, KeyPadFragment())
        ft.replace(R.id.fastFinishingModeFrame, FastFinishingModeFragment())
        ft.replace(R.id.gameSolvedFrame, GameSolvedFragment())
        ft.replace(R.id.gameTopFrame, topFragment)
        ft.commit()

        registerForContextMenu(binding.gridview)

        bottomAppBarService = MainBottomAppBarService(this, binding)
        bottomAppBarService.initialize()

        val navigationViewService = MainNavigationViewService(this, binding)
        navigationViewService.initialize()

        configureActivity()

        FerrisWheelConfigurer(binding.ferrisWheelView).configure()

        val specialListener =
            OnSharedPreferenceChangeListener { _: SharedPreferences, key: String? ->
                if (key == "theme" || key == "maximumCellSize") {
                    this.recreate()
                } else if (key == "gridTakesRemainingSpaceIfNecessary") {
                    updateMainGridCellShape()
                }
            }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        preferences.registerOnSharedPreferenceChangeListener(specialListener)

        bottomAppBarService.updateAppBarState()
        navigationViewService.updateMainBottomBarMargins()

        val viewModel: MainViewModel by viewModels()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    reactOnUiState(it.state)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.nextGridState.collect {
                    reactOnNextGridState(it)
                }
            }
        }

        MainDialogs(this).openNewUserHelpDialog()
    }

    private fun reactOnUiState(state: MainUiState) {
        runOnUiThread {
            when (state) {
                MainUiState.CALCULATING_NEW_GRID ->
                    {
                        binding.gridview.visibility = View.INVISIBLE
                        binding.ferrisWheelView.visibility = View.VISIBLE
                        binding.ferrisWheelView.startAnimation()
                    }

                MainUiState.PLAYING ->
                    {
                        binding.gridview.visibility = View.VISIBLE

                        binding.ferrisWheelView.visibility = View.INVISIBLE
                        binding.ferrisWheelView.stopAnimation()

                        binding.konfettiView.stopGracefully()

                        updateMainGridCellShape()
                        updateNumeralSystemIcon()
                        bottomAppBarService.updateAppBarState()

                        binding.gridview.reCreate()
                        binding.gridview.invalidate()
                    }

                MainUiState.ALREADY_SOLVED -> bottomAppBarService.updateAppBarState()
                MainUiState.SOLVED -> {
                    bottomAppBarService.updateAppBarState()

                    if (!game.grid.isCheated()) {
                        KonfettiStarter(binding.konfettiView).startKonfetti()
                    }
                }
            }
        }
    }

    private fun reactOnNextGridState(state: NextGridState) {
        runOnUiThread {
            binding.pendingNextGridCalculation.visibility =
                when (state) {
                    NextGridState.CURRENTLY_CALCULATING -> View.VISIBLE
                    NextGridState.CALCULATED -> View.INVISIBLE
                }
        }
    }

    private fun updateMainGridCellShape() {
        val newCellShape =
            MainActivityGridCellShapeService(
                binding.gridview,
                applicationPreferences,
            ).calculateCellShape()

        if (binding.gridview.cellShape != newCellShape) {
            binding.gridview.cellShape = newCellShape
            binding.gridview.requestLayout()
        }
    }

    public override fun onPause() {
        gameLifecycle.pauseGame()

        super.onPause()
    }

    public override fun onResume() {
        gameLifecycle.setCoroutineScope(this.lifecycleScope)

        configureActivity()

        binding.konfettiView.reset()

        if (game.grid.isActive) {
            binding.gridview.requestFocus()
            binding.gridview.invalidate()
            gameLifecycle.resumeGame()
        }

        super.onResume()
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK && binding.gridview.isSelectorShown) {
            binding.gridview.requestFocus()
            binding.gridview.isSelectorShown = false
            binding.gridview.invalidate()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun configureActivity() {
        activityUtils.configureTheme(this)
        activityUtils.configureKeepScreenOn(this)
        activityUtils.configureFullscreen(this)

        binding.gridview.updateTheme()
    }

    fun showNewGameDialog() {
        val intent = Intent(this, NewGameActivity::class.java)

        val options =
            ActivityOptions.makeSceneTransitionAnimation(
                this,
                game.gridUI as View,
                "grid",
            )

        startActivity(intent, options.toBundle())
    }

    private fun updateNumeralSystemIcon() {
        if (game.grid.variant.options.numeralSystem == NumeralSystem.Decimal) {
            binding.numeralSystem.visibility = View.INVISIBLE
        } else {
            binding.numeralSystem.visibility = View.VISIBLE
            binding.numeralSystem.setImageResource(
                when (game.grid.variant.options.numeralSystem) {
                    NumeralSystem.Binary -> R.drawable.numeric_2_box_outline
                    NumeralSystem.Quaternary -> R.drawable.numeric_4_box_outline
                    NumeralSystem.Octal -> R.drawable.numeric_8_box_outline
                    NumeralSystem.Hexadecimal -> R.drawable.alpha_f_box_outline
                    NumeralSystem.Decimal -> R.drawable.baseline_pending_20
                },
            )
        }
    }

    fun checkProgress() {
        if (game.grid.isSolved()) {
            return
        }

        logger.info { "Going to show the hint popup from grid" }
        logger.info { game.grid.detailedToString() }

        BalloonHintPopup(binding, resources, game, applicationContext, theme, this).show()

        logger.info { "Showing the hint popup from grid" }
        logger.info { game.grid.detailedToString() }
    }

    fun gameSaved() {
        Snackbar
            .make(
                binding.root,
                resources.getText(R.string.main_activity_application_bar_item_current_game_saved),
                Snackbar.LENGTH_LONG,
            ).show()
    }
}
