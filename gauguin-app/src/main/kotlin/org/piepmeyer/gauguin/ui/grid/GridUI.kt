package org.piepmeyer.gauguin.ui.grid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import org.koin.core.component.KoinComponent
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.grid.Grid
import org.piepmeyer.gauguin.grid.GridCell
import org.piepmeyer.gauguin.grid.GridSize
import org.piepmeyer.gauguin.grid.GridView
import org.piepmeyer.gauguin.options.DigitSetting
import org.piepmeyer.gauguin.options.GameOptionsVariant
import org.piepmeyer.gauguin.options.GameVariant
import kotlin.math.min
import kotlin.math.sqrt

class GridUI :
    View,
    OnTouchListener,
    GridView,
    KoinComponent {
    private val gridUiInjectionStrategy: GridUiInjectionStrategy = GridUiInjectionFactory.createStreategy(this)

    private val cells = mutableListOf<GridCellUI>()
    private val cages = mutableListOf<GridCageUI>()

    enum class CellShape { Square, Rectangular }

    var cellShape = CellShape.Square
    var isSelectorShown = false

    override var grid =
        Grid(
            GameVariant(
                GridSize(9, 9),
                GameOptionsVariant.createClassic(DigitSetting.FIRST_DIGIT_ZERO),
            ),
        )
        set(value) {
            field = value
            rebuildCellsFromGrid()
            updatePadding()
        }

    private var paintHolder = GridPaintHolder(this, context)
    var isPreviewMode = false
    private var previewStillCalculating = false
    private var maximumCellSizeInDP = gridUiInjectionStrategy.maximumCellSizeInDP()

    private var padding = Pair(0, 0)

    constructor(context: Context?) : super(context) {
        initGridView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initGridView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle,
    ) {
        initGridView()
    }

    private fun initGridView() {
        setOnTouchListener(this)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    fun updateTheme() {
        paintHolder = GridPaintHolder(this, context)
        rebuildCellsFromGrid()

        this.invalidate()
    }

    fun reCreate() {
        if (grid.gridSize.amountOfNumbers < 2) {
            return
        }
        rebuildCellsFromGrid()
        isSelectorShown = false
    }

    fun rebuildCellsFromGrid() {
        cells.clear()
        for (cell in grid.cells) {
            cells.add(GridCellUI(cell, paintHolder))
        }

        cages.clear()
        for (cage in grid.cages) {
            cages.add(GridCageUI(this, cage, paintHolder, gridUiInjectionStrategy.showOperators(), gridUiInjectionStrategy.numeralSystem()))
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val measure = measureGrid(widthMeasureSpec, heightMeasureSpec, grid.gridSize)

        setMeasuredDimension(
            measure.first,
            measure.second,
        )
    }

    private fun measureGrid(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        gridSize: GridSize,
    ): Pair<Int, Int> {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val maximumWidth = (gridSize.width * maximumCellSizeInDP * resources.displayMetrics.density).toInt()
        val maximumHeight = (gridSize.height * maximumCellSizeInDP * resources.displayMetrics.density).toInt()

        println("on measure: $widthMode x $heightMode")
        println("on measure2: $widthSize x $heightSize")

        val measured =
            when {
                widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED -> {
                    println("both unspecified: $maximumWidth x $maximumHeight")

                    Pair(maximumWidth, maximumHeight)
                }
                widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> {
                    println("both exectly: $widthSize x $heightSize")

                    Pair(widthSize, heightSize)
                }
                /*widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST -> {
                    println("most: $widthSize x $heightSize")

                    Pair(widthSize, min(widthSize, heightSize))
                }*/
                else -> {
                    val cellSize = potentialCellSize(widthSize, heightSize)

                    println("else with cellSize $cellSize")
                    println(
                        "else with ${min(
                            widthSize,
                            cellSize.first * gridSize.width,
                        )} x ${min(heightSize, cellSize.second * gridSize.height)}",
                    )

                    Pair(
                        min(widthSize, cellSize.first * gridSize.width),
                        min(heightSize, cellSize.second * gridSize.height),
                    )
                }
            }

        println("measured: ${measured.first} x ${measured.second}")

        return measured
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)

        updatePadding()
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        super.onLayout(changed, left, top, right, bottom)

        updatePadding()
    }

    private fun updatePadding() {
        padding =
            Pair(
                (width - (cellSize.first * grid.gridSize.width)) / 2,
                (height - (cellSize.second * grid.gridSize.height)) / 2,
            )
    }

    override fun onDraw(canvas: Canvas) {
        val layoutDetails =
            GridLayoutDetails(
                cellSizeFloat(),
                paintHolder,
            )

        canvas.drawRoundRect(
            padding.first.toFloat(),
            padding.second.toFloat(),
            padding.first.toFloat() + cellSize.first * grid.gridSize.width,
            padding.second.toFloat() + cellSize.second * grid.gridSize.height,
            layoutDetails.gridPaintRadius(),
            layoutDetails.gridPaintRadius(),
            paintHolder.backgroundPaint(),
        )

        val cellSize = cellSizeFloat()
        val showBadMaths = gridUiInjectionStrategy.showBadMaths()
        val fastFinishMode = gridUiInjectionStrategy.isInFastFinishingMode()
        val numeralSystem = gridUiInjectionStrategy.numeralSystem()
        val markDuplicatedInRowOrColumn = gridUiInjectionStrategy.markDuplicatedInRowOrColumn()

        cells.forEach {
            it.onDraw(canvas, this, cellSize, padding, layoutDetails, fastFinishMode, showBadMaths, markDuplicatedInRowOrColumn)
        }

        cages.forEach {
            it.drawCageBackground(canvas, cellSize, padding, layoutDetails, showBadMaths)
            it.drawCageText(canvas, cellSize, layoutDetails, fastFinishMode)
        }

        cells.forEach {
            it.onDrawForeground(canvas, cellSize, this, padding, layoutDetails, fastFinishMode, numeralSystem)
        }

        if (isPreviewMode) {
            drawPreviewBanner(canvas)
        }
    }

    private fun drawPreviewBanner(canvas: Canvas) {
        val distanceFromEdge =
            min(
                resources.displayMetrics.density * 60,
                (cellSizeFloat().first * grid.gridSize.width) / 2,
            )
        val width = distanceFromEdge * 0.6f

        val previewPath = Path()
        previewPath.moveTo(0f, distanceFromEdge + width)
        previewPath.lineTo(distanceFromEdge + width, 0f)
        previewPath.lineTo(distanceFromEdge, 0f)
        previewPath.lineTo(distanceFromEdge, 0f)
        previewPath.lineTo(0f, distanceFromEdge)
        val cageTextSize = (distanceFromEdge / 3).toInt()

        val textPaint = paintHolder.previewBannerTextPaint()
        textPaint.textSize = cageTextSize.toFloat()

        val previewText =
            if (previewStillCalculating) {
                resources.getText(R.string.new_grid_preview_banner_still_calculating)
            } else {
                resources.getText(R.string.new_grid_preview_banner_already_calculated)
            }.toString()

        val textWidth =
            textPaint.measureText(
                resources
                    .getText(R.string.new_grid_preview_banner_already_calculated)
                    .toString(),
            )

        previewPath.offset(padding.first.toFloat(), padding.second.toFloat())

        canvas.drawPath(previewPath, paintHolder.previewBannerBackgroundPaint())
        canvas.drawTextOnPath(
            previewText,
            previewPath,
            ((distanceFromEdge + width) * sqrt(2f) - textWidth) / 2,
            distanceFromEdge * -0.08f,
            textPaint,
        )
    }

    private val cellSize: Pair<Int, Int>
        get() {
            return potentialCellSize(this.measuredWidth, this.measuredHeight)
        }

    private fun potentialCellSize(
        measuredWidth: Int,
        measuredHeight: Int,
    ): Pair<Int, Int> {
        val cellSizeWidth = (measuredWidth.toFloat() - 2 * BORDER_WIDTH) / grid.gridSize.width.toFloat()
        val cellSizeHeight = (measuredHeight.toFloat() - 2 * BORDER_WIDTH) / grid.gridSize.height.toFloat()
        val maximumCellSize = maximumCellSizeInDP * resources.displayMetrics.density

        return when (cellShape) {
            CellShape.Square -> {
                val cellSizeSquare = min(cellSizeWidth, cellSizeHeight)

                val size = min(cellSizeSquare, maximumCellSize).toInt()

                Pair(size, size)
            }
            CellShape.Rectangular -> {
                when {
                    cellSizeWidth >= maximumCellSize && cellSizeHeight >= maximumCellSize ->
                        Pair(maximumCellSize.toInt(), maximumCellSize.toInt())
                    // cellSizeWidth < maximumCellSize && cellSizeHeight < maximumCellSize ->
                    else -> Pair(cellSizeWidth.toInt(), cellSizeHeight.toInt())
                }
            }
        }
    }

    private fun cellSizeFloat(): Pair<Float, Float> = Pair(cellSize.first.toFloat(), cellSize.second.toFloat())

    override fun onTouch(
        arg0: View,
        event: MotionEvent,
    ): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) {
            return false
        }
        if (!grid.isActive) {
            return false
        }

        getCell(event)?.let {
            isSelectorShown = true
            gridUiInjectionStrategy.cellClicked(it)
        }

        return false
    }

    private fun getCell(event: MotionEvent): GridCell? {
        val x = event.x - padding.first
        val y = event.y - padding.second

        if (x < 0 || y < 0) {
            return null
        }

        val row = (y / cellSize.second).toInt()
        if (row > grid.gridSize.height - 1) {
            return null
        }

        val col = (x / cellSize.first).toInt()
        if (col > grid.gridSize.width - 1) {
            return null
        }

        return grid.getCellAt(row, col)
    }

    fun setPreviewStillCalculating(previewStillCalculating: Boolean) {
        this.previewStillCalculating = previewStillCalculating
    }

    companion object {
        const val BORDER_WIDTH = 1
    }
}
