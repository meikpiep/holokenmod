package org.piepmeyer.gauguin.difficulty.human.strategy

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.piepmeyer.gauguin.creation.GridBuilder
import org.piepmeyer.gauguin.creation.cage.GridCageType
import org.piepmeyer.gauguin.difficulty.human.PossiblesCache
import org.piepmeyer.gauguin.grid.GridCageAction

class RemoveImpossibleCombinationInLineTest :
    FunSpec({

        test("4x4 grid") {
            val grid =
                GridBuilder(4, 4)
                    .addSingleCage(1, 3)
                    .addSingleCage(4, 10)
                    .addCage(
                        24,
                        GridCageAction.ACTION_MULTIPLY,
                        GridCageType.TRIPLE_HORIZONTAL,
                        0,
                    ).addCage(
                        24,
                        GridCageAction.ACTION_MULTIPLY,
                        GridCageType.L_VERTICAL_SHORT_RIGHT_TOP,
                        4,
                    ).addCage(
                        8,
                        GridCageAction.ACTION_ADD,
                        GridCageType.ANGLE_LEFT_BOTTOM,
                        6,
                    ).addCage(
                        2,
                        GridCageAction.ACTION_DIVIDE,
                        GridCageType.DOUBLE_VERTICAL,
                        9,
                    ).addCage(
                        1,
                        GridCageAction.ACTION_SUBTRACT,
                        GridCageType.DOUBLE_HORIZONTAL,
                        14,
                    ).createGrid()

            grid.cells[0].possibles = setOf(3, 4)
            grid.cells[1].possibles = setOf(3, 4)
            grid.cells[2].userValue = 2
            grid.cells[3].userValue = 1
            grid.cells[4].possibles = setOf(1, 2, 3, 4)
            grid.cells[5].possibles = setOf(3, 4)
            grid.cells[6].possibles = setOf(1, 3)
            grid.cells[7].possibles = setOf(2, 4)
            grid.cells[8].possibles = setOf(1, 2)
            grid.cells[9].possibles = setOf(1, 2)
            grid.cells[10].userValue = 4
            grid.cells[11].userValue = 3
            grid.cells[12].possibles = setOf(1, 2, 3, 4)
            grid.cells[13].possibles = setOf(1, 2)
            grid.cells[14].possibles = setOf(1, 3)
            grid.cells[15].possibles = setOf(2, 4)

            val solver = RemoveImpossibleCombinationInLineBecauseOfSingleCell()

            println(grid)

            // solver should find two possibles and delete one of them for each run
            solver.fillCells(grid, PossiblesCache(grid)) shouldBe true

            println(grid)

            assertSoftly {
                withClue("cell 14") {
                    grid.cells[14].possibles shouldContainExactly setOf(3)
                }
            }
        }

        test("4x4 grid impossible combinations") {
            val grid =
                GridBuilder(4, 4)
                    .addSingleCage(2, 3)
                    .addSingleCage(1, 13)
                    .addCage(
                        3,
                        GridCageAction.ACTION_DIVIDE,
                        GridCageType.DOUBLE_VERTICAL,
                        0,
                    ).addCage(
                        9,
                        GridCageAction.ACTION_ADD,
                        GridCageType.ANGLE_LEFT_BOTTOM,
                        1,
                    ).addCage(
                        6,
                        GridCageAction.ACTION_MULTIPLY,
                        GridCageType.DOUBLE_VERTICAL,
                        5,
                    ).addCage(
                        8,
                        GridCageAction.ACTION_ADD,
                        GridCageType.TRIPLE_VERTICAL,
                        7,
                    ).addCage(
                        2,
                        GridCageAction.ACTION_DIVIDE,
                        GridCageType.DOUBLE_VERTICAL,
                        8,
                    ).addCage(
                        1,
                        GridCageAction.ACTION_SUBTRACT,
                        GridCageType.DOUBLE_VERTICAL,
                        10,
                    ).createGrid()

            grid.cells[0].possibles = setOf(1, 3)
            grid.cells[1].userValue = 4
            grid.cells[2].possibles = setOf(1, 3)
            grid.cells[3].userValue = 2
            grid.cells[4].possibles = setOf(1, 3)
            grid.cells[5].possibles = setOf(2, 3)
            grid.cells[6].possibles = setOf(2, 4)
            grid.cells[7].possibles = setOf(1, 3, 4)
            grid.cells[8].possibles = setOf(2, 4)
            grid.cells[9].possibles = setOf(2, 3)
            grid.cells[10].possibles = setOf(1, 2, 3, 4)
            grid.cells[11].possibles = setOf(1, 3, 4)
            grid.cells[12].possibles = setOf(2, 4)
            grid.cells[13].userValue = 1
            grid.cells[14].possibles = setOf(2, 3, 4)
            grid.cells[15].possibles = setOf(3, 4)

            val solver = RemoveImpossibleCombinationInLineBecauseOfPossiblesOfOtherCage()

            println(grid)

            // solver should find two possibles and delete one of them for each run
            solver.fillCells(grid, PossiblesCache(grid)) shouldBe true

            println(grid)

            assertSoftly {
                withClue("cell 2") {
                    grid.cells[2].possibles shouldContainExactly setOf(1)
                }
            }
        }
    })
