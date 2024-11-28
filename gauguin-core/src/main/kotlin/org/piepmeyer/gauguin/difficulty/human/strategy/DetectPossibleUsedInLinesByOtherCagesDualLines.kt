package org.piepmeyer.gauguin.difficulty.human.strategy

import org.piepmeyer.gauguin.difficulty.human.HumanSolverCache
import org.piepmeyer.gauguin.difficulty.human.GridLines
import org.piepmeyer.gauguin.difficulty.human.HumanSolverStrategy
import org.piepmeyer.gauguin.grid.Grid

class DetectPossibleUsedInLinesByOtherCagesDualLines : HumanSolverStrategy {
    override fun fillCells(
        grid: Grid,
        cache: HumanSolverCache,
    ): Boolean {
        val lines = cache.adjacentlines(2)

        lines.forEach { dualLines ->

            val cellsOfLines = dualLines.cells()

            val (cagesIntersectingWithLines, possiblesInLines) = GridLineHelper.getIntersectingCagesAndPossibles(dualLines, cache)

            cagesIntersectingWithLines.forEach { cage ->
                val possiblesForFirstCage = possiblesInLines[cage]!!

                val possibleInEachFirstCageCombination =
                    grid.variant.possibleDigits.filter { possible ->
                        possiblesForFirstCage.all { it.contains(possible) }
                    }

                if (possibleInEachFirstCageCombination.isNotEmpty()) {
                    cagesIntersectingWithLines
                        .filter { it.id > cage.id }
                        .forEach { otherCage ->
                            val possiblesForOtherCage = possiblesInLines[otherCage]!!

                            val possibleInEachOtherCageCombination =
                                grid.variant.possibleDigits.filter { possible ->
                                    possiblesForOtherCage.all { it.contains(possible) }
                                }

                            val possiblesInBothCages =
                                possibleInEachFirstCageCombination.intersect(
                                    possibleInEachOtherCageCombination,
                                )

                            if (possiblesInBothCages.isNotEmpty()) {
                                val foreignCells = cellsOfLines - cage.cells - otherCage.cells

                                var found = false

                                possiblesInBothCages.forEach { possible ->
                                    if (foreignCells.any { it.possibles.contains(possible) }) {
                                        found = true

                                        foreignCells.forEach { it.possibles -= possible }
                                    }
                                }

                                if (found) {
                                    return true
                                }
                            }
                        }
                }
            }
        }

        return false
    }
}
