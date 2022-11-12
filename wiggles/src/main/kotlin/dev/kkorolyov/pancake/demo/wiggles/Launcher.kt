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
import dev.kkorolyov.pancake.core.component.tag.Collidable
import dev.kkorolyov.pancake.core.component.tag.Correctable
import dev.kkorolyov.pancake.core.system.AccelerationSystem
import dev.kkorolyov.pancake.core.system.ActionSystem
import dev.kkorolyov.pancake.core.system.CappingSystem
import dev.kkorolyov.pancake.core.system.ChainSystem
import dev.kkorolyov.pancake.core.system.CollisionSystem
import dev.kkorolyov.pancake.core.system.CorrectionSystem
import dev.kkorolyov.pancake.core.system.DampingSystem
import dev.kkorolyov.pancake.core.system.IntersectionSystem
import dev.kkorolyov.pancake.core.system.MovementSystem
import dev.kkorolyov.pancake.core.system.cleanup.PhysicsCleanupSystem
import dev.kkorolyov.pancake.demo.bindToWindow
import dev.kkorolyov.pancake.demo.drawEnd
import dev.kkorolyov.pancake.demo.drawStart
import dev.kkorolyov.pancake.demo.editor
import dev.kkorolyov.pancake.demo.inputSystem
import dev.kkorolyov.pancake.demo.setInputMode
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
import dev.kkorolyov.pancake.graphics.system.CameraSystem
import dev.kkorolyov.pancake.input.Compensated
import dev.kkorolyov.pancake.input.Reaction
import dev.kkorolyov.pancake.input.component.Input
import dev.kkorolyov.pancake.input.glfw.input.CursorPosEvent
import dev.kkorolyov.pancake.input.glfw.toggle
import dev.kkorolyov.pancake.input.glfw.whenKey
import dev.kkorolyov.pancake.platform.Config
import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.Pipeline
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.math.Vector2
import dev.kkorolyov.pancake.platform.math.Vector3
import org.lwjgl.glfw.GLFW.*
import java.awt.Color

private val cameraQueue = CameraQueue()
private val gameEngine = GameEngine().apply {
	setPipelines(
		Pipeline(
			inputSystem(),
			ActionSystem()
		),
		Pipeline(
			AccelerationSystem(),
			CappingSystem(),
			MovementSystem(),
			ChainSystem(),
			DampingSystem(),
			IntersectionSystem(),
			CorrectionSystem(),
			CollisionSystem(),
			PhysicsCleanupSystem()
		).withFrequency(100),
		Pipeline(
			CameraSystem(cameraQueue),
			drawStart(),
			DrawSystem(cameraQueue),
			editor(this),
			drawEnd()
		)
	)
}

private val blankTexture = GLTexture { PixelBuffer.blank2() }

private val program = GLProgram(
	GLShader(GLShader.Type.VERTEX, Resources.inStream("shaders/literal.vert")),
	GLShader(GLShader.Type.FRAGMENT, Resources.inStream("shaders/user.frag"))
)

private val camera = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of(0.0)),
		Lens(
			Vector2.of(32.0, 32.0),
			Vector2.of(Config.get().getProperty("width").toDouble(), Config.get().getProperty("height").toDouble())
		).apply {
			bindToWindow()
		}
	)
}

private val cursor = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of(0.0)),
//		Velocity(Vectors.create(0.0, 0.0, 0.0)),
//		Mass(1.0),
		Bounds.round(0.5),
		Collidable(),
		Correctable(),
		Model(
			program,
			GLMesh(
				GLVertexBuffer {
					val color = Color.CYAN.toVector()

					ellipse(Vector2.of(1.0, 1.0)) { position, _ ->
						add(position, color)
					}
				},
				mode = GLMesh.Mode.TRIANGLE_FAN,
				textures = listOf(blankTexture)
			)
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
				Reaction.matchType(whenKey(GLFW_KEY_F1 to toggle(Compensated(Action { toggleEditor() }, Action.NOOP))))
			)
		),
		ActionQueue()
	)
}

private val obstPoly = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of(-5.0, 0.0, 0.0)),
		Bounds(Graph<Vector3, Void>().apply {
			put(Vector3.of(0.0, 2.0, 0.0), Vector3.of(-2.0, -2.0, 0.0), Vector3.of(2.0, -2.0, 0.0))
			put(Vector3.of(-2.0, -2.0, 0.0), Vector3.of(2.0, -2.0, 0.0))
		}),
//		Bounds.box(Vectors.create(4.0, 4.0, 0.0)),
		Model(
			program,
			GLMesh(
				GLVertexBuffer {
					val color = Color.BLUE.toVector()

					add(Vector2.of(0.0, 2.0), color)
					add(Vector2.of(-2.0, -2.0), color)
					add(Vector2.of(2.0, -2.0), color)
				},
				mode = GLMesh.Mode.TRIANGLES,
				textures = listOf(blankTexture)
			)
//			rectangle(Vectors.create(4.0, 4.0), Color.DARKOLIVEGREEN.toVector())
		)
	)
}

private val obstRound = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of(5.0, 0.0, 0.0)),
		Bounds.round(2.0),
		Model(
			program,
			GLMesh(
				GLVertexBuffer {
					val color = Color.GREEN.toVector()

					ellipse(Vector2.of(4.0, 4.0)) { position, _ ->
						add(position, color)
					}
				},
				mode = GLMesh.Mode.TRIANGLE_FAN,
				textures = listOf(blankTexture)
			)
		)
	)
}

private val strands = (0..0).map {
	makeStrand(cursor[Transform::class.java].position, 20)
}

private fun makeStrand(root: Vector3, length: Int) {
	gameEngine.entities.create().apply {
		put(
			Transform(Vector3.of(0.0, root.y - 1, 0.0)),
			Velocity(Vector3.of(0.0)),
			VelocityCap(Vector3.of(10.0, 10.0, 0.0)),
			Damping(Vector3.of(0.9, 0.9, 0.9)),
			Mass(1.0),
			Force(Vector3.of(0.0, -9.81, 0.0)),
			Chain(root, 1.1),
			Bounds.round(0.5),
			Collidable(),
			Correctable(),
			Model(
				program,
				GLMesh(
					GLVertexBuffer {
						val color = Color.CYAN.toVector()

						ellipse(Vector2.of(1.0, 1.0)) { position, texCoord ->
							add(position, color)
						}
					},
					mode = GLMesh.Mode.TRIANGLE_FAN,
					textures = listOf(blankTexture)
				)
			)
		)

		if (length > 0) {
			makeStrand(this[Transform::class.java].position, length - 1)
		}
	}
}

fun main() {
	setInputMode(GLFW_CURSOR, GLFW_CURSOR_HIDDEN)
	start(gameEngine)
}
