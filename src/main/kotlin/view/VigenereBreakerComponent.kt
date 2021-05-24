package view

import Config
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseOverFunction
import model.*
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.get
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*

external interface VigenereBreakerRProps : RProps {
	var state: RootState
	var updateFn: (Action) -> Unit
}

object UnitState : RState

class VigenereBreakerComponent(props: VigenereBreakerRProps) : RComponent<VigenereBreakerRProps, UnitState>(props) {
	override fun RBuilder.render() {
		div(classes = "vigenere-root") {
			main {
				div(classes = "container") {
					div(classes = "row") {
						div(classes = "col") {
							h1 {
								+"Vigenère Slider"
							}
							val alphabet = 'A'..'Z'
							div(classes = "vigenere-slider") {
								div(classes = "vigenere-slider-left") {
									button(classes = "btn btn-primary btn-lg") {
										+"⬅"
										attrs {
											onClickFunction = { _ ->
												props.updateFn(IncrementSlider)
											}
										}
									}
								}
								div(classes = "vigenere-slider-letters") {
									+alphabet.joinToString("")
									div(classes = "vigenere-slider-bottom") {
										+alphabet
											.dropWhile { it != props.state.sliderStartChar }
											.joinToString("")
										+alphabet
											.take(props.state.sliderStartChar - 'A')
											.joinToString("")
									}
								}
								div(classes = "vigenere-slider-right") {
									button(classes = "btn btn-primary btn-lg") {
										+"➡"
										attrs {
											onClickFunction = { _ ->
												props.updateFn(DecrementSlider)
											}
										}
									}
								}
							}
						}
						div(classes = "col") {
							textArea(classes = "form-control notes") {
								attrs {
									rows = "5"
									defaultValue = "Write any temporary notes you want here"
									spellCheck = false
									wrap = TextAreaWrap.soft
								}
							}
						}
					}
					h1 {
						+"Ciphertext"
					}
					textArea(classes = "form-control") {
						attrs {
							value = props.state.ciphertext
							onChangeFunction = { event ->
								val newValue = (event.target as HTMLTextAreaElement).value
								props.updateFn(UpdateCiphertextAction(newValue))
							}
						}
					}
					if (Config.showFullyNormalizedCypherText) {
						h1 {
							+"Fully normalized ciphertext"
						}
						textArea(classes = "form-control") {
							attrs {
								value = props.state.fullyNormalizedCipherText
								readonly = true
							}
						}
					}

					h1 {
						+"Repeated words"
					}
					ul {
						props.state.repeatedWords
							.renderEach {
								li {
									+"${it.first} "
									+"("
									+"${it.second} times"
									+"; at positions: ${props.state.repeatedWordIndices[it.first]?.joinToString(", ")}"
									+"; separations: ${props.state.repeatedWordSpacing[it.first]?.joinToString(", ")}"
									+")"
									attrs {
										key = it.first
									}
								}
							}
					}
					h1 {
						+"Key"
					}
					div(classes = "row") {
						div(classes = "col-sm-10") {
							input(type = InputType.text, classes = "form-control") {
								attrs {
									value = props.state.key
									onChangeFunction = { event ->
										val newValue =
											(event.target as HTMLInputElement).value
												.toUpperCase()
										props.updateFn(UpdateKeyAction(newValue))
									}
								}
							}
						}
						div(classes = "col-sm-2") {
							+"${props.state.key.length} character${if (props.state.key.length == 1) "" else "s"}"
						}
					}
					div(classes = "row") {
						div(classes = "col") {
							h1 {
								+"Plaintext in grid"
							}
							div(classes = "plaintext-grid") {
								val gridWidth =
									if (props.state.key.isEmpty()) {
										5
									} else {
										props.state.key.length
									}
								var gridX = 0
								var brKey = 0
								props.state.fullyNormalizedAnnotatedPlainText.forEachIndexed { index, annotatedChar ->
									val classes: MutableSet<String> =
										if (annotatedChar.explicitlyMapped) {
											mutableSetOf("mapped")
										} else {
											mutableSetOf("unmapped")
										}
									if (index == props.state.normalizedIndexHighlight) {
										classes.add("highlight")
									}
									span(classes = classes.joinToString(" ")) {
										+annotatedChar.plain.toString()
										attrs["data-normalized-index"] = index
										attrs {
											key = index.toString()
											onMouseOverFunction = { event ->
												val span = event.target as? HTMLSpanElement
												props.updateFn(HighlightNormalizedIndex(span?.let {
													it.dataset["normalizedIndex"]?.toIntOrNull(10)
												}))
											}
										}
									}
									gridX += 1
									if (gridX >= gridWidth) {
										br {
											attrs {
												key = "br-$brKey"
												brKey += 1
											}
										}
										gridX = 0
									}
								}
							}
						}
						div(classes = "col") {
							h1 {
								+"Plaintext in original context"
							}
							val gridWidth =
								if (props.state.key.isEmpty()) {
									Int.MAX_VALUE
								} else {
									props.state.key.length
								}
							var normalizedLetterIndex = 0
							for (annotatedChar in props.state.annotatedPlaintext) {
								val classes: MutableSet<String> =
									if (annotatedChar.explicitlyMapped) {
										mutableSetOf("mapped")
									} else {
										mutableSetOf("unmapped")
									}
								if (normalizedLetterIndex == props.state.normalizedIndexHighlight && annotatedChar.isLetter) {
									classes.add("highlight")
								}
								span(classes = classes.joinToString(" ")) {
									+annotatedChar.plain.toString()
									attrs["data-normalized-index"] = normalizedLetterIndex
									if (annotatedChar.isLetter) {
										val columnNumber = ((normalizedLetterIndex % gridWidth) + 1).toString()
										attrs.title = "$columnNumber: ${annotatedChar.cipher} -> ${annotatedChar.plain}"
										normalizedLetterIndex += 1
									}
									attrs {
										onMouseOverFunction = { event ->
											val span = event.target as? HTMLSpanElement
											props.updateFn(HighlightNormalizedIndex(span?.let {
												it.dataset["normalizedIndex"]?.toIntOrNull(10)
											}))
										}
									}
								}
							}
						}
					}
				}
			}
			footer {
				div(classes = "container") {
					div(classes = "row") {
						div(classes = "col-10") {
							+"Copyright 2021 © Nebu Pookins ("
							a {
								+"nebupookins@gmail.com"
								attrs {
									href =
										"mailto:nebupookins@gmail.com?subject=Vigenère Breaker&body=Hi, I just tried out your Vigenère Breaker app at ${window.location.href} and..."
									target = "_blank"
								}
							}
							+") "
							a {
								+"GitHub Repo"
								attrs {
									href = "https://github.com/NebuPookins/vigenere-breaker"
									target = "_blank"
								}
							}
						}
						div(classes = "col-2") {
							a(classes = "btn btn-primary btn-sm") {
								+"What the heck is this?"
								attrs {
									href = "https://www.youtube.com/watch?v=6qXFwH_JXeY"
									target = "_blank"
								}
							}
						}
					}
				}
			}
		}
	}
}