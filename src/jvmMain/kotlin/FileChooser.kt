package com.github.mnemotechnician.fdel

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import java.io.File
import kotlin.concurrent.thread

/**
 * Creates and shows a file chooser dialog with the provided option.
 *
 * If the user chooses a file or files and confirms the choice, [then] is called.
 *
 * If the user cancels the selection, [then] is called with a null argument.
 * Otherwise, it may contain one or multiple elements depending on whether [multiSelect] is false or true.
 *
 * The [preSelected] parameter can be used to define default selections.
 */
fun FileChooser(
	title: String,
	baseDir: File,
	directoryMode: Boolean,
	multiSelect: Boolean,
	preSelected: List<File>? = null,
	then: (List<File>?) -> Unit
) = thread(isDaemon = true) {
	val selectedFiles = mutableStateListOf<File>()
	if (preSelected != null) {
		selectedFiles.addAll(preSelected.filter(File::exists))

		require(!directoryMode || selectedFiles.all { it.isDirectory }) { "Default selection contains files in directory mode." }
		require(directoryMode || selectedFiles.all { it.isFile }) { "Default selection contains directories in file mode." }
		require(multiSelect || selectedFiles.size <= 1) { "Default selection contains multiple files in single-file mode." }
	}

	application(exitProcessOnExit = false) {
		Window(onCloseRequest = {
			then(null)
			exitApplication()
		}, title = title) {
			FileChooserApp(baseDir, directoryMode, multiSelect, selectedFiles) {
				then(it)
				exitApplication()
			}
		}

	}
}

@Composable
private fun FileChooserApp(
	baseDir: File,
	directoryMode: Boolean,
	multiSelect: Boolean,
	selectedFiles: SnapshotStateList<File>,
	completionFunction: (List<File>?) -> Unit
) {
	val currentDirState = remember {
		require(baseDir.isDirectory && baseDir.exists()) {
			"Invalid base directory: $baseDir"
		}
		mutableStateOf(baseDir)
	}
	val showHiddenState = remember { mutableStateOf(false) }

	MaterialTheme(colors = themeColors) {
		Surface(Modifier.fillMaxSize(), elevation = 3.dp) {
			Column {
				Surface(Modifier.padding(15.dp).height(Max).fillMaxWidth(), elevation = 2.dp) {
					TopBar(currentDirState, showHiddenState)
				}

				Box(Modifier.weight(1f)) {
					FileList(currentDirState, showHiddenState, selectedFiles, multiSelect, directoryMode, completionFunction)
				}

				Surface(Modifier.fillMaxWidth(), elevation = 2.dp) {
					BottomBar(currentDirState, selectedFiles, multiSelect, directoryMode, completionFunction)
				}
			}
		}
	}
}

@Composable
private fun TopBar(
	currentDirState: MutableState<File>,
	showHiddenState: MutableState<Boolean>
) {
	var currentDir by currentDirState
	var showHidden by showHiddenState
	var location by remember(currentDir) { mutableStateOf(currentDir.absolutePath) }

	Row(Modifier.padding(5.dp)) {
		// Up button
		OutlinedButton(onClick = {
			currentDir = currentDir.parentFile
		}, Modifier.fillMaxHeight(), enabled = currentDir.parent != null) {
			Icon(Icons.Default.KeyboardArrowUp, "Go up")
		}

		// Home button
		OutlinedButton(onClick = {
			currentDir = File(System.getProperty("user.home")!!)
		}, Modifier.fillMaxHeight()) {
			Icon(Icons.Default.Home, "Home")
		}

		// Show/hide hidden files
		OutlinedButton(onClick = {
			showHidden = !showHidden
		}, Modifier.fillMaxHeight()) {
			Icon(if (showHidden) R.Icon.eye else R.Icon.noEye, "Show hidden files")
		}

		// Location bar
		val onSurfaceColor = MaterialTheme.colors.onSurface
		val errorColor = MaterialTheme.colors.error
		var locationColor by remember { mutableStateOf(onSurfaceColor) }

		OutlinedTextField(
			location,
			textStyle = TextStyle(color = locationColor),
			modifier = Modifier.fillMaxWidth(),
			maxLines = 1,
			onValueChange = {
				val new = File(it)
				if (new.exists() && new.isDirectory) {
					currentDir = new
					locationColor = onSurfaceColor
				} else {
					locationColor = errorColor
				}
				location = it
			}
		)
	}
}

