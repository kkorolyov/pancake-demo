package dev.kkorolyov.pancake.demo

import dev.kkorolyov.pancake.editor.openEditor
import dev.kkorolyov.pancake.editor.registerEditor
import dev.kkorolyov.pancake.platform.Config
import dev.kkorolyov.pancake.platform.GameLoop
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import tornadofx.App
import tornadofx.View

private var pane: Parent? = null

/**
 * Starts a demo window running [gameLoop] and displaying [root].
 */
fun start(gameLoop: GameLoop, root: Parent) {
	pane = root
	Platform.startup {
		Demo(gameLoop)
	}
	gameLoop.start()
}

class Demo(private val gameLoop: GameLoop) : App(DemoView::class) {
	init {
		registerEditor(gameLoop)

		start(Stage())
	}

	override fun start(stage: Stage) {
		stage.icons += Image(Config.get().getProperty("icon"))

		stage.width = Config.get().getProperty("width").toDouble()
		stage.height = Config.get().getProperty("height").toDouble()

		stage.onCloseRequest = EventHandler { gameLoop.stop() }

		super.start(stage)
	}
}

class DemoView : View(Config.get().getProperty("title")) {
	override val root = pane!!

	override fun onDock() {
		currentStage?.let { curStage ->
			curStage.scene?.onKeyPressed = EventHandler { e ->
				when (e.code) {
					KeyCode.F1 -> openEditor(curStage)
					else -> {}
				}
			}
		}
	}
}
