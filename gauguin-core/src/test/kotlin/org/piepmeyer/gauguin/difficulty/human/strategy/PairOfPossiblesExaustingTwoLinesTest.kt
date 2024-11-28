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

class PairOfPossiblesExaustingTwoLinesTest :
    FunSpec({

        test("2x6 grid") {
            val grid =
                GridBuilder(2, 6)
                    .addCage(
                        2,
                        GridCageAction.ACTION_SUBTRACT,
                        GridCageType.DOUBLE_HORIZONTAL,
                        0,
                    ).addCage(
                        1,
                        GridCageAction.ACTION_SUBTRACT,
                        GridCageType.DOUBLE_HORIZONTAL,
                        2,
                    ).addCage(
                        18,
                        GridCageAction.ACTION_MULTIPLY,
                        GridCageType.DOUBLE_HORIZONTAL,
                        4,
                    ).addSingleCage(2, 6)
                    .addSingleCage(5, 7)
                    .addCage(
                        3,
                        GridCageAction.ACTION_DIVIDE,
                        GridCageType.DOUBLE_HORIZONTAL,
                        8,
                    ).addCage(
                        9,
                        GridCageAction.ACTION_ADD,
                        GridCageType.DOUBLE_HORIZONTAL,
                        10,
                    ).createGrid()

            grid.cells[0].possibles = setOf(1, 3, 4, 5, 6)
            grid.cells[1].possibles = setOf(1, 2, 3, 4, 6)
            grid.cells[2].possibles = setOf(1, 3, 4, 5)
            grid.cells[3].possibles = setOf(2, 3, 4)
            grid.cells[4].possibles = setOf(3, 6)
            grid.cells[5].possibles = setOf(3, 6)
            grid.cells[6].userValue = 2
            grid.cells[7].userValue = 5
            grid.cells[8].possibles = setOf(1, 3, 6)
            grid.cells[9].possibles = setOf(1, 2, 3)
            grid.cells[10].possibles = setOf(3, 5, 6)
            grid.cells[11].possibles = setOf(3, 4, 6)

            val solver = PairOfPossiblesExaustingTwoLines()

            println(grid)

            solver.fillCells(grid, PossiblesCache(grid)) shouldBe true

            println(grid)

            assertSoftly {
                withClue("possibles should be deleted from other cells") {
                    grid.cells[0].possibles shouldContainExactly setOf(1, 3, 4, 5, 6)
                    grid.cells[1].possibles shouldContainExactly setOf(1, 2, 3, 4, 6)
                    grid.cells[2].possibles shouldContainExactly setOf(1, 3, 4, 5)
                    grid.cells[3].possibles shouldContainExactly setOf(2, 3, 4)
                    grid.cells[8].possibles shouldContainExactly setOf(1, 3, 6)
                    grid.cells[9].possibles shouldContainExactly setOf(1, 2, 3)
                    grid.cells[10].possibles shouldContainExactly setOf(5)
                    grid.cells[11].possibles shouldContainExactly setOf(4)
                }
            }
        }
    })
