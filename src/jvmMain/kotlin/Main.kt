package com.github.mnemotechnician.fdel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import org.jetbrains.skiko.setSystemLookAndFeel
import java.awt.FileDialog
import java.awt.FileDialog.*
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

fun main() = application {
	Window(onCloseRequest = ::exitApplication) {
		App(window)
	}
}

var baseDirectory by mutableStateOf(File(System.getProperty("user.home")!!))
val selectedFiles = mutableStateListOf<File>()

@Composable
@Preview
fun App(window: Frame) {
	MaterialTheme(colors = darkColors()) {
		Surface(Modifier.fillMaxSize(), elevation = 3.dp) {
			Column {
				Text(
					modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp),
					text = "The Ultimate Auto File Deleter",
					letterSpacing = 5f.sp,
					fontSize = 30.sp
				)

				Spacer(Modifier.height(20.dp))

				Row(Modifier.height(IntrinsicSize.Min)) {
					// The button list
					Column(
						Modifier
							.padding(10.dp)
							.border(1.dp, MaterialTheme.colors.onSurface)
							.width(IntrinsicSize.Max)
					) {
						val modifier = Modifier.fillMaxWidth().padding(10.dp)

						Button(::selectFiles, modifier) {
							Text("Select files")
						}

						Button(::deleteSelectedFiles, modifier) {
							Text("Delete selected")
						}

						Button(::deleteAllFiles, modifier) {
							Text("Delete all files")
						}
					}

					// The file list
					Column(
						Modifier
							.padding(10.dp)
							.fillMaxHeight()
							.border(1.dp, MaterialTheme.colors.onSurface)
							.verticalScroll(ScrollState(0))
							.padding(10.dp) // after border
							.weight(1f)
					) {
						selectedFiles.forEach {
							Text(
								it.relativeTo(baseDirectory).path,
								maxLines = 1
							)
						}
					}
				}

				Spacer(Modifier.weight(1f))

				Row(Modifier.fillMaxWidth().padding(10.dp)) {
					Spacer(Modifier.weight(1f)) // Modifier.align doesn't work for some reason

					Button(onClick = ::chooseBaseDirectory) {
						Text("Choose base directory")
					}
				}
			}
		}
	}
}

fun chooseBaseDirectory() {
	FileChooser("Choose a base directory", baseDirectory, true, false) { files ->
		files?.singleOrNull()?.let { baseDirectory = it }
	}

}

fun selectFiles() {

}

fun deleteSelectedFiles() {

}

fun deleteAllFiles() {

}
