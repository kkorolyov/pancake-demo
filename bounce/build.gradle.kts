plugins {
	application
}

description = "Demos collision physics"

dependencies {
	implementation(projects.pancakeDemo) {
		exclude("org.openjfx")
	}
}

application {
	mainModule.set("dev.kkorolyov.pancake.demo.bounce")
	mainClass.set("dev.kkorolyov.pancake.demo.bounce.LauncherKt")
}
