package dev.kkorolyov.pancake.demo.wiggles

import dev.kkorolyov.pancake.audio.jfx.system.AudioSystem
import dev.kkorolyov.pancake.core.component.ActionQueue
import dev.kkorolyov.pancake.core.component.Bounds
import dev.kkorolyov.pancake.core.component.Chain
import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.movement.Damping
import dev.kkorolyov.pancake.core.component.movement.Force
import dev.kkorolyov.pancake.core.component.movement.Mass
import dev.kkorolyov.pancake.core.component.movement.Velocity
import dev.kkorolyov.pancake.core.component.movement.VelocityCap
import dev.kkorolyov.pancake.core.system.AccelerationSystem
import dev.kkorolyov.pancake.core.system.ActionSystem
import dev.kkorolyov.pancake.core.system.CappingSystem
import dev.kkorolyov.pancake.core.system.ChainSystem
import dev.kkorolyov.pancake.core.system.CollisionSystem
import dev.kkorolyov.pancake.core.system.DampingSystem
import dev.kkorolyov.pancake.core.system.IntersectionSystem
import dev.kkorolyov.pancake.core.system.MovementSystem
import dev.kkorolyov.pancake.demo.start
import dev.kkorolyov.pancake.graphics.jfx.component.Graphic
import dev.kkorolyov.pancake.graphics.jfx.component.Lens
import dev.kkorolyov.pancake.graphics.jfx.drawable.Oval
import dev.kkorolyov.pancake.graphics.jfx.drawable.Rectangle
import dev.kkorolyov.pancake.graphics.jfx.system.CameraSystem
import dev.kkorolyov.pancake.graphics.jfx.system.DrawSystem
import dev.kkorolyov.pancake.input.jfx.Reaction
import dev.kkorolyov.pancake.input.jfx.component.Input
import dev.kkorolyov.pancake.input.jfx.system.InputSystem
import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.GameLoop
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.entity.EntityPool
import dev.kkorolyov.pancake.platform.event.EventLoop
import dev.kkorolyov.pancake.platform.math.Vector3
import dev.kkorolyov.pancake.platform.math.Vectors
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.layout.TilePane
import javafx.scene.paint.Color
import tornadofx.runLater

val pane = TilePane().apply {
	cursor = Cursor.NONE
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
		ChainSystem(),
		DampingSystem(),
		IntersectionSystem(),
		CollisionSystem(),
		AudioSystem(),
		CameraSystem(),
		DrawSystem()
	)
)
val gameLoop = GameLoop(gameEngine)

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

val cursor = entities.create().apply {
	put(
		Transform(Vectors.create(0.0, 0.0, 0.0)),
//		Velocity(Vectors.create(0.0, 0.0, 0.0)),
//		Mass(1.0),
		Bounds.round(0.5),
		Graphic(Oval(Vectors.create(1.0, 1.0), Color.DARKMAGENTA)),
		Input(
			Reaction.matchType(
				Reaction { event: MouseEvent ->
					when (event.eventType) {
						MouseEvent.MOUSE_MOVED -> Action {
							it[Transform::class.java].position.let { pos ->
								val scale = camera[Lens::class.java].scale
								pos.x = (event.x - pane.width / 2) / scale.x
								pos.y = -(event.y - pane.height / 2) / scale.y
							}
						}
						else -> null
					}
				}
			)
		),
		ActionQueue()
	)
}

val obstPoly = entities.create().apply {
	put(
		Transform(Vectors.create(-5.0, 0.0, 0.0)),
		Bounds.box(Vectors.create(4.0, 4.0, 0.0)),
		Graphic(Rectangle(Vectors.create(4.0, 4.0), Color.DARKOLIVEGREEN))
	)
}

val obstRound = entities.create().apply {
	put(
		Transform(Vectors.create(5.0, 0.0, 0.0)),
		Bounds.round(2.0),
		Graphic(Oval(Vectors.create(4.0, 4.0), Color.FORESTGREEN))
	)
}

val strands = (0..0).map {
	makeStrand(cursor[Transform::class.java].position, 20)
}

fun makeStrand(root: Vector3, length: Int) {
	entities.create().apply {
		put(
			Transform(Vectors.create(0.0, root.y - 1, 0.0)),
			Velocity(Vectors.create(0.0, 0.0, 0.0)),
			VelocityCap(Vectors.create(10.0, 10.0, 0.0)),
			Damping(Vectors.create(0.9, 0.9, 0.9)),
			Mass(1.0),
			Force(Vectors.create(0.0, -9.81, 0.0)),
			Chain(root, 1.1),
			Bounds.round(0.5),
			Graphic(Oval(Vectors.create(1.0, 1.0), Color.DEEPPINK))
		)

		if (length > 0) {
			makeStrand(this[Transform::class.java].position, length - 1)
		}
	}
}

fun main() {
	start(gameLoop, pane)
	runLater { pane.requestFocus() }
}
