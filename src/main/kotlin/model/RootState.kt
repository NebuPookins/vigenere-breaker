package model

data class AnnotatedCharacter(
	val cipher: Char,
	val plain: Char,
	val explicitlyMapped: Boolean,
) {
	val isLetter: Boolean = when (plain) {
		in 'A'..'Z' -> true
		in 'a'..'z' -> true
		else -> false
	}
}

data class RootState(
	val sliderStartChar: Char = 'A',
	val ciphertext: String = "",
	val key: String = "",
	val normalizedIndexHighlight: Int? = null
) {
	companion object {
		fun filterAllButUpperCaseEnglish(input: String): String =
			input
				.asSequence()
				.filter { char -> char in 'A'..'Z' }
				.joinToString("")
	}

	private val caseFoldedCipherText: String = ciphertext.toUpperCase()

	val fullyNormalizedCipherText: String = filterAllButUpperCaseEnglish(caseFoldedCipherText)

	private val wordHistogram: Map<String, Int> by lazy {
		caseFoldedCipherText
			.split("[^a-zA-Z]".toRegex())
			.filter { it.isNotBlank() }
			.groupBy { it }
			.mapValues { it.value.size }
	}
	val repeatedWords: List<Pair<String, Int>> by lazy {
		wordHistogram
			.filter { it.value > 1 }
			.map { Pair(it.key, it.value) }
			.sortedByDescending { it.second }
			.toList()
	}
	val repeatedWordIndices: Map<String, List<Int>> by lazy {
		val retVal = mutableMapOf<String, MutableList<Int>>()
			.withDefault { mutableListOf() }
		repeatedWords.forEach { repeatedWord ->
			retVal[repeatedWord.first] = retVal.getValue(repeatedWord.first)
			var startIndex = 0
			repeat(repeatedWord.second) {
				val index = fullyNormalizedCipherText.indexOf(repeatedWord.first, startIndex)
				startIndex = index + 1
				retVal.getValue(repeatedWord.first).add(index)
			}
		}
		retVal
	}
	val repeatedWordSpacing: Map<String, List<Int>> by lazy {
		repeatedWordIndices.mapValues { entry ->
			val wordIndices = entry.value
			val retVal = mutableListOf<Int>()
			for (i in 0 until (wordIndices.size - 1)) {
				retVal.add(wordIndices[i + 1] - wordIndices[i])
			}
			retVal
		}
	}

	val annotatedPlaintext: List<AnnotatedCharacter> by lazy {
		var keyIndex = 0
		fun mapChar(cipherChar: Char, firstLetter: Char): AnnotatedCharacter {
			val cipher26 = cipherChar - firstLetter
			val keyOffset: Int
			val explicitlyMapped: Boolean
			when {
				key.isEmpty() -> {
					keyOffset = 0
					explicitlyMapped = false
				}
				key[keyIndex] == '?' -> {
					keyOffset = 0
					explicitlyMapped = false
				}
				else -> {
					keyOffset = key[keyIndex] - 'A'
					explicitlyMapped = true
				}
			}
			val plain26 = (cipher26 + 26 - keyOffset) % 26
			keyIndex =
				if (key.isEmpty()) {
					0
				} else {
					(keyIndex + 1) % key.length
				}
			return AnnotatedCharacter(cipherChar, firstLetter + plain26, explicitlyMapped)
		}
		ciphertext.asSequence()
			.map { char ->
				when (char) {
					in 'a'..'z' -> mapChar(char, 'a')
					in 'A'..'Z' -> mapChar(char, 'A')
					else -> AnnotatedCharacter(char, char, false)
				}
			}.toList()
	}

	val fullyNormalizedAnnotatedPlainText: List<AnnotatedCharacter> by lazy {
		annotatedPlaintext
			.filter {
				it.plain in 'a'..'z' || it.plain in 'A'..'Z'
			}.map { it.copy(plain = it.plain.toUpperCase()) }
	}
}