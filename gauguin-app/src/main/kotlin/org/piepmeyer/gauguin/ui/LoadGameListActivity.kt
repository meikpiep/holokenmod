package org.piepmeyer.gauguin.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.game.Game
import org.piepmeyer.gauguin.game.SaveGame
import org.piepmeyer.gauguin.ui.LoadGameListAdapter.ItemClickListener
import org.koin.android.ext.android.inject
import java.io.File

class LoadGameListActivity : AppCompatActivity(), ItemClickListener {
    private val game: Game by inject()
    private val activityUtils: ActivityUtils by inject()
    private lateinit var mAdapter: LoadGameListAdapter
    private lateinit var empty: View

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_savegame)

        val recyclerView = findViewById<RecyclerView>(android.R.id.list)
        activityUtils.configureFullscreen(this)

        empty = findViewById(android.R.id.empty)
        val relativeWidth = (resources.displayMetrics.widthPixels
                / resources.displayMetrics.density).toInt()

        var columns = relativeWidth / 180
        if (columns < 1) {
            columns = 1
        }

        if (columns > 4) {
            columns = 4
        }

        recyclerView.layoutManager = GridLayoutManager(this, columns)

        mAdapter = LoadGameListAdapter(this)
        mAdapter.setClickListener(this)
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
        for (file in saveGameFiles) {
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
    }

    val saveGameFiles: List<File>
        get() {
            return this.filesDir
                .listFiles { _: File?, name: String -> name.startsWith("savegame_") }
                ?.toList()
                ?.filterNotNull() ?: emptyList()
        }

    fun deleteGameDialog(filename: File?) {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.dialog_delete_title))
            .setMessage(resources.getString(R.string.dialog_delete_msg))
            .setNegativeButton(resources.getString(R.string.dialog_cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .setPositiveButton(resources.getString(R.string.dialog_ok)) { _: DialogInterface?, _: Int ->
                deleteSaveGame(
                    filename
                )
            }
            .show()
    }

    private fun deleteAllGamesDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_all_title)
            .setMessage(R.string.dialog_delete_all_msg)
            .setNegativeButton(R.string.dialog_cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .setPositiveButton(R.string.dialog_ok) { _: DialogInterface?, _: Int -> deleteAllSaveGames() }
            .show()
    }

    fun loadSaveGame(filename: File?) {
        val saver = SaveGame.createWithFile(File(filename!!.absolutePath))

        saver.restore()?.let {
            game.updateGrid(it)
        }

        setResult(RESULT_OK)
        finish()
    }

    override fun onItemClick(view: View?, position: Int) {
        loadSaveGame(saveGameFiles[position])
    }
}