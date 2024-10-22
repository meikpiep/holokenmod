package org.piepmeyer.gauguin.difficulty.human

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.piepmeyer.gauguin.game.save.SaveGame
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.isDirectory
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}

class HumanDifficultyBalanceTest :
    FunSpec({
        test("balancing") {
            val savedGames =
                Files
                    .list(File("src/test/resources/difficulty-balancing").toPath())
                    .collect(Collectors.toList())
                    .filter { !it.isDirectory() }

            val namesToGrids =
                savedGames
                    .associateWith {
                        val grid = SaveGame.createWithFile(it.toFile()).restore()!!
                        grid.clearUserValues()
                        grid.addPossiblesAtNewGame()

                        grid
                    }.mapKeys { it.key.name }

            val namesToDifficulties =
                namesToGrids.mapValues {
                    logger.info { it.key + "..." }

                    val result = HumanSolver(it.value).solveAndCalculateDifficulty()

                    withClue(it.key) {
                        result.success shouldBe true
                    }

                    result.difficulty
                }

            namesToDifficulties.entries
                .sortedBy { it.value }
                .forEach {
                    logger.info { "${it.value} -> ${it.key}" }
                }
        }
    })
