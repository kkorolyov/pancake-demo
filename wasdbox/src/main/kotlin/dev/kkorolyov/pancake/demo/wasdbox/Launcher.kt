package dev.kkorolyov.pancake.demo.wasdbox

import dev.kkorolyov.pancake.audio.jfx.AddListener
import dev.kkorolyov.pancake.audio.jfx.Listener
import dev.kkorolyov.pancake.audio.jfx.component.AudioEmitter
import dev.kkorolyov.pancake.audio.jfx.system.AudioSystem
import dev.kkorolyov.pancake.core.component.ActionQueue
import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.movement.Damping
import dev.kkorolyov.pancake.core.component.movement.Force
import dev.kkorolyov.pancake.core.component.movement.Mass
import dev.kkorolyov.pancake.core.component.movement.Velocity
import dev.kkorolyov.pancake.core.component.movement.VelocityCap
import dev.kkorolyov.pancake.core.system.AccelerationSystem
import dev.kkorolyov.pancake.core.system.ActionSystem
import dev.kkorolyov.pancake.core.system.CappingSystem
import dev.kkorolyov.pancake.core.system.DampingSystem
import dev.kkorolyov.pancake.core.system.MovementSystem
import dev.kkorolyov.pancake.demo.start
import dev.kkorolyov.pancake.graphics.jfx.component.Graphic
import dev.kkorolyov.pancake.graphics.jfx.component.Lens
import dev.kkorolyov.pancake.graphics.jfx.drawable.Rectangle
import dev.kkorolyov.pancake.graphics.jfx.system.CameraSystem
import dev.kkorolyov.pancake.graphics.jfx.system.DrawSystem
import dev.kkorolyov.pancake.input.jfx.Compensated
import dev.kkorolyov.pancake.input.jfx.Reaction
import dev.kkorolyov.pancake.input.jfx.component.Input
import dev.kkorolyov.pancake.input.jfx.system.InputSystem
import dev.kkorolyov.pancake.platform.Config
import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.GameLoop
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.entity.EntityPool
import dev.kkorolyov.pancake.platform.event.EventLoop
import dev.kkorolyov.pancake.platform.math.Vectors
import dev.kkorolyov.pancake.platform.plugin.DeferredConverterFactory
import dev.kkorolyov.pancake.platform.plugin.Plugins
import dev.kkorolyov.pancake.platform.registry.Registry
import dev.kkorolyov.pancake.platform.registry.ResourceReader
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.layout.TilePane
import javafx.scene.media.Media
import javafx.scene.paint.Color
import tornadofx.runLater
import java.nio.file.Path

val pane = TilePane()

val actions by lazy {
	Resources.inStream("actions.yaml").use {
		Registry<String, Action>().apply {
			load(ResourceReader(Plugins.deferredConverter(DeferredConverterFactory.ActionStrat::class.java)).fromYaml(it))
		}
	}
}

val events = EventLoop.Broadcasting()
val entities = EntityPool(events)
val gameEngine = GameEngine(
	events,
	entities,
	listOf(
		InputSystem(listOf(pane)),
		ActionSystem(),
		AccelerationSystem(),
		CappingSystem(),
		MovementSystem(),
		DampingSystem(),
		AudioSystem(),
		CameraSystem(),
		DrawSystem()
	)
)
val gameLoop = GameLoop(
	gameEngine
)

val camera = entities.create().apply {
	put(
		Transform(Vectors.create(0.0, 0.0, 0.0)),
		Lens(
			Canvas().also {
				pane.children += it
				it.widthProperty().bind(pane.widthProperty())
				it.heightProperty().bind(pane.heightProperty())
			},
			Vectors.create(32.0, 32.0)
		)
	)
}

val player = entities.create().apply {
	put(
		Mass(0.01),
		Force(Vectors.create(0.0, 0.0, 0.0)),
		Velocity(Vectors.create(0.0, 0.0, 0.0)),
		VelocityCap(Vectors.create(20.0, 20.0, 20.0)),
		Damping(Vectors.create(0.0, 0.0, 0.0)),
		Transform(Vectors.create(0.0, 0.0, 0.0)),
		AudioEmitter(),
		Graphic(Rectangle(Vectors.create(1.0, 1.0), Color.AQUA)),
		Input(
			Reaction.matchType(
				Reaction.whenCode(
					KeyCode.W to Reaction.keyToggle(Compensated(actions["forceUp"], actions["forceDown"])),
					KeyCode.S to Reaction.keyToggle(Compensated(actions["forceDown"], actions["forceUp"])),
					KeyCode.A to Reaction.keyToggle(Compensated(actions["forceLeft"], actions["forceRight"])),
					KeyCode.D to Reaction.keyToggle(Compensated(actions["forceRight"], actions["forceLeft"]))
				)
			)
		),
		ActionQueue()
	)
}

fun main() {
	start(gameLoop, pane)
	runLater { pane.requestFocus() }

	player[AudioEmitter::class.java].add(Media(ClassLoader.getSystemResource("assets/audio/bg.wav").toURI().toString()))
	events.enqueue(
		AddListener(
			Listener(
				position = player[Transform::class.java].position,
				volume = Config.get().getProperty("volume").toDouble()
			)
		)
	)
}
