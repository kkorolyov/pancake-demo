package dev.kkorolyov.pancake.demo

import dev.kkorolyov.pancake.editor.openEditor
import dev.kkorolyov.pancake.editor.registerEditor
import dev.kkorolyov.pancake.platform.Config
import dev.kkorolyov.pancake.platform.GameEngine
import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.lwjgl.glfw.GLFW
import tornadofx.App
import tornadofx.View
import tornadofx.runLater

val window = makeWindow()

/**
 * Starts a demo window running [engine].
 */
fun start(engine: GameEngine) {
	GLFW.glfwSetWindowCloseCallback(window) {
		engine.stop()
		Platform.exit()
	}.use { }

	Platform.startup {
		Editor(engine)
	}

	engine.start()
}

fun onResize(action: (width: Int, height: Int) -> Unit) {
	GLFW.glfwSetFramebufferSizeCallback(window) { _, w, h -> action(w, h) }.use { }
}

fun loadContext() {
	GLFW.glfwMakeContextCurrent(window)
	GLFW.glfwSwapInterval(1)
}

fun swap() {
	GLFW.glfwSwapBuffers(window)
}

fun editor() {
	runLater { openEditor() }
}

private fun makeWindow(): Long {
	if (!GLFW.glfwInit()) throw IllegalStateException("Cannot init GLFW")
	val window = GLFW.glfwCreateWindow(Config.get().getProperty("width").toInt(), Config.get().getProperty("height").toInt(), Config.get().getProperty("title"), 0, 0)
	if (window == 0L) throw IllegalStateException("Cannot create window")

	GLFW.glfwShowWindow(window)

	return window
}

class EmptyView : View() {
	override val root = StackPane()
}

class Editor(engine: GameEngine) : App(EmptyView::class) {
	init {
		registerEditor(engine)
		val stage = Stage()
		start(stage)

		Platform.setImplicitExit(false)
		stage.hide()
	}
}
