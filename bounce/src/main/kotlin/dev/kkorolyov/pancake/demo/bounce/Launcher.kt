package dev.kkorolyov.pancake.demo.bounce

import dev.kkorolyov.pancake.core.component.ActionQueue
import dev.kkorolyov.pancake.core.component.Bounds
import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.movement.Velocity
import dev.kkorolyov.pancake.core.event.EntitiesIntersected
import dev.kkorolyov.pancake.core.system.AccelerationSystem
import dev.kkorolyov.pancake.core.system.ActionSystem
import dev.kkorolyov.pancake.core.system.CappingSystem
import dev.kkorolyov.pancake.core.system.CollisionSystem
import dev.kkorolyov.pancake.core.system.IntersectionSystem
import dev.kkorolyov.pancake.core.system.MovementSystem
import dev.kkorolyov.pancake.demo.start
import dev.kkorolyov.pancake.graphics.jfx.component.Graphic
import dev.kkorolyov.pancake.graphics.jfx.component.Lens
import dev.kkorolyov.pancake.graphics.jfx.drawable.Oval
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
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.entity.EntityPool
import dev.kkorolyov.pancake.platform.event.EventLoop
import dev.kkorolyov.pancake.platform.math.Vector2
import dev.kkorolyov.pancake.platform.math.Vectors
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.layout.TilePane
import javafx.scene.media.AudioClip
import javafx.scene.paint.Color
import tornadofx.runLater

val pane = TilePane()

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
		IntersectionSystem(),
		CollisionSystem(),
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

val walls = makeWalls(
	Vectors.create(
		Config.get().getProperty("width").toDouble() / 2 / camera[Lens::class.java].scale.x,
		Config.get().getProperty("height").toDouble() / 2 / camera[Lens::class.java].scale.y
	)
)

val ball = entities.create().apply {
	put(
		Transform(Vectors.create(0.0, 0.0, 0.0)),
		Bounds.round(1.0),
		Velocity(Vectors.create(30.0, 15.0, 0.0)),
		Graphic(Oval(Vectors.create(2.0, 2.0), Color.TEAL))
	)
}

fun makeWalls(radii: Vector2): List<EntityPool.ManagedEntity> {
	val moveLeft = Action { it[Transform::class.java].position.x -= 2 }
	val moveRight = Action { it[Transform::class.java].position.x += 2 }
	val moveDown = Action { it[Transform::class.java].position.y -= 2 }
	val moveUp = Action { it[Transform::class.java].position.y += 2 }

	val bounds = Bounds.box(Vectors.create(radii.x * 2, radii.y * 2, 0.0))
	val graphic = Graphic(Rectangle(Vectors.create(radii.x * 2, radii.y * 2), Color.MAROON))

	return listOf(
		Vectors.create(-radii.x * 2, 0.0, 0.0),
		Vectors.create(radii.x * 2, 0.0, 0.0),
		Vectors.create(0.0, -radii.y * 2, 0.0),
		Vectors.create(0.0, radii.y * 2, 0.0)
	).map {
		entities.create().apply {
			put(
				Transform(it),
				ActionQueue(),
				Input(
					Reaction.matchType(
						Reaction.whenCode(
							KeyCode.W to Reaction.keyToggle(Compensated(moveUp, Action.NOOP)),
							KeyCode.A to Reaction.keyToggle(Compensated(moveLeft, Action.NOOP)),
							KeyCode.S to Reaction.keyToggle(Compensated(moveDown, Action.NOOP)),
							KeyCode.D to Reaction.keyToggle(Compensated(moveRight, Action.NOOP))
						)
					)
				),
				bounds,
				graphic
			)
		}
	}
}

fun main() {
	start(gameLoop, pane)
	runLater { pane.requestFocus() }

	val spawnClip = AudioClip(ClassLoader.getSystemResource("spawn.wav").toURI().toString())
	events.register(EntitiesIntersected::class.java) {
		spawnClip.play()
	}
}
