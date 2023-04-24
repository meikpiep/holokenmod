package com.holokenmod.creation

import com.holokenmod.RandomSingleton
import com.holokenmod.Randomizer
import com.holokenmod.backtrack.hybrid.MathDokuCage2BackTrack
import com.holokenmod.creation.cage.GridSingleCageCreator
import com.holokenmod.grid.Grid
import com.holokenmod.options.DigitSetting
import com.holokenmod.options.GameVariant
import com.srlee.dlx.DLX
import com.srlee.dlx.MathDokuDLX
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class GridCalculator(
    private val randomizer: Randomizer,
    private val shuffler: PossibleDigitsShuffler,
    private val variant: GameVariant
) {
    private val LOGGER = LoggerFactory.getLogger(GridCalculator::class.java)

    constructor(variant: GameVariant) : this(
        RandomSingleton.instance,
        RandomPossibleDigitsShuffler(),
        variant
    )

    fun calculate(): Grid {
        val debug = false
        var dlxNumber = 0
        var backTrack2Number = 0
        var num_attempts = 0
        var sumBacktrack2Duration: Long = 0
        var sumDLXDuration: Long = 0
        val useDLX = variant.gridSize.isSquare &&
                (variant.options.digitSetting == DigitSetting.FIRST_DIGIT_ZERO
                        || variant.options.digitSetting == DigitSetting.FIRST_DIGIT_ONE)
        var grid: Grid?
        do {
            grid = GridCreator(randomizer, shuffler, variant).createRandomizedGridWithCages()
            num_attempts++
            if (useDLX) {
                val dlxMillis = System.currentTimeMillis()
                val mdd = MathDokuDLX(grid)
                // Stop solving as soon as we find multiple solutions
                dlxNumber = mdd.Solve(DLX.SolveType.MULTIPLE)
                val dlxDuration = System.currentTimeMillis() - dlxMillis
                sumDLXDuration += dlxDuration
                LOGGER.info("DLX Num Solns = $dlxNumber in $dlxDuration ms")
                if (dlxNumber == 0) {
                    LOGGER.debug(grid.toString())
                }
            }
            if (!useDLX || debug) {
                val backtrack2Millis = System.currentTimeMillis()
                val backTrack2 = MathDokuCage2BackTrack(grid, true)
                backTrack2Number = backTrack2.solve()
                val backtrack2Duration = System.currentTimeMillis() - backtrack2Millis
                sumBacktrack2Duration += backtrack2Duration
                grid.clearUserValues()
                LOGGER.info("Backtrack2 Num Solns = $backTrack2Number in $backtrack2Duration ms")
                if (backTrack2Number != dlxNumber) {
                    LOGGER.debug("difference: backtrack2 $backTrack2Number - dlx $dlxNumber:$grid")

                    //System.exit(0);
                }
                if (backTrack2Number == 1) {
                    grid.clearUserValues()
                }
                if (backTrack2Number == 0) {
                    LOGGER.debug("backtrack2 found no solution: $grid")
                    for (cage in grid.cages) {
                        LOGGER.debug(
                            "backtrack2 cage "
                                    + cage.id
                        )
                        for (possibleNums in GridSingleCageCreator(grid, cage).possibleNums) {
                            LOGGER.debug("backtrack2     " + possibleNums.contentToString())
                        }
                    }
                    exitProcess(0)
                }
            }
        } while ((useDLX && dlxNumber != 1 || !useDLX) && backTrack2Number != 1)
        val averageBacktrack2 = sumBacktrack2Duration / num_attempts
        val averageDLX = sumDLXDuration / num_attempts
        LOGGER.debug("DLX Num Attempts = $num_attempts in $sumDLXDuration ms (average $averageDLX ms)")
        LOGGER.debug(
            "Backtrack 2 Num Attempts = $num_attempts in $sumBacktrack2Duration ms (average $averageBacktrack2 ms)"
        )
        grid!!.clearUserValues()
        return grid
    }
}