plugins {
	application
}

description = "Demo rendering controllable 2D rectangles"

dependencies {
	implementation(projects.pancakeDemo)
}

sourceSets {
	main {
		output.setResourcesDir(java.classesDirectory)
	}
}

application {
	mainModule.set("dev.kkorolyov.pancake.demo.wasdbox")
	mainClass.set("dev.kkorolyov.pancake.demo.wasdbox.LauncherKt")
}
tasks.named<JavaExec>("run") {
	// Launch alongside loose resources
	workingDir = File("src/dist")
}
