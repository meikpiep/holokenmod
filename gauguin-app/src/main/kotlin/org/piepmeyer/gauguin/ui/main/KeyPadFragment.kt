package org.piepmeyer.gauguin.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.game.Game
import org.piepmeyer.gauguin.ui.WindowClassCalculator
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class KeyPadFragment :
    Fragment(),
    KoinComponent {
    private val game: Game by inject()

    private val numbers = mutableListOf<MaterialButton>()

    private val numberButtonToDigit = mutableMapOf<MaterialButton, Int>()

    private lateinit var layoutCalculator: KeyPadLayoutCalculator
    private var layoutId by Delegates.notNull<Int>()
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        layoutCalculator = KeyPadLayoutCalculator(WindowClassCalculator(requireActivity()))
        layoutId = layoutCalculator.calculateLayoutId(game.grid)

        rootView = inflater.inflate(layoutId, parent, false)

        numbers += rootView.findViewById<MaterialButton>(R.id.button1)
        numbers += rootView.findViewById<MaterialButton>(R.id.button2)
        numbers += rootView.findViewById<MaterialButton>(R.id.button3)
        numbers += rootView.findViewById<MaterialButton>(R.id.button4)
        numbers += rootView.findViewById<MaterialButton>(R.id.button5)
        numbers += rootView.findViewById<MaterialButton>(R.id.button6)
        numbers += rootView.findViewById<MaterialButton>(R.id.button7)
        numbers += rootView.findViewById<MaterialButton>(R.id.button8)
        numbers += rootView.findViewById<MaterialButton>(R.id.button9)
        numbers += rootView.findViewById<MaterialButton>(R.id.button10)
        numbers += rootView.findViewById<MaterialButton>(R.id.button11)
        numbers += rootView.findViewById<MaterialButton>(R.id.button12)

        return rootView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        numbers.forEach {
            addButtonListeners(it)
        }

        if (layoutId != layoutCalculator.calculateLayoutId(game.grid)) {
            // requireActivity().recreate()
            this.requireView().forceLayout()
        }

        val viewModel: MainViewModel by viewModels()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    requireActivity().runOnUiThread {
                        setButtonStates()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fastFinishingModeState.collect {
                    requireActivity().runOnUiThread {
                        setButtonStates()
                    }
                }
            }
        }
    }

    private fun addButtonListeners(numberButton: MaterialButton) {
        numberButton.setOnClickListener {
            game.enterPossibleNumber(numberButtonToDigit.getValue(numberButton))
        }
        numberButton.setOnLongClickListener {
            game.enterNumber(numberButtonToDigit.getValue(numberButton))
            true
        }
    }

    private fun setButtonStates() {
        val visibleRows = ceil(game.grid.variant.possibleDigits.size / 3.0).toInt()
        val lastVisibleNumber = visibleRows * 3 - 1

        val digitSetting = game.grid.variant.options.digitSetting
        val digits = digitSetting.numbers.toMutableList()

        if (digitSetting.zeroOnKeyPadShouldBePlacedAtLast()) {
            digits.remove(0)
            digits.add(lastVisibleNumber, 0)
        }

        val digitsIterator = digits.iterator()

        var i = 0

        numberButtonToDigit.clear()

        numbers.forEach {
            val digit = digitsIterator.next()

            numberButtonToDigit[it] = digit

            it.text = possibleMultiLneText(digit)

            it.visibility =
                when {
                    (i <= lastVisibleNumber && !game.isInFastFinishingMode()) -> View.VISIBLE
                    (i <= lastVisibleNumber && game.isInFastFinishingMode()) -> View.INVISIBLE
                    else -> View.GONE
                }
            it.isEnabled =
                game.grid.variant.possibleDigits
                    .contains(digit)
            i++
        }

        /*val padding = layoutCalculator.calculateLayoutMarginBottom(game.grid)
        rootView.updateLayoutParams<ViewGroup.MarginLayoutParams> { }
        rootView.setPaddingRelative(0, padding, 0, padding)
        rootView.invalidate()*/
    }

    private fun possibleMultiLneText(digit: Int): String {
        var text =
            game.grid.variant.options.numeralSystem
                .displayableString(digit)

        if (text.length > 4) {
            val cutTextIndex = (text.length / 2 + 0.4).roundToInt()

            text = "${text.subSequence(0, cutTextIndex)}\n${
                text.subSequence(
                    cutTextIndex,
                    text.length,
                )
            }"
        }
        return text
    }
}
