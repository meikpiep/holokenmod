package org.piepmeyer.gauguin.ui.statistics

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getString
import androidx.fragment.app.commit
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import org.koin.android.ext.android.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.databinding.ActivityStatisticsBinding
import org.piepmeyer.gauguin.preferences.StatisticsManager
import org.piepmeyer.gauguin.ui.ActivityUtils

class StatisticsActivity : AppCompatActivity() {
    private val activityUtils: ActivityUtils by inject()
    private val statisticsManager: StatisticsManager by inject()

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var scatterPlotDiagramFragment: StatisticsScatterPlotDiagramFragment
    private lateinit var difficultyDiagramFragment: StatisticsDifficultyDiagramFragment
    private lateinit var durationDiagramFragment: StatisticsDurationDiagramFragment
    private lateinit var streaksDiagramFragment: StatisticsStreaksDiagramFragment
    private var multiDiagramFragment: StatisticsMultiDiagramFragment? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        activityUtils.configureTheme(this)

        super.onCreate(savedInstanceState)

        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clearstats.setOnClickListener { _: View? ->
            resetStatisticsDialog()
        }

        activityUtils.configureFullscreen(this)

        scatterPlotDiagramFragment = StatisticsScatterPlotDiagramFragment()
        difficultyDiagramFragment = StatisticsDifficultyDiagramFragment()
        durationDiagramFragment = StatisticsDurationDiagramFragment()
        streaksDiagramFragment = StatisticsStreaksDiagramFragment()

        supportFragmentManager.commit {
            if (binding.multiDiagramFrame != null) {
                val fragment =
                    StatisticsMultiDiagramFragment(
                        scatterPlotDiagramFragment,
                        durationDiagramFragment,
                    )

                multiDiagramFragment = fragment
                replace(binding.multiDiagramFrame!!.id, fragment)
            } else {
                binding.scatterPlotCardView?.let { replace(it.id, scatterPlotDiagramFragment) }
                binding.overallDurationCardView?.let { replace(it.id, durationDiagramFragment) }
            }

            binding.overallDifficultyCardView.let { replace(it.id, difficultyDiagramFragment) }
            binding.overallStreaksCardView.let { replace(it.id, streaksDiagramFragment) }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        updateViews()
    }

    private fun updateViews() {
        val overall = statisticsManager.statistics().overall

        val chartsAvailable =
            overall.solvedDifficulty.isNotEmpty() &&
                overall.solvedDuration.isNotEmpty()

        if (chartsAvailable) {
            binding.noStatisticsAvailableYetCardView.visibility = View.INVISIBLE
        } else {
            hideCharts()
            binding.noStatisticsAvailableYetCardView.visibility = View.VISIBLE
        }

        binding.startedstat.text = statisticsManager.totalStarted().toString()
        // binding.hintedstat?.text = statisticsManager.totalHinted().toString()
        binding.solvedstat.text = statisticsManager.totalSolved().toString()
        binding.solvedstreak.text = statisticsManager.currentStreak().toString()
        binding.longeststreak.text = statisticsManager.longestStreak().toString()
    }

    private fun hideCharts() {
        binding.multiDiagramFrame?.visibility = View.INVISIBLE
        binding.overallDifficultyCardView.visibility = View.INVISIBLE
        binding.overallDurationCardView?.visibility = View.INVISIBLE
        binding.overallStreaksCardView.visibility = View.INVISIBLE
    }

    private fun solveRate(): Double =
        if (statisticsManager.totalStarted() == 0) {
            0.0
        } else {
            statisticsManager.totalSolved() * 100.0 / statisticsManager.totalStarted()
        }

    private fun resetStatisticsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.statistics_dialog_reset_statistics_title)
            .setMessage(R.string.statistics_dialog_reset_statistics_message)
            .setNegativeButton(
                R.string.statistics_dialog_reset_statistics_cancel_button,
            ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .setPositiveButton(R.string.statistics_dialog_reset_statistics_ok_button) { _: DialogInterface?, _: Int ->
                run {
                    statisticsManager.clearStatistics()
                    updateViews()
                }
            }.show()
    }

    companion object {
        fun <T : Number> fillChart(
            chartView: CartesianChartView,
            chartData: List<T>,
            average: Double,
            lineColor: Int,
        ) {
            val wrappedChartData = dublicateIfSingleItem(chartData)

            chartView.setModel(
                CartesianChartModel(
                    LineCartesianLayerModel.build {
                        series(wrappedChartData)
                    },
                ),
            )

            addColorToLine(
                chartView,
                lineColor,
            )

            if (wrappedChartData.any { it.toDouble() != average }) {
                val averageLine =
                    HorizontalLine(
                        y = { _ -> average },
                        label = { _ -> getString(chartView.context, R.string.statistics_diagram_threshold_average_value) },
                        line =
                            LineComponent(
                                color = MaterialColors.getColor(chartView.rootView, R.attr.colorCustomColor1),
                                thicknessDp = 2f,
                            ),
                        labelComponent =
                            TextComponent(
                                color = MaterialColors.getColor(chartView.rootView, R.attr.colorCustomColor1),
                            ),
                    )

                chartView.chart =
                    chartView.chart!!.copy(
                        decorations = listOf(averageLine),
                    )
            }
        }

        private fun <T> dublicateIfSingleItem(items: List<T>): List<T> =
            if (items.size == 1) {
                listOf(
                    items.first(),
                    items.first(),
                )
            } else {
                items
            }

        private fun addColorToLine(
            chartView: CartesianChartView,
            foregroundColor: Int,
        ) {
            val fill =
                LineCartesianLayer.LineFill.single(
                    Fill(
                        MaterialColors.getColor(chartView.rootView, foregroundColor),
                    ),
                )

            val oldAxis = chartView.chart!!.startAxis

            chartView.chart =
                CartesianChart(
                    layers =
                        arrayOf(
                            LineCartesianLayer({ _, _ ->
                                LineCartesianLayer.Line(fill)
                            }),
                        ),
                    startAxis = oldAxis,
                )
        }
    }
}
