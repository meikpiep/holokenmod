package org.piepmeyer.gauguin.ui.newgame

import kotlinx.coroutines.CoroutineScope
import org.piepmeyer.gauguin.calculation.GridPreviewCalculationService
import org.piepmeyer.gauguin.grid.Grid
import org.piepmeyer.gauguin.options.GameVariant

class GridPreviewCalculationServiceMock(
    private val grid: Grid,
) : GridPreviewCalculationService() {
    override fun calculateGrid(
        variant: GameVariant,
        scope: CoroutineScope,
    ) {
        listeners.forEach {
            it.previewGridCalculated(grid)
        }
    }
}
