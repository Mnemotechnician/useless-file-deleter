import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	kotlin("multiplatform")
	id("org.jetbrains.compose")
}

group = "com.github.mnemotechnician"
version = "1.0-SNAPSHOT"

repositories {
	google()
	mavenCentral()
	maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
	jvm {
		jvmToolchain(11)
		withJava()
	}
	sourceSets {
		val jvmMain by getting {
			dependencies {
				implementation(compose.desktop.currentOs)
			}
		}
		val jvmTest by getting
	}
}

compose.desktop {
	application {
		mainClass = "com.github.mnemotechnician.fdel.MainKt"
		nativeDistributions {
			targetFormats(TargetFormat.Exe, TargetFormat.AppImage)
			packageName = "file-deleter"
			packageVersion = "1.0.0"
		}
	}
}
