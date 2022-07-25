package dev.kkorolyov.pancake.demo.wasdbox

import dev.kkorolyov.pancake.audio.al.AudioSource
import dev.kkorolyov.pancake.audio.al.AudioStreamer
import dev.kkorolyov.pancake.audio.al.component.AudioEmitter
import dev.kkorolyov.pancake.audio.al.component.AudioReceiver
import dev.kkorolyov.pancake.audio.al.system.AudioPlaySystem
import dev.kkorolyov.pancake.audio.al.system.AudioPositionSystem
import dev.kkorolyov.pancake.audio.al.system.AudioReceiverPositionSystem
import dev.kkorolyov.pancake.audio.al.system.AudioReceiverVelocitySystem
import dev.kkorolyov.pancake.audio.al.system.AudioVelocitySystem
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
import dev.kkorolyov.pancake.demo.drawEnd
import dev.kkorolyov.pancake.demo.drawStart
import dev.kkorolyov.pancake.demo.editor
import dev.kkorolyov.pancake.demo.inputSystem
import dev.kkorolyov.pancake.demo.start
import dev.kkorolyov.pancake.demo.toVector
import dev.kkorolyov.pancake.demo.toggleEditor
import dev.kkorolyov.pancake.graphics.CameraQueue
import dev.kkorolyov.pancake.graphics.component.Lens
import dev.kkorolyov.pancake.graphics.gl.component.Model
import dev.kkorolyov.pancake.graphics.gl.mesh.rectangle
import dev.kkorolyov.pancake.graphics.gl.shader.Program
import dev.kkorolyov.pancake.graphics.gl.shader.Shader
import dev.kkorolyov.pancake.graphics.gl.system.DrawSystem
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
import dev.kkorolyov.pancake.platform.math.Vector2
import dev.kkorolyov.pancake.platform.math.Vector3
import dev.kkorolyov.pancake.platform.registry.BasicParsers
import dev.kkorolyov.pancake.platform.registry.Registry
import dev.kkorolyov.pancake.platform.registry.ResourceConverters
import org.lwjgl.glfw.GLFW
import java.awt.Color

private val cameraQueue = CameraQueue()

private val program = Program(
	Shader(Shader.Type.VERTEX, Resources.inStream("shaders/literal.vert")),
	Shader(Shader.Type.FRAGMENT, Resources.inStream("shaders/user.frag"))
)

private val music: AudioSource by lazy {
	AudioSource().apply {
		gain = 0.2F
		AudioStreamer({ Resources.inStream("assets/audio/bg.wav") })
			.attach(this)
	}
}

val actions by lazy {
	Resources.inStream("actions.yaml").use {
		Registry<Action>().apply {
			putAll(BasicParsers.yaml().andThen(ResourceConverters.get(Action::class.java)).parse(it))
		}
	}
}

val gameEngine = GameEngine().apply {
	setPipelines(
		Pipeline(
			inputSystem(),
			ActionSystem(),
		),
		Pipeline(
			AccelerationSystem(),
			CappingSystem(),
			MovementSystem(),
			DampingSystem(),
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
		),
		AudioReceiver(),
		Input(
			Reaction.matchType(
				whenKey(GLFW.GLFW_KEY_F1 to toggle(Compensated(Action { toggleEditor() }, Action.NOOP)))
			)
		),
		ActionQueue()
	)
}

val player = gameEngine.entities.create().apply {
	put(
		Mass(0.01),
		Force(Vector3.of(0.0)),
		Velocity(Vector3.of(0.0)),
		VelocityCap(Vector3.of(20.0, 20.0, 20.0)),
		Damping(Vector3.of(0.0)),
		Transform(Vector3.of(0.0)),
		AudioEmitter(music),
		Model(
			program,
			rectangle(Vector2.of(1.0, 1.0), Color.BLUE.toVector())
		),
		Input(
			Reaction.matchType(
				whenKey(
					GLFW.GLFW_KEY_W to toggle(Compensated(actions["forceUp"], actions["forceDown"])),
					GLFW.GLFW_KEY_S to toggle(Compensated(actions["forceDown"], actions["forceUp"])),
					GLFW.GLFW_KEY_A to toggle(Compensated(actions["forceLeft"], actions["forceRight"])),
					GLFW.GLFW_KEY_D to toggle(Compensated(actions["forceRight"], actions["forceLeft"]))
				)
			)
		),
		ActionQueue()
	)
}

fun main() {
	start(gameEngine)
}
