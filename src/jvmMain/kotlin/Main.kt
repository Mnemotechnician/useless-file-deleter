package com.github.mnemotechnician.fdel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import kotlinx.coroutines.*
import java.awt.Frame
import java.io.File

var themeColors: Colors by mutableStateOf(darkColors())
var baseDirectory by mutableStateOf(File(System.getProperty("user.home")!!))
val selectedFiles = mutableStateListOf<File>()

val events = mutableStateListOf<ComposableEvent>()

fun main() = application {
	Window(onCloseRequest = ::exitApplication) {
		App(window)
	}
}

@Composable
@Preview
fun App(window: Frame) {
	if (events.isNotEmpty()) {
		val iterator = events.listIterator()
		iterator.forEach {
			if (!it.invoke()) iterator.remove()
		}
	}

	MaterialTheme(colors = themeColors) {
		Surface(Modifier.fillMaxSize(), elevation = 3.dp) {
			Column {
				Text(
					modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp),
					text = "The Ultimate Auto File Deleter",
					letterSpacing = 5f.sp,
					fontSize = 30.sp
				)

				Spacer(Modifier.height(20.dp))

				Row(Modifier.weight(1f)) {
					// The button list
					Column(
						Modifier
							.fillMaxHeight()
							.padding(10.dp)
							.border(1.dp, MaterialTheme.colors.onSurface)
							.width(Max)
					) {
						val modifier = Modifier.fillMaxWidth().padding(10.dp)

						Button(::selectFiles, modifier) {
							Text("Select files")
						}

						Button(::deleteSelectedFiles, modifier) {
							Text("Delete selected")
						}

						Button({ deleteAllFiles() }, modifier) {
							Text("Delete all files")
						}
					}

					// The file list
					Box(Modifier
						.padding(10.dp)
						.weight(1f)
						.border(1.dp, MaterialTheme.colors.onSurface)
						.padding(10.dp)
					) {
						val listState = rememberLazyListState(0)
						LazyColumn(
							 // after border
							state = listState
						) {
							items(selectedFiles) {
								Text(
									it.relativeTo(baseDirectory).path,
									maxLines = 1
								)
							}
						}

						VerticalScrollbar(ScrollbarAdapter(listState), Modifier.fillMaxHeight().align(Alignment.CenterEnd))
					}
				}

				Spacer(Modifier.weight(1f))

				Row(
					Modifier
						.fillMaxWidth()
						.padding(10.dp)
				) {
					Text("Base directory: ${baseDirectory.absolutePath}", Modifier.weight(1f).width(Min))

					Button(onClick = ::chooseBaseDirectory) {
						Text("Choose base directory", maxLines = 1)
					}
				}
			}
		}
	}
}

fun fire(event: ComposableEvent) = events.add(event)

fun chooseBaseDirectory() {
	FileChooser("Choose a base directory", baseDirectory, true, false) { files ->
		files?.singleOrNull()?.let { baseDirectory = it }
	}

}

fun selectFiles() {
	FileChooser("Choose files", baseDirectory, false, true, preSelected = selectedFiles) { files ->
		if (files != null) {
			selectedFiles.clear()
			selectedFiles.addAll(files)
		}
	}
}

fun deleteSelectedFiles() {
	selectedFiles.forEach { it.delete() }
	selectedFiles.clear()
}

@OptIn(DelicateCoroutinesApi::class)
fun deleteAllFiles() = GlobalScope.launch {
	delay(100L)
	fire {
		var shown by remember { mutableStateOf(true) }

		Dialog({ shown = false }, DialogProperties(true, true)) {
			MaterialTheme(colors = themeColors) {
				Surface(Modifier.background(MaterialTheme.colors.surface)) {
					Column(Modifier.padding(20.dp)) {
						Icon(
							Icons.Default.Warning,
							null,
							Modifier.align(Alignment.CenterHorizontally),
							tint = MaterialTheme.colors.error
						)

						Spacer(Modifier.height(20.dp))

						Text("I have no damn idea what this button is supposed to do. 'rm -rf /'?")
					}
				}
			}
		}

		shown
	}
}

/**
 * An event that will be dispatched on the next composition.
 * If the event returns false, it's removed from the composition, otherwise it stays.
 */
fun interface ComposableEvent {
	@Composable
	fun invoke(): Boolean
}
