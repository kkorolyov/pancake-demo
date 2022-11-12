package dev.kkorolyov.pancake.demo.bounce

import dev.kkorolyov.pancake.audio.al.component.AudioReceiver
import dev.kkorolyov.pancake.audio.al.system.AudioPlaySystem
import dev.kkorolyov.pancake.audio.al.system.AudioPositionSystem
import dev.kkorolyov.pancake.audio.al.system.AudioReceiverPositionSystem
import dev.kkorolyov.pancake.audio.al.system.AudioReceiverVelocitySystem
import dev.kkorolyov.pancake.audio.al.system.AudioVelocitySystem
import dev.kkorolyov.pancake.core.component.ActionQueue
import dev.kkorolyov.pancake.core.component.Bounds
import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.movement.Velocity
import dev.kkorolyov.pancake.core.component.tag.Collidable
import dev.kkorolyov.pancake.core.component.tag.Correctable
import dev.kkorolyov.pancake.core.system.AccelerationSystem
import dev.kkorolyov.pancake.core.system.ActionSystem
import dev.kkorolyov.pancake.core.system.CappingSystem
import dev.kkorolyov.pancake.core.system.CollisionSystem
import dev.kkorolyov.pancake.core.system.IntersectionSystem
import dev.kkorolyov.pancake.core.system.MovementSystem
import dev.kkorolyov.pancake.core.system.cleanup.PhysicsCleanupSystem
import dev.kkorolyov.pancake.demo.bindToWindow
import dev.kkorolyov.pancake.demo.drawEnd
import dev.kkorolyov.pancake.demo.drawStart
import dev.kkorolyov.pancake.demo.editor
import dev.kkorolyov.pancake.demo.inputSystem
import dev.kkorolyov.pancake.demo.start
import dev.kkorolyov.pancake.demo.toVector
import dev.kkorolyov.pancake.demo.toggleEditor
import dev.kkorolyov.pancake.graphics.CameraQueue
import dev.kkorolyov.pancake.graphics.PixelBuffer
import dev.kkorolyov.pancake.graphics.component.Lens
import dev.kkorolyov.pancake.graphics.component.Model
import dev.kkorolyov.pancake.graphics.ellipse
import dev.kkorolyov.pancake.graphics.gl.resource.GLMesh
import dev.kkorolyov.pancake.graphics.gl.resource.GLProgram
import dev.kkorolyov.pancake.graphics.gl.resource.GLShader
import dev.kkorolyov.pancake.graphics.gl.resource.GLTexture
import dev.kkorolyov.pancake.graphics.gl.resource.GLVertexBuffer
import dev.kkorolyov.pancake.graphics.gl.system.DrawSystem
import dev.kkorolyov.pancake.graphics.rectangle
import dev.kkorolyov.pancake.graphics.system.CameraSystem
import dev.kkorolyov.pancake.input.Compensated
import dev.kkorolyov.pancake.input.Reaction
import dev.kkorolyov.pancake.input.component.Input
import dev.kkorolyov.pancake.input.glfw.toggle
import dev.kkorolyov.pancake.input.glfw.whenKey
import dev.kkorolyov.pancake.platform.Config
import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.Pipeline
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.entity.Entity
import dev.kkorolyov.pancake.platform.math.Vector2
import dev.kkorolyov.pancake.platform.math.Vector3
import org.lwjgl.glfw.GLFW
import java.awt.Color

val blankTexture = GLTexture { PixelBuffer.blank2() }

private val cameraQueue = CameraQueue()

private val program = GLProgram(
	GLShader(GLShader.Type.VERTEX, Resources.inStream("shaders/literal.vert")),
	GLShader(GLShader.Type.FRAGMENT, Resources.inStream("shaders/user.frag"))
)

val gameEngine = GameEngine().apply {
	setPipelines(
		Pipeline(
			inputSystem(),
			ActionSystem()
		),
		Pipeline(
			AccelerationSystem(),
			CappingSystem(),
			MovementSystem(),
			IntersectionSystem(),
			CollisionSystem(),
			BounceEffectSystem(program),
			PhysicsCleanupSystem()
		).withFrequency(100),
		Pipeline(
			AudioReceiverPositionSystem(),
			AudioReceiverVelocitySystem(),
			AudioPositionSystem(),
			AudioVelocitySystem(),
			AudioPlaySystem()
		),
		Pipeline(
			CameraSystem(cameraQueue),
			drawStart(),
			DrawSystem(cameraQueue),
			editor(this),
			drawEnd()
		)
	)
}

val camera = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of(0.0)),
		Lens(
			Vector2.of(32.0, 32.0),
			Vector2.of(Config.get().getProperty("width").toDouble(), Config.get().getProperty("height").toDouble())
		).apply {
			bindToWindow()
		},
		Input(
			Reaction.matchType(
				whenKey(GLFW.GLFW_KEY_F1 to toggle(Compensated(Action { toggleEditor() }, Action.NOOP)))
			)
		),
		ActionQueue(),
		AudioReceiver()
	)
}

val walls = makeWalls(
	Vector2.of(
		Config.get().getProperty("width").toDouble() / 2 / camera[Lens::class.java].scale.x,
		Config.get().getProperty("height").toDouble() / 2 / camera[Lens::class.java].scale.y
	)
)
val ball = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of(0.0, 0.0, 0.0)),
		Bounds.round(1.0),
		Correctable(),
		Velocity(Vector3.of(30.0, 15.0, 0.0)),
		Collidable(),
		Model(
			program,
			GLMesh(
				GLVertexBuffer {
					val color = Color.BLUE.toVector()

					ellipse(Vector2.of(2.0, 2.0)) { position, _ ->
						add(position, color)
					}
				},
				mode = GLMesh.Mode.TRIANGLE_FAN,
				textures = listOf(blankTexture)
			)
		)
	)
}

fun makeWalls(radii: Vector2): List<Entity> {
	val step = 10.0

	val moveLeft = Action { it[Velocity::class.java].value.x = -step }
	val moveRight = Action { it[Velocity::class.java].value.x = step }
	val moveDown = Action { it[Velocity::class.java].value.y = -step }
	val moveUp = Action { it[Velocity::class.java].value.y = step }
	val stop = Action { it[Velocity::class.java].value.scale(0.0) }

	val bounds = Bounds.box(Vector3.of(radii.x * 2, radii.y * 2))
	val graphic = Model(
		program,
		GLMesh(
			GLVertexBuffer {
				val color = Color.RED.toVector()

				rectangle(Vector2.of(radii.x * 2, radii.y * 2)) { position, _ ->
					add(position, color)
				}
			},
			mode = GLMesh.Mode.TRIANGLE_FAN,
			textures = listOf(blankTexture)
		)
	)

	return listOf(
		Vector3.of(-radii.x * 2, 0.0),
		Vector3.of(radii.x * 2, 0.0),
		Vector3.of(0.0, -radii.y * 2),
		Vector3.of(0.0, radii.y * 2)
	).map {
		gameEngine.entities.create().apply {
			put(
				Transform(it),
				Velocity(Vector3.of()),
				ActionQueue(),
				Input(
					Reaction.matchType(
						whenKey(
							GLFW.GLFW_KEY_W to toggle(Compensated(moveUp, stop)),
							GLFW.GLFW_KEY_A to toggle(Compensated(moveLeft, stop)),
							GLFW.GLFW_KEY_S to toggle(Compensated(moveDown, stop)),
							GLFW.GLFW_KEY_D to toggle(Compensated(moveRight, stop))
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
	start(gameEngine)
}
