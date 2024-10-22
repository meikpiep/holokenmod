package org.piepmeyer.gauguin.difficulty.human

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.piepmeyer.gauguin.creation.RandomCageGridCalculator
import org.piepmeyer.gauguin.creation.RandomPossibleDigitsShuffler
import org.piepmeyer.gauguin.creation.SeedRandomizerMock
import org.piepmeyer.gauguin.grid.GridSize
import org.piepmeyer.gauguin.options.GameOptionsVariant
import org.piepmeyer.gauguin.options.GameVariant

class HumanDifficultySolverPerformanceTest :
    FunSpec({
        for (seed in 0..9) {
            withClue("seed $seed") {

                test("seed random grid should be solved") {
                    val randomizer = SeedRandomizerMock(1)

                    val calculator =
                        RandomCageGridCalculator(
                            GameVariant(
                                GridSize(11, 11),
                                GameOptionsVariant.createClassic(),
                            ),
                            randomizer,
                            RandomPossibleDigitsShuffler(randomizer.random),
                        )

                    val grid = calculator.calculate()
                    grid.cells.forEach { it.possibles = grid.variant.possibleDigits }

                    val solver = HumanSolver(grid)

                    val solverResult = solver.solveAndCalculateDifficulty()

                    println(grid.toString())

                    if (!grid.isSolved()) {
                        if (grid.numberOfMistakes() != 0) {
                            throw IllegalStateException("Found a grid with wrong values.")
                        }
                    }

                    grid.isSolved() shouldBe true

                    solverResult.difficulty shouldBeGreaterThan 0
                }
            }
        } 
    })
