package com.github.mnemotechnician.fdel

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

/** References to resources of this application. */
object R {
	object Icon {
		val folder @Composable get() = painterResource("icon/folder.svg")
		val file @Composable get() = painterResource("icon/file.svg")
		val eye @Composable get() = painterResource("icon/eye.svg")
		val noEye @Composable get() = painterResource("icon/no-eye.svg")
		val darkMode @Composable get() = painterResource("icon/dark-mode.svg")
		val lightMode @Composable get() = painterResource("icon/light-mode.svg")
	}
}
