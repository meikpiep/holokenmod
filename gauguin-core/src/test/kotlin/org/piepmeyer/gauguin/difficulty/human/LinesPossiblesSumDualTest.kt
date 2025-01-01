package org.piepmeyer.gauguin.difficulty.human

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.piepmeyer.gauguin.creation.GridBuilder
import org.piepmeyer.gauguin.creation.cage.GridCageType
import org.piepmeyer.gauguin.difficulty.human.strategy.LinesSingleCagePossiblesSumDual
import org.piepmeyer.gauguin.grid.GridCageAction

class LinesPossiblesSumDualTest :
    FunSpec({

        test("4x4 grid") {
            val grid =
                GridBuilder(4, 4)
                    .addSingleCage(3, 3)
                    .addSingleCage(2, 14)
                    .addCage(
                        24,
                        GridCageAction.ACTION_MULTIPLY,
                        GridCageType.L_HORIZONTAL_SHORT_RIGHT_BOTTOM,
                        0,
                    ).addCage(
                        1,
                        GridCageAction.ACTION_SUBTRACT,
                        GridCageType.DOUBLE_VERTICAL,
                        4,
                    ).addCage(
                        9,
                        GridCageAction.ACTION_ADD,
                        GridCageType.ANGLE_RIGHT_TOP,
                        5,
                    ).addCage(
                        8,
                        GridCageAction.ACTION_MULTIPLY,
                        GridCageType.TRIPLE_VERTICAL,
                        7,
                    ).addCage(
                        3,
                        GridCageAction.ACTION_DIVIDE,
                        GridCageType.DOUBLE_HORIZONTAL,
                        12,
                    ).createGrid()

            grid.cells[0].possibles = setOf(2, 4)
            grid.cells[1].possibles = setOf(2, 4)
            grid.cells[2].userValue = 1
            grid.cells[3].userValue = 3
            grid.cells[4].possibles = setOf(1, 2, 4)
            grid.cells[5].possibles = setOf(2, 4)
            grid.cells[6].userValue = 3
            grid.cells[7].possibles = setOf(1, 2)
            grid.cells[8].possibles = setOf(1, 2, 3)
            grid.cells[9].possibles = setOf(1, 3)
            grid.cells[10].userValue = 4
            grid.cells[11].possibles = setOf(1, 2)
            grid.cells[12].possibles = setOf(1, 3)
            grid.cells[13].possibles = setOf(1, 3)
            grid.cells[14].userValue = 2
            grid.cells[15].userValue = 4

            val solver = LinesSingleCagePossiblesSumDual()

            println(grid)

            // solver should find two possibles and delete one of them for each run
            solver.fillCells(grid, PossiblesCache(grid)) shouldBe true

            println(grid)

            assertSoftly {
                withClue("cell 4") {
                    grid.cells[4].possibles shouldContainExactly setOf(2, 4)
                }
            }
        }
    })
