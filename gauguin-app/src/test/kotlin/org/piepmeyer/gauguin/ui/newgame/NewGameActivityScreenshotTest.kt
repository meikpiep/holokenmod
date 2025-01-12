package org.piepmeyer.gauguin.ui.newgame

import com.github.takahirom.roborazzi.captureRoboImage
import com.google.android.material.tabs.TabLayout
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.piepmeyer.gauguin.MainApplication
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.ScreenshotTest
import org.piepmeyer.gauguin.ScreenshotTestUtils
import org.piepmeyer.gauguin.Theme
import org.piepmeyer.gauguin.creation.GridCreator
import org.piepmeyer.gauguin.creation.RandomPossibleDigitsShuffler
import org.piepmeyer.gauguin.creation.SeedRandomizerMock
import org.piepmeyer.gauguin.grid.Grid
import org.piepmeyer.gauguin.grid.GridSize
import org.piepmeyer.gauguin.options.DifficultySetting
import org.piepmeyer.gauguin.options.DigitSetting
import org.piepmeyer.gauguin.options.GameOptionsVariant
import org.piepmeyer.gauguin.options.GameVariant
import org.piepmeyer.gauguin.options.GridCageOperation
import org.piepmeyer.gauguin.options.NumeralSystem
import org.piepmeyer.gauguin.options.SingleCageUsage
import org.piepmeyer.gauguin.preferences.ApplicationPreferences
import org.piepmeyer.gauguin.ui.grid.GridUI
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.uitesting.robolectric.activityscenario.robolectricActivityScenarioForActivityRule
import sergio.sastre.uitesting.robolectric.config.screen.DeviceScreen
import sergio.sastre.uitesting.robolectric.utils.activity.TestDataForActivity
import sergio.sastre.uitesting.robolectric.utils.activity.TestDataForActivityCombinator
import sergio.sastre.uitesting.utils.activityscenario.ActivityConfigItem
import sergio.sastre.uitesting.utils.common.FontSize
import sergio.sastre.uitesting.utils.common.Orientation
import sergio.sastre.uitesting.utils.common.UiMode

@Category(ScreenshotTest::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NewGameActivityScreenshotTest(
    private val testItem: TestDataForActivity<UiStateEnum>,
) {
    enum class UiStateEnum {
        TabBasic,
        TabNumbers,
        TabAdvanced,
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
                    ActivityConfigItem(
                        uiMode = UiMode.DAY,
                        fontSize = FontSize.NORMAL,
                        orientation = Orientation.PORTRAIT,
                    ),
                    ActivityConfigItem(
                        uiMode = UiMode.DAY,
                        fontSize = FontSize.NORMAL,
                        orientation = Orientation.LANDSCAPE,
                    ),
                ).combineAll()
    }

    @get:Rule
    val robolectricScreenshotRule =
        robolectricActivityScenarioForActivityRule<NewGameActivity>(
            config = testItem.config,
            deviceScreen = testItem.device,
        )

    @Before
    fun before() {
        MainApplication.testOverideModule =
            module {
                single {
                    mockk<ApplicationPreferences>(relaxed = true) {
                        every { theme } returns Theme.LIGHT
                        every { difficultySetting } returns DifficultySetting.ANY
                        every { digitSetting } returns DigitSetting.FIRST_DIGIT_ONE
                        every { numeralSystem } returns NumeralSystem.Decimal
                        every { operations } returns GridCageOperation.OPERATIONS_ALL
                        every { singleCageUsage } returns SingleCageUsage.FIXED_NUMBER
                        every { gridWidth } returns 9
                        every { gridHeigth } returns 9
                        every { gameVariant } returns GameOptionsVariant.createClassic()
                    }
                } withOptions { binds(listOf(ApplicationPreferences::class)) }
            }
    }

    @After
    fun after() {
        stopKoin()
    }

    @Config(sdk = [34]) // Do not use qualifiers if using `DeviceScreen` in the Rule
    @Test
    fun screenshotTest() {
        robolectricScreenshotRule.activityScenario.onActivity {
            it.findViewById<GridUI>(R.id.newGridPreview).grid = createDefaultGrid()

            val tabs = it.findViewById<TabLayout>(R.id.new_game_options_tablayout)

            when (testItem.uiState) {
                UiStateEnum.TabBasic -> tabs.selectTab(tabs.getTabAt(0))
                UiStateEnum.TabNumbers -> tabs.selectTab(tabs.getTabAt(1))
                UiStateEnum.TabAdvanced -> tabs.selectTab(tabs.getTabAt(2))
            }
        }

        robolectricScreenshotRule
            .rootView
            .captureRoboImage(ScreenshotTestUtils.filePath(this::class, testItem))
    }

    private fun createDefaultGrid(): Grid =
        createGrid(
            GameVariant(GridSize(6, 6), GameOptionsVariant.createClassic()),
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
