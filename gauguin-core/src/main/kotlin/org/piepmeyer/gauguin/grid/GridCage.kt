package org.piepmeyer.gauguin.grid

import org.piepmeyer.gauguin.creation.cage.GridCageType

class GridCage(
    val id: Int,
    private val grid: Grid,
    val action: GridCageAction,
    val cageType: GridCageType
) {
    var cells: List<GridCell> = mutableListOf()

    var cageText: String = ""
        private set

    var result = 0
    private var mUserMathCorrect = true
    private var mSelected = false

    override fun toString(): String {
        var retStr = ""
        retStr += "Cage id: $id"
        retStr += ", Action: "
        retStr += when (action) {
            GridCageAction.ACTION_NONE -> "None"
            GridCageAction.ACTION_ADD -> "Add"
            GridCageAction.ACTION_SUBTRACT -> "Subtract"
            GridCageAction.ACTION_MULTIPLY -> "Multiply"
            GridCageAction.ACTION_DIVIDE -> "Divide"
        }
        retStr += ", CageType: " + cageType.name
        retStr += ", ActionStr: " + action.operationDisplayName + ", Result: " + result
        retStr += ", cells: $cellNumbers"
        return retStr
    }

    private val isAddMathsCorrect: Boolean
        get() {
            var total = 0
            for (cell in cells) {
                total += cell.userValue!!
            }
            return total == result
        }
    private val isMultiplyMathsCorrect: Boolean
        get() {
            var total = 1
            for (cell in cells) {
                total *= cell.userValue!!
            }
            return total == result
        }
    private val isDivideMathsCorrect: Boolean
        get() {
            if (cells.size != 2) {
                return false
            }
            return if (cells[0].userValue!! > cells[1].userValue!!) {
                cells[0].userValue!! == cells[1].userValue!! * result
            } else {
                cells[1].userValue!! == cells[0].userValue!! * result
            }
        }
    private val isSubtractMathsCorrect: Boolean
        get() {
            if (cells.size != 2) {
                return false
            }
            return if (cells[0].userValue!! > cells[1].userValue!!) {
                cells[0].userValue!! - cells[1].userValue!! == result
            } else {
                cells[1].userValue!! - cells[0].userValue!! == result
            }
        }

    fun isMathsCorrect(): Boolean {
        if (cells.size == 1) {
            return cells[0].isUserValueCorrect
        }
        return if (grid.options.showOperators) {
            when (action) {
                GridCageAction.ACTION_ADD -> isAddMathsCorrect
                GridCageAction.ACTION_MULTIPLY -> isMultiplyMathsCorrect
                GridCageAction.ACTION_DIVIDE -> isDivideMathsCorrect
                GridCageAction.ACTION_SUBTRACT -> isSubtractMathsCorrect
                GridCageAction.ACTION_NONE -> true
            }
        } else {
            isAddMathsCorrect || isMultiplyMathsCorrect ||
                isDivideMathsCorrect || isSubtractMathsCorrect
        }
    }

    fun userValuesCorrect() {
        mUserMathCorrect = true

        for (cell in cells) {
            if (cell.userValue == null) {
                return
            }
        }
        mUserMathCorrect = isMathsCorrect()
    }

    fun isUserMathCorrect(): Boolean = mUserMathCorrect

    fun addCell(cell: GridCell) {
        cells = cells + cell
        cell.cage = this
    }

    val cellNumbers: String
        get() {
            val numbers = StringBuilder()
            for (cell in cells) {
                numbers.append(cell.cellNumber).append(",")
            }
            return numbers.toString()
        }
    val numberOfCells: Int
        get() = cells.size

    fun getCell(cellNumber: Int): GridCell {
        return cells[cellNumber]
    }

    fun updateCageText() {
        cageText = if (grid.options.showOperators) {
            result.toString() + action.operationDisplayName
        } else {
            result.toString()
        }
    }

    fun setSelected(mSelected: Boolean) {
        this.mSelected = mSelected
    }

    fun calculateResultFromAction() {
        if (action == GridCageAction.ACTION_ADD) {
            var total = 0
            for (cell in cells) {
                total += cell.value
            }
            result = total
            return
        }
        if (action == GridCageAction.ACTION_MULTIPLY) {
            var total = 1
            for (cell in cells) {
                total *= cell.value
            }
            result = total
            return
        }
        val cell1Value = cells[0].value
        val cell2Value = cells[1].value
        var higher = cell1Value
        var lower = cell2Value
        if (cell1Value < cell2Value) {
            higher = cell2Value
            lower = cell1Value
        }
        if (action == GridCageAction.ACTION_DIVIDE) {
            if (lower == 0) {
                result = 0
                return
            }
            result = higher / lower
        } else {
            result = higher - lower
        }
    }

    fun satisfiesConstraints(possibleNumbers: IntArray): Boolean {
        return cageType.satisfiesConstraints(possibleNumbers)
    }

    companion object {
        fun createWithCells(
            id: Int,
            grid: Grid,
            action: GridCageAction,
            firstCell: GridCell,
            cageType: GridCageType
        ): GridCage {
            val cage = GridCage(id, grid, action, cageType)
            for (coordinate in cageType.coordinates) {
                val col = firstCell.column + coordinate.first
                val row = firstCell.row + coordinate.second
                cage.addCell(grid.getValidCellAt(row, col))
            }
            return cage
        }

        fun createWithSingleCellArithmetic(id: Int, grid: Grid, gridCell: GridCell): GridCage {
            val cage = GridCage(id, grid, GridCageAction.ACTION_NONE, GridCageType.SINGLE)
            cage.result = gridCell.value
            cage.addCell(gridCell)

            return cage
        }
    }
}
