plugins {
	application
}

description = "Demo rendering controllable 2D rectangles"

dependencies {
	implementation(projects.pancakeDemo) {
		exclude("org.openjfx")
	}
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
