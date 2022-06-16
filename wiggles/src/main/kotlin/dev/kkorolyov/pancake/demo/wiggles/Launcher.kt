package dev.kkorolyov.pancake.demo.wiggles

import dev.kkorolyov.flub.data.Graph
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
import dev.kkorolyov.pancake.core.system.cleanup.PhysicsCleanupSystem
import dev.kkorolyov.pancake.demo.editor
import dev.kkorolyov.pancake.demo.loadContext
import dev.kkorolyov.pancake.demo.onResize
import dev.kkorolyov.pancake.demo.start
import dev.kkorolyov.pancake.demo.swap
import dev.kkorolyov.pancake.demo.toVector
import dev.kkorolyov.pancake.demo.window
import dev.kkorolyov.pancake.graphics.common.CameraQueue
import dev.kkorolyov.pancake.graphics.common.component.Lens
import dev.kkorolyov.pancake.graphics.common.system.CameraSystem
import dev.kkorolyov.pancake.graphics.gl.component.Model
import dev.kkorolyov.pancake.graphics.gl.mesh.ColorPoint
import dev.kkorolyov.pancake.graphics.gl.mesh.Mesh
import dev.kkorolyov.pancake.graphics.gl.mesh.oval
import dev.kkorolyov.pancake.graphics.gl.shader.Program
import dev.kkorolyov.pancake.graphics.gl.shader.Shader
import dev.kkorolyov.pancake.graphics.gl.system.DrawSystem
import dev.kkorolyov.pancake.input.common.Reaction
import dev.kkorolyov.pancake.input.common.component.Input
import dev.kkorolyov.pancake.input.glfw.input.CursorPosEvent
import dev.kkorolyov.pancake.input.glfw.system.InputSystem
import dev.kkorolyov.pancake.input.glfw.whenKey
import dev.kkorolyov.pancake.platform.Config
import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.Pipeline
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.math.Vector3
import dev.kkorolyov.pancake.platform.math.Vectors
import javafx.scene.paint.Color
import org.lwjgl.glfw.GLFW.*

val cameraQueue = CameraQueue()
val gameEngine = GameEngine(
	Pipeline(
		InputSystem(window),
		ActionSystem()
	),
	Pipeline(
		AccelerationSystem(),
		CappingSystem(),
		MovementSystem(),
		ChainSystem(),
		DampingSystem(),
		IntersectionSystem(),
		CollisionSystem(),
		PhysicsCleanupSystem()
	).withFrequency(100),
	Pipeline(
		CameraSystem(cameraQueue),
		DrawSystem(cameraQueue, ::loadContext, ::swap)
	)
)

private val program = Program(Shader(Shader.Type.VERTEX, Resources.inStream("shaders/literal.vert")), Shader(Shader.Type.FRAGMENT, Resources.inStream("shaders/user.frag")))

val camera = gameEngine.entities.create().apply {
	put(
		Transform(Vectors.create(0.0, 0.0, 0.0)),
		Lens(
			Vectors.create(32.0, 32.0),
			Vectors.create(Config.get().getProperty("width").toDouble(), Config.get().getProperty("height").toDouble())
		).apply {
			onResize { width, height ->
				size.x = width.toDouble()
				size.y = height.toDouble()
			}
		}
	)
}

val cursor = gameEngine.entities.create().apply {
	put(
		Transform(Vectors.create(0.0, 0.0, 0.0)),
//		Velocity(Vectors.create(0.0, 0.0, 0.0)),
//		Mass(1.0),
		Bounds.round(0.5).apply { isCorrectable = true },
		Model(
			program,
			oval(Vectors.create(1.0, 1.0), Color.DARKMAGENTA.toVector())
		),
		Input(
			Reaction.first(
				Reaction.matchType(
					Reaction { (_, x, y): CursorPosEvent ->
						Action {
							it[Transform::class.java].position.let { pos ->
								val lens = camera[Lens::class.java]
								val scale = lens.scale
								val size = lens.size
								pos.x = (x - size.x / 2) / scale.x
								pos.y = -(y - size.y / 2) / scale.y
							}
						}
					}),
				Reaction.matchType(whenKey(GLFW_KEY_F1 to Reaction { Action { editor() } }))
			)
		),
		ActionQueue()
	)
}

val obstPoly = gameEngine.entities.create().apply {
	put(
		Transform(Vectors.create(-5.0, 0.0, 0.0)),
		Bounds(Graph<Vector3?, Void?>().apply {
			put(Vectors.create(0.0, 2.0, 0.0), Vectors.create(-2.0, -2.0, 0.0), Vectors.create(2.0, -2.0, 0.0))
			put(Vectors.create(-2.0, -2.0, 0.0), Vectors.create(2.0, -2.0, 0.0))
		}),
//		Bounds.box(Vectors.create(4.0, 4.0, 0.0)),
		Model(
			program,
			Mesh.vertex(
				ColorPoint().apply {
					add(Vectors.create(0.0, 2.0, 0.0), Color.DARKMAGENTA.toVector())
					add(Vectors.create(-2.0, -2.0, 0.0), Color.DARKMAGENTA.toVector())
					add(Vectors.create(2.0, -2.0, 0.0), Color.DARKMAGENTA.toVector())
				},
				Mesh.DrawMode.TRIANGLES
			)
//			rectangle(Vectors.create(4.0, 4.0), Color.DARKOLIVEGREEN.toVector())
		)
	)
}

val obstRound = gameEngine.entities.create().apply {
	put(
		Transform(Vectors.create(5.0, 0.0, 0.0)),
		Bounds.round(2.0),
		Model(
			program,
			oval(Vectors.create(4.0, 4.0), Color.FORESTGREEN.toVector())
		)
	)
}

val strands = (0..0).map {
	makeStrand(cursor[Transform::class.java].position, 20)
}

fun makeStrand(root: Vector3, length: Int) {
	gameEngine.entities.create().apply {
		put(
			Transform(Vectors.create(0.0, root.y - 1, 0.0)),
			Velocity(Vectors.create(0.0, 0.0, 0.0)),
			VelocityCap(Vectors.create(10.0, 10.0, 0.0)),
			Damping(Vectors.create(0.9, 0.9, 0.9)),
			Mass(1.0),
			Force(Vectors.create(0.0, -9.81, 0.0)),
			Chain(root, 1.1),
			Bounds.round(0.5).apply { isCorrectable = true },
			Model(
				program,
				oval(Vectors.create(1.0, 1.0), Color.DEEPPINK.toVector())
			)
		)

		if (length > 0) {
			makeStrand(this[Transform::class.java].position, length - 1)
		}
	}
}

fun main() {
	glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN)
	start(gameEngine)
}
