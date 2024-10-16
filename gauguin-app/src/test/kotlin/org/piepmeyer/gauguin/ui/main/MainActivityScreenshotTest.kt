package org.piepmeyer.gauguin.ui.main

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.piepmeyer.gauguin.ScreenshotTest
import org.piepmeyer.gauguin.ScreenshotTestUtils
import org.piepmeyer.gauguin.creation.GridCreator
import org.piepmeyer.gauguin.creation.RandomPossibleDigitsShuffler
import org.piepmeyer.gauguin.creation.SeedRandomizerMock
import org.piepmeyer.gauguin.game.Game
import org.piepmeyer.gauguin.game.GameLifecycle
import org.piepmeyer.gauguin.grid.Grid
import org.piepmeyer.gauguin.grid.GridSize
import org.piepmeyer.gauguin.options.DigitSetting
import org.piepmeyer.gauguin.options.GameOptionsVariant
import org.piepmeyer.gauguin.options.GameVariant
import org.piepmeyer.gauguin.preferences.ApplicationPreferences
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.uitesting.robolectric.activityscenario.robolectricActivityScenarioForActivityRule
import sergio.sastre.uitesting.robolectric.config.screen.DeviceScreen
import sergio.sastre.uitesting.robolectric.utils.activity.TestDataForActivity
import sergio.sastre.uitesting.robolectric.utils.activity.TestDataForActivityCombinator
import sergio.sastre.uitesting.utils.activityscenario.ActivityConfigItem
import sergio.sastre.uitesting.utils.common.Orientation
import sergio.sastre.uitesting.utils.common.UiMode

@Category(ScreenshotTest::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainActivityScreenshotTest(
    private val testItem: TestDataForActivity<UiStateEnum>,
) : KoinTest {
    enum class UiStateEnum {
        NewGame,
        NewGameWithCellDetails,
        GameWith6x6GridFromZeroOnPossibleIn3x3,
        GameWith6x6GridFastFinishingMode,
        GameWith7x7GridFromZeroOnPossibleIn3x3,
        NewGameWithRectangularGrid,
        NewGameWithRectangularGridAndFastFinishingMode,
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun testItemProvider(): Array<out TestDataForActivity<out Enum<*>>> =
            TestDataForActivityCombinator(uiStates = UiStateEnum.entries.toTypedArray())
                .forDevices(
                    DeviceScreen.Phone.NEXUS_ONE,
                    DeviceScreen.Phone.SMALL_PHONE,
                    DeviceScreen.Phone.PIXEL_4A,
                    DeviceScreen.Tablet.MEDIUM_TABLET,
                    DeviceScreen.Desktop.LARGE_DESKTOP,
                ).forConfigs(
                    ActivityConfigItem(uiMode = UiMode.DAY, orientation = Orientation.PORTRAIT),
                    ActivityConfigItem(uiMode = UiMode.NIGHT, orientation = Orientation.LANDSCAPE),
                ).combineAll()
                .filterNot {
                    it.config!!.orientation == Orientation.LANDSCAPE &&
                        it.device in
                        listOf(
                            DeviceScreen.Phone.NEXUS_ONE,
                            DeviceScreen.Phone.SMALL_PHONE,
                            DeviceScreen.Phone.PIXEL_4A,
                        )
                }.toTypedArray()
    }

    @get:Rule
    val robolectricScreenshotRule =
        robolectricActivityScenarioForActivityRule<MainActivity>(
            config = testItem.config,
            deviceScreen = testItem.device,
        )

    private val game: Game by inject()
    private val gameLifecycle: GameLifecycle by inject()
    private val preferences: ApplicationPreferences by inject()

    @After
    fun after() {
        stopKoin()
    }

    @Config(sdk = [30]) // Do not use qualifiers if using `DeviceScreen` in the Rule
    @Test
    fun screenshotTest() {
        robolectricScreenshotRule.activityScenario.onActivity {
            preferences.clear()
            // preferences.theme = Theme.SYSTEM_DEFAULT

            onActivityViaUiState()

            gameLifecycle.stoppGameTimerAndResetGameTime()

            while (it.binding.mainBottomAppBar.isLayoutRequested) {
                Thread.sleep(100)
            }
        }

        robolectricScreenshotRule
            .rootView
            .captureRoboImage(ScreenshotTestUtils.filePath(this::class, testItem))
    }

    private fun onActivityViaUiState() {
        when (testItem.uiState) {
            UiStateEnum.NewGame -> {
                preferences.gridTakesRemainingSpaceIfNecessary = false
                game.updateGrid(createDefaultGrid())
            }

            UiStateEnum.NewGameWithCellDetails -> {
                preferences.gridTakesRemainingSpaceIfNecessary = false
                game.updateGrid(createDefaultGrid())

                game.selectCell(game.grid.getCell(0))
                game.enterNumber(1)
                game.grid.getCell(20).possibles = game.grid.variant.possibleDigits
                game.gridUI.invalidate()
            }

            UiStateEnum.GameWith6x6GridFromZeroOnPossibleIn3x3 -> {
                preferences.show3x3Pencils = true
                game.updateGrid(
                    createGrid(
                        GameVariant(
                            GridSize(6, 6),
                            GameOptionsVariant.createClassic(DigitSetting.FIRST_DIGIT_ZERO),
                        ),
                    ),
                )

                game.selectCell(game.grid.getCell(0))
                game.grid.getCell(25).possibles = game.grid.variant.possibleDigits
                game.gridUI.invalidate()
            }

            UiStateEnum.GameWith6x6GridFastFinishingMode -> {
                preferences.gridTakesRemainingSpaceIfNecessary = false

                game.updateGrid(createGrid(11, 11))

                game.selectCell(game.grid.getCell(15))
                game.enterFastFinishingMode()

                game.gridUI.invalidate()
            }

            UiStateEnum.GameWith7x7GridFromZeroOnPossibleIn3x3 -> {
                preferences.show3x3Pencils = true
                game.updateGrid(
                    createGrid(
                        GameVariant(
                            GridSize(7, 7),
                            GameOptionsVariant.createClassic(DigitSetting.FIRST_DIGIT_ZERO),
                        ),
                    ),
                )

                game.selectCell(game.grid.getCell(0))
                game.grid.getCell(22).possibles = game.grid.variant.possibleDigits
                game.gridUI.invalidate()
            }

            UiStateEnum.NewGameWithRectangularGrid -> {
                preferences.gridTakesRemainingSpaceIfNecessary = true
                game.updateGrid(createGrid(11, 11))

                game.gridUI.invalidate()
            }

            UiStateEnum.NewGameWithRectangularGridAndFastFinishingMode -> {
                preferences.gridTakesRemainingSpaceIfNecessary = true
                preferences.useFastFinishingMode = true
                game.updateGrid(createGrid(11, 11))

                game.selectCell(game.grid.getCell(40))
                game.enterFastFinishingMode()

                game.gridUI.invalidate()
            }
        }
    }

    private fun createDefaultGrid(): Grid = createGrid(9, 9)

    private fun createGrid(
        width: Int,
        height: Int,
    ): Grid =
        createGrid(
            GameVariant(GridSize(width, height), GameOptionsVariant.createClassic()),
        )

    private fun createGrid(variant: GameVariant): Grid {
        val randomizer = SeedRandomizerMock(0)

        return GridCreator(
            variant,
            randomizer,
            RandomPossibleDigitsShuffler(randomizer.random),
        ).createRandomizedGridWithCages()
    }
}
