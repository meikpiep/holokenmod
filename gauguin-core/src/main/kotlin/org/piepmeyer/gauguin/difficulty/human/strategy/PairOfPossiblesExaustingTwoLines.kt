package org.piepmeyer.gauguin.difficulty.human.strategy

import org.piepmeyer.gauguin.difficulty.human.GridLines
import org.piepmeyer.gauguin.difficulty.human.HumanSolverStrategy
import org.piepmeyer.gauguin.difficulty.human.PossiblesCache
import org.piepmeyer.gauguin.grid.Grid

/**
 * Finds a set of two possibles which occur in two lines and counts its minimum existence in all
 * combinations.
 */
class PairOfPossiblesExaustingTwoLines : HumanSolverStrategy {
    override fun fillCells(
        grid: Grid,
        cache: PossiblesCache,
    ): Boolean {
        val lines = GridLines(grid).adjacentlines(2)

        lines.forEach { dualLines ->

            val (cagesIntersectingWithLines, possiblesInLines) = GridLineHelper.getIntersectingCagesAndPossibles(dualLines, cache)

            val combinations =
                grid.variant.possibleDigits.flatMap { firstPossible ->
                    (grid.variant.possibleDigits - firstPossible).map { setOf(it, firstPossible) }
                }

            combinations.forEach { combinationOfPossibles ->
                val cageMinimumOccurences =
                    cagesIntersectingWithLines.associateWith { cage ->
                        possiblesInLines[cage]!!.minOf { combinationOfPossibles.intersect(it).size }
                    }

                val minimumOccurences = cageMinimumOccurences.values.sum()

                if (minimumOccurences > 1) {
                    cageMinimumOccurences.forEach { (cage, minimumOccurence) ->
                        val occurencesLeft = 4 - minimumOccurences + minimumOccurence

                        val invalidPossibles =
                            possiblesInLines[cage]!!.filter {
                                combinationOfPossibles
                                    .intersect(
                                        it,
                                    ).size > occurencesLeft
                            }

                        if (invalidPossibles.isNotEmpty()) {
                            val validPossibles = possiblesInLines - invalidPossibles

                            /*val reduced = PossiblesReducer(cage).reduceToPossibleCombinations(validPossibles)

                            if (reduced) {
                                return true
                            }*/
                        }
                    }
                }
            }
        }

        return false
    }
}
