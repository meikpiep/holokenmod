package org.piepmeyer.gauguin.ui.main

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameRes
import com.mikepenz.materialdrawer.util.updateItem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.databinding.ActivityMainBinding
import org.piepmeyer.gauguin.game.save.CurrentGameSaver
import org.piepmeyer.gauguin.game.save.SavedGamesListener
import org.piepmeyer.gauguin.game.save.SavedGamesService
import org.piepmeyer.gauguin.ui.LoadGameListActivity
import org.piepmeyer.gauguin.ui.MainDialogs
import org.piepmeyer.gauguin.ui.SettingsActivity
import org.piepmeyer.gauguin.ui.statistics.StatisticsActivity

class MainNavigationViewService(
    private val mainActivity: MainActivity,
    private val binding: ActivityMainBinding,
) : KoinComponent {
    private val savedGamesService: SavedGamesService by inject()
    private val currentGameSaver: CurrentGameSaver by inject()

    private val newGameItem =
        PrimaryDrawerItem().apply {
            nameRes = R.string.main_menu_item_new
            identifier = 1
            iconRes = R.drawable.outline_add_24
        }
    private val restartGameItem =
        PrimaryDrawerItem().apply {
            nameRes = R.string.main_menu_item_restart_game
            identifier = 2
            iconRes = R.drawable.outline_replay_24
        }
    private val loadGameItem =
        PrimaryDrawerItem().apply {
            nameRes = R.string.main_menu_item_load_game
            identifier = 3
            iconRes = R.drawable.outline_open_in_new_24
        }
    private val saveGameItem =
        PrimaryDrawerItem().apply {
            nameRes = R.string.main_menu_item_save_game
            identifier = 4
            iconRes = R.drawable.outline_save_24
        }
    private val saveGameWithCommentItem =
        PrimaryDrawerItem().apply {
            nameRes = R.string.main_menu_item_save_game_with_comment
            identifier = 5
            iconRes = R.drawable.outline_save_24
        }
    private val statisticsItem =
        SecondaryDrawerItem().apply {
            nameRes = R.string.main_menu_item_show_statistics
            identifier = 6
            iconRes = R.drawable.outline_leaderboard_24
        }
    private val settingsItem =
        SecondaryDrawerItem().apply {
            nameRes = R.string.main_menu_item_open_settings
            identifier = 7
            iconRes = R.drawable.outline_settings_24
        }
    private val helpItem =
        SecondaryDrawerItem().apply {
            nameRes = R.string.main_menu_item_show_help
            identifier = 8
            iconRes = R.drawable.outline_help_24
        }
    private val bugsAndFeaturesItem =
        SecondaryDrawerItem().apply {
            nameRes = R.string.main_menu_item_open_github_issues
            identifier = 9
            iconRes = R.drawable.outline_bug_report_24
        }

    fun initialize() {
        binding.container.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val savedGamesListener =
            SavedGamesListener {
                val countOfSavedGames = savedGamesService.countOfSavedGames()

                if (countOfSavedGames > 0) {
                    loadGameItem.badge = StringHolder(countOfSavedGames.toString())
                } else {
                    loadGameItem.badge = null
                }

                binding.mainNavigationView.updateItem(loadGameItem)
            }

        savedGamesService.addSavedGamesListener(savedGamesListener)
        savedGamesListener.savedGamesChanged()

        binding.mainNavigationView.itemAdapter.add(
            newGameItem,
            restartGameItem,
            DividerDrawerItem(),
            loadGameItem,
            saveGameItem,
        )

        if (mainActivity.resources.getBoolean(R.bool.debuggable)) {
            binding.mainNavigationView.itemAdapter.add(saveGameWithCommentItem)
        }

        binding.mainNavigationView.itemAdapter.add(
            DividerDrawerItem(),
            statisticsItem,
            settingsItem,
            helpItem,
            bugsAndFeaturesItem,
        )

        val header =
            View.inflate(
                ContextThemeWrapper(
                    binding.mainNavigationView.context,
                    R.style.AppTheme,
                ),
                R.layout.view_main_navigation_drawer_header,
                null,
            )

        binding.mainNavigationView.stickyHeaderView = header
        header.setBackgroundResource(0)
        header.setOnClickListener {
            val ft = mainActivity.supportFragmentManager.beginTransaction()

            binding.mainNavigationView.drawerLayout?.close()
            MainDialogs(mainActivity).openAboutDialog(binding.mainNavigationView)

            ft.commit()
        }

        binding.mainNavigationView.onDrawerItemClickListener = createDrawerClickListener()

        binding.mainBottomAppBar.setOnMenuItemClickListener(
            BottomAppBarItemClickListener(mainActivity),
        )
        binding.mainBottomAppBar.setNavigationOnClickListener { binding.container.open() }

        binding.gridview.addOnLayoutChangeListener { _, _, _, right, _, _, _, _, _ ->
            updateMainBottomBarMargins(right)
        }
    }

    fun updateMainBottomBarMargins() {
        updateMainBottomBarMargins(binding.gridview.right)
    }

    private fun updateMainBottomBarMargins(right: Int) {
        val marginParams =
            binding.mainBottomAppBar.layoutParams as ViewGroup.MarginLayoutParams

        if (marginParams.marginStart != 0 && right > 0 && marginParams.marginStart != right) {
//            marginParams.marginStart = right

            binding.mainBottomAppBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.marginStart = right
                this.width -= binding.root.width - right
            }

            binding.hint.requestLayout()
            binding.mainBottomAppBar.invalidateMenu()
            binding.mainBottomAppBar.forceLayout()
            binding.mainBottomAppBar.requestLayout()
            binding.root.forceLayout()
            println("------------------------------------------------")
        }
        println(right.toString() + " - " + marginParams.marginStart)
    }

    private fun createDrawerClickListener(): (v: View?, item: IDrawerItem<*>, position: Int) -> Boolean =
        { _, menuItem, _ ->
            when (menuItem) {
                newGameItem -> mainActivity.showNewGameDialog()
                loadGameItem -> {
                    mainActivity.startActivity(
                        Intent(mainActivity, LoadGameListActivity::class.java),
                    )
                }

                saveGameItem -> {
                    currentGameSaver.save()

                    mainActivity.gameSaved()
                }

                saveGameWithCommentItem -> {
                    MainDialogs(mainActivity).saveGameWithCommentDialog(currentGameSaver)
                }

                restartGameItem -> MainDialogs(mainActivity).restartGameDialog()
                statisticsItem ->
                    mainActivity.startActivity(
                        Intent(
                            mainActivity,
                            StatisticsActivity::class.java,
                        ),
                    )

                settingsItem ->
                    mainActivity.startActivity(
                        Intent(
                            mainActivity,
                            SettingsActivity::class.java,
                        ),
                    )

                helpItem -> MainDialogs(mainActivity).openHelpDialog()
                bugsAndFeaturesItem -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://github.com/meikpiep/gauguin/issues")
                    mainActivity.startActivity(intent)
                }
            }
            val drawerLayout = mainActivity.findViewById<DrawerLayout>(R.id.container)
            drawerLayout.close()

            binding.mainNavigationView.selectExtension.deselect()

            true
        }
}
