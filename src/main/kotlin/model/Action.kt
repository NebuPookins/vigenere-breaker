package model

sealed class Action

data class UpdateCiphertextAction(
	val cipherText: String
) : Action()

data class UpdateKeyAction(
	val key: String
) : Action()

object IncrementSlider: Action()

object DecrementSlider: Action()

data class HighlightNormalizedIndex(
	val index: Int?
): Action()