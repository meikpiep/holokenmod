package org.piepmeyer.gauguin.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.game.GameLifecycle
import org.piepmeyer.gauguin.game.save.SavedGamesService
import java.io.File

class LoadGameListActivity : AppCompatActivity() {
    private val gameLifecycle: GameLifecycle by inject()
    private val savedGamesService: SavedGamesService by inject()
    private val activityUtils: ActivityUtils by inject()
    private lateinit var mAdapter: LoadGameListAdapter
    private lateinit var empty: View

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_savegame)

        val recyclerView = findViewById<RecyclerView>(android.R.id.list)

        activityUtils.configureTheme(this)
        activityUtils.configureFullscreen(this)

        empty = findViewById(android.R.id.empty)
        val relativeWidth =
            (
                resources.displayMetrics.widthPixels /
                    resources.displayMetrics.density
            ).toInt()

        var columns = relativeWidth / 180
        if (columns < 1) {
            columns = 1
        }

        if (columns > 4) {
            columns = 4
        }

        recyclerView.layoutManager = GridLayoutManager(this, columns)

        mAdapter = LoadGameListAdapter(this)
        recyclerView.adapter = mAdapter
        if (mAdapter.itemCount == 0) {
            empty.visibility = View.VISIBLE
        }
        val appBar = findViewById<MaterialToolbar>(R.id.saveGameAppBar)
        appBar.setOnMenuItemClickListener { item: MenuItem ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.discardbutton -> {
                    deleteAllGamesDialog()
                    true
                }
                else -> false
            }
        }
        appBar.setNavigationOnClickListener {
            this@LoadGameListActivity.setResult(RESULT_CANCELED)
            finish()
        }
        numberOfSavedGamesChanged()
    }

    private fun deleteSaveGame(filename: File?) {
        filename!!.delete()
        mAdapter.refreshFiles()
        mAdapter.notifyDataSetChanged()
        numberOfSavedGamesChanged()
    }

    private fun deleteAllSaveGames() {
        for (file in savedGamesService.savedGameFiles()) {
            file.delete()
        }
        mAdapter.refreshFiles()
        mAdapter.notifyDataSetChanged()
        numberOfSavedGamesChanged()
    }

    private fun numberOfSavedGamesChanged() {
        if (mAdapter.itemCount == 0) {
            empty.visibility = View.VISIBLE
            findViewById<View>(R.id.discardbutton).isEnabled = false
        } else {
            empty.visibility = View.GONE
            findViewById<View>(R.id.discardbutton).isEnabled = true
        }

        savedGamesService.informSavedGamesChanged()
    }

    fun deleteGameDialog(filename: File?) {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.dialog_delete_game_title))
            .setMessage(resources.getString(R.string.dialog_delete_game_message))
            .setNegativeButton(
                resources.getString(R.string.dialog_delete_game_cancel_button),
            ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .setPositiveButton(resources.getString(R.string.dialog_delete_game_ok_button)) { _: DialogInterface?, _: Int ->
                deleteSaveGame(
                    filename,
                )
            }.show()
    }

    private fun deleteAllGamesDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_all_games_title)
            .setMessage(R.string.dialog_delete_all_games_message)
            .setNegativeButton(R.string.dialog_delete_all_games_cancel_button) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .setPositiveButton(R.string.dialog_delete_all_games_ok_button) { _: DialogInterface?, _: Int -> deleteAllSaveGames() }
            .show()
    }

    fun loadSaveGame(filename: File?) {
        gameLifecycle.loadGame(File(filename!!.absolutePath))

        setResult(RESULT_OK)
        finish()
    }
}
