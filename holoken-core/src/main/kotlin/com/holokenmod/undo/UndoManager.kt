package com.holokenmod.undo

import com.holokenmod.grid.GridCell

class UndoManager(private val listener: UndoListener) {
    private val undoList = mutableListOf<UndoState>()
    fun clear() {
        undoList.clear()
    }

    @Synchronized
    fun saveUndo(cell: GridCell, batch: Boolean) {
        val undoState = UndoState(
            cell,
            cell.userValue,
            cell.possibles,
            batch
        )
        undoList.add(undoState)
        listener.undoStateChanged(true)
    }

    @Synchronized
    fun restoreUndo() {
        if (undoList.isNotEmpty()) {
            val undoState = undoList.removeLast()
            val cell = undoState.cell
            cell.setUserValueIntern(undoState.userValue)
            cell.possibles = undoState.possibles
            cell.isLastModified = true
            if (undoState.isBatch) {
                restoreUndo()
            }
        }
        if (undoList.isEmpty()) {
            listener.undoStateChanged(false)
        }
    }
}
