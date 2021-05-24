import react.dom.render
import kotlinx.browser.document
import kotlinx.browser.window
import model.*
import view.VigenereBreakerComponent

var globalState = RootState()

private fun updateState(action: Action) {
	@Suppress("UNUSED_VARIABLE")
	val ensureExhaustive = when (action) {
		is UpdateCiphertextAction -> globalState = globalState.copy(ciphertext = action.cipherText)
		is UpdateKeyAction -> globalState = globalState.copy(key = action.key)
		IncrementSlider -> globalState = globalState.copy(
			sliderStartChar =
			if (globalState.sliderStartChar >= 'Z') {
				'A'
			} else {
				globalState.sliderStartChar + 1
			}
		)
		DecrementSlider ->  globalState = globalState.copy(
			sliderStartChar =
			if (globalState.sliderStartChar <= 'A') {
				'Z'
			} else {
				globalState.sliderStartChar - 1
			}
		)
		is HighlightNormalizedIndex -> globalState = globalState.copy(normalizedIndexHighlight = action.index)
	}
	updateUi()
}

private fun updateUi() {
	render(document.getElementById("vigenere-breaker")) {
		child(VigenereBreakerComponent::class) {
			attrs {
				this.state = globalState
				this.updateFn = ::updateState
			}
		}
	}
}

fun main() {
	window.onload = {
		updateUi()
	}
}