@Composable
private fun FileList(
	currentDirState: MutableState<File>,
	showHiddenState: MutableState<Boolean>,
	selectedFiles: SnapshotStateList<File>,
	multiSelect: Boolean,
	directoryMode: Boolean,
	completionFunction: (List<File>) -> Unit
) {
	var currentDir by currentDirState
	val showHidden by showHiddenState

	val files by derivedStateOf {
		currentDir.listFiles().orEmpty().sortedWith { a, b ->
			when {
				a.isDirectory && !b.isDirectory -> -1
				b.isDirectory && !a.isDirectory -> 1
				else -> a.name.compareTo(b.name)
			}
		}.filter {
			!directoryMode || it.isDirectory // only show directories in directory mode
		}.filter {
			showHidden || !it.name.startsWith(".")
		}
	}

	val lastClicks by remember { derivedStateOf {
		mutableStateMapOf(currentDir to 0L) // the single value is to make this state derived
	} }
	// This modifier is called when a file tile is clicked
	fun clickedMod(file: File) = Modifier.clickable(role = Role.Button) {
		val now = System.currentTimeMillis()
		val lastClick = lastClicks[file] ?: 0L
		val isDoubleClick = now - lastClick < 350L
		lastClicks[file] = now

		when {
			file.isDirectory -> {
				// Select/deselect on single click, enter on double click
				if (isDoubleClick) {
					currentDir = file
				} else if (directoryMode) {
					when {
						multiSelect && file !in selectedFiles -> selectedFiles += file
						multiSelect && file in selectedFiles -> selectedFiles -= file
						!multiSelect -> {
							selectedFiles.clear()
							selectedFiles += file
						}
					}
				}
			}
			file.isFile && !directoryMode -> {
				if (isDoubleClick && !multiSelect) {
					completionFunction(listOf(file))
				} else {
					when {
						multiSelect && file in selectedFiles -> selectedFiles -= file
						multiSelect && file !in selectedFiles -> selectedFiles += file
						!multiSelect -> {
							selectedFiles.clear()
							selectedFiles += file
						}
					}
				}
			}
		}
	}

	LazyVerticalGrid(
		GridCells.Adaptive(130.dp),
		Modifier.fillMaxSize()
	) {
		items(files) { file ->
			Surface(
				Modifier
					.padding(5.dp)
					.aspectRatio(1f)
					.then(clickedMod(file))
					.then(when {
						file in selectedFiles -> Modifier
							.border(2.dp, MaterialTheme.colors.primary)
							.padding(2.dp)
						else -> Modifier // empty
					}),
				elevation = 2.dp
			) {
				Box(contentAlignment = Center) {
					Column(horizontalAlignment = CenterHorizontally) {
						val color = when {
							file.name.startsWith(".") -> Color.Gray
							else -> MaterialTheme.colors.onSurface
						}

						Icon(when {
							file.isDirectory -> R.Icon.folder
							else -> R.Icon.file
						}, null, Modifier.size(80.dp), tint = color)

						Spacer(Modifier.height(5.dp))

						Text(
							file.name,
							Modifier.align(CenterHorizontally),
							maxLines = 2,
							fontSize = 12.sp,
							color = color
						)
					}
				}
			}
		}
	}
}

@Composable
private fun BottomBar(
	currentDirState: MutableState<File>,
	selectedFiles: SnapshotStateList<File>,
	multiSelect: Boolean,
	directoryMode: Boolean,
	completionFunction: (List<File>?) -> Unit
) {
	val currentDir by currentDirState

	val canConfirm = (directoryMode && !multiSelect) || selectedFiles.isNotEmpty()
	val usingThisDirectory = directoryMode && !multiSelect && (selectedFiles.isEmpty() || selectedFiles[0] == currentDir)

	val text by remember { derivedStateOf {
		val fdString = if (directoryMode) "directory" else "file"
		when {
			!canConfirm -> "No $fdString is selected"
			usingThisDirectory -> ""
			selectedFiles.size == 1 -> "Selected $fdString: ${selectedFiles.single().relativeTo(currentDir)}"
			else -> "Selected ${selectedFiles.size} $fdString entries"
		}
	} }

	Row {
		Text(text, Modifier.weight(1f).padding(10.dp).width(Min))

		Column(Modifier.padding(10.dp).width(Max)) {
			// Cancel button
			OutlinedButton(onClick = {
				completionFunction(null)
			}, Modifier.fillMaxWidth()) {
				Text("Cancel")
			}

			Button(onClick = {
				if (usingThisDirectory) {
					completionFunction(listOf(currentDir))
				} else {
					completionFunction(selectedFiles.toList())
				}
			}, Modifier.fillMaxWidth(), enabled = canConfirm) {
				Text(when {
					usingThisDirectory -> "Select this directory"
					else -> "Confirm selection"
				}, maxLines = 1)
			}
		}
	}
}
