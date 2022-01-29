plugins {
	application
}

description = "Demo of mouse-controlled physics"

dependencies {
	implementation(projects.pancakeDemo)
}

application {
	mainModule.set("dev.kkorolyov.pancake.demo.wiggles")
	mainClass.set("dev.kkorolyov.pancake.demo.wiggles.LauncherKt")
}
