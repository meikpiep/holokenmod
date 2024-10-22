package org.piepmeyer.gauguin.difficulty.human

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.piepmeyer.gauguin.creation.MergingCageGridCalculator
import org.piepmeyer.gauguin.creation.RandomPossibleDigitsShuffler
import org.piepmeyer.gauguin.creation.SeedRandomizerMock
import org.piepmeyer.gauguin.grid.GridSize
import org.piepmeyer.gauguin.options.GameOptionsVariant
import org.piepmeyer.gauguin.options.GameVariant

class HumanDifficultySolverHandpickedTest :
    FunSpec({
        test("seed random grid should be solved") {
            val randomizer = SeedRandomizerMock(16)

            val calculator =
                MergingCageGridCalculator(
                    GameVariant(
                        GridSize(3, 6),
                        GameOptionsVariant.createClassic(),
                    ),
                    randomizer,
                    RandomPossibleDigitsShuffler(randomizer.random),
                )

            val grid = calculator.calculate()
            grid.cells.forEach { it.possibles = grid.variant.possibleDigits }

            val solver = HumanSolver(grid, true)

            solver.solveAndCalculateDifficulty()

            println(grid.toString())

            grid.isSolved() shouldBe true
        }

        test("merging algo 4x4 wrong solution") {
            val randomizer = SeedRandomizerMock(6096)

            val calculator =
                MergingCageGridCalculator(
                    GameVariant(
                        GridSize(4, 4),
                        GameOptionsVariant.createClassic(),
                    ),
                    randomizer,
                    RandomPossibleDigitsShuffler(randomizer.random),
                )

            val grid = calculator.calculate()
            grid.cells.forEach { it.possibles = grid.variant.possibleDigits }

            val solver = HumanSolver(grid)

            solver.solveAndCalculateDifficulty()

            println(grid.toString())

            grid.isSolved() shouldBe true
        }

        test("merging algo 4x4 DetectPossiblesBreakingOtherCagesPossiblesDualLines bug") {
            val randomizer = SeedRandomizerMock(36)

            val calculator =
                MergingCageGridCalculator(
                    GameVariant(
                        GridSize(5, 5),
                        GameOptionsVariant.createClassic(),
                    ),
                    randomizer,
                    RandomPossibleDigitsShuffler(randomizer.random),
                )

            val grid = calculator.calculate()
            grid.cells.forEach { it.possibles = grid.variant.possibleDigits }

            val solver = HumanSolver(grid)

            solver.solveAndCalculateDifficulty()

            println(grid.toString())

            grid.isSolved() shouldBe true
        }
    })
