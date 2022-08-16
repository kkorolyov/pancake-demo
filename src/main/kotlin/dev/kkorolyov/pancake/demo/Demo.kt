package dev.kkorolyov.pancake.demo

import dev.kkorolyov.pancake.editor.Container
import dev.kkorolyov.pancake.editor.widget.Editor
import dev.kkorolyov.pancake.editor.widget.Window
import dev.kkorolyov.pancake.graphics.component.Lens
import dev.kkorolyov.pancake.input.glfw.system.InputSystem
import dev.kkorolyov.pancake.platform.Config
import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.GameSystem
import dev.kkorolyov.pancake.platform.Pipeline
import dev.kkorolyov.pancake.platform.Resources
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL46.*

private const val editorSettings = "editor.ini"

private val window = run {
	if (!glfwInit()) throw IllegalStateException("Cannot init GLFW")
	glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
	val window = glfwCreateWindow(Config.get().getProperty("width").toInt(), Config.get().getProperty("height").toInt(), Config.get().getProperty("title"), 0, 0)
	if (window == 0L) throw IllegalStateException("Cannot create window")

	glfwMakeContextCurrent(window)
	glfwSwapInterval(1)

	glfwShowWindow(window)

	GL.createCapabilities()

	window
}
private val container = Container(window).apply {
	Resources.inStream(editorSettings)?.use(::load)
}

private lateinit var editor: Window

/**
 * Starts a demo window running [engine].
 */
fun start(engine: GameEngine) {
	glfwSetWindowCloseCallback(window) {
		engine.stop()
	}.use { }

	engine.start()

	Resources.outStream(editorSettings)?.use(container::close) ?: container.close()
}

fun inputSystem() = InputSystem(window)

fun drawStart() = Pipeline.run {
	glfwMakeContextCurrent(window)
	glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
}

fun drawEnd() = Pipeline.run {
	glfwSwapBuffers(window)
}

fun editor(engine: GameEngine): GameSystem {
	editor = Window("Editor", Editor(engine)).apply { visible = false }
	return Pipeline.run {
		container(editor)
	}
}

fun toggleEditor() {
	editor.visible = !editor.visible
}

fun Lens.bindToWindow() {
	glfwSetFramebufferSizeCallback(window) { _, w, h ->
		size.x = w.toDouble()
		size.y = h.toDouble()
	}.use { }
}

fun setInputMode(mode: Int, value: Int) = glfwSetInputMode(window, mode, value)
