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
import dev.kkorolyov.pancake.demo.editor
import dev.kkorolyov.pancake.demo.loadContext
import dev.kkorolyov.pancake.demo.start
import dev.kkorolyov.pancake.demo.swap
import dev.kkorolyov.pancake.demo.toVector
import dev.kkorolyov.pancake.demo.window
import dev.kkorolyov.pancake.graphics.common.CameraQueue
import dev.kkorolyov.pancake.graphics.common.component.Lens
import dev.kkorolyov.pancake.graphics.common.system.CameraSystem
import dev.kkorolyov.pancake.graphics.gl.component.Model
import dev.kkorolyov.pancake.graphics.gl.mesh.rectangle
import dev.kkorolyov.pancake.graphics.gl.shader.Program
import dev.kkorolyov.pancake.graphics.gl.shader.Shader
import dev.kkorolyov.pancake.graphics.gl.system.DrawSystem
import dev.kkorolyov.pancake.input.common.Compensated
import dev.kkorolyov.pancake.input.common.Reaction
import dev.kkorolyov.pancake.input.common.component.Input
import dev.kkorolyov.pancake.input.glfw.system.InputSystem
import dev.kkorolyov.pancake.input.glfw.toggle
import dev.kkorolyov.pancake.input.glfw.whenKey
import dev.kkorolyov.pancake.platform.Config
import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.Pipeline
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.math.Vectors
import dev.kkorolyov.pancake.platform.plugin.DeferredConverterFactory
import dev.kkorolyov.pancake.platform.plugin.Plugins
import dev.kkorolyov.pancake.platform.registry.Registry
import dev.kkorolyov.pancake.platform.registry.ResourceReader
import javafx.scene.paint.Color
import org.lwjgl.glfw.GLFW

private val cameraQueue = CameraQueue()

private val program = Program(
	Shader(Shader.Type.VERTEX, Resources.inStream("shaders/literal.vert")),
	Shader(Shader.Type.FRAGMENT, Resources.inStream("shaders/user.frag"))
)

private val music: AudioSource by lazy {
	AudioSource().apply {
		gain = 0.2F
		AudioStreamer({ Resources.inStream("assets/audio/bg.wav") })
			// TODO VOLUME down
			.attach(this)
	}
}

val actions by lazy {
	Resources.inStream("actions.yaml").use {
		Registry<String, Action>().apply {
			load(ResourceReader(Plugins.deferredConverter(DeferredConverterFactory.ActionStrat::class.java)).fromYaml(it))
		}
	}
}

val gameEngine = GameEngine(
	Pipeline(
		InputSystem(window),
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
		DrawSystem(cameraQueue, ::loadContext, ::swap)
	)
)

val camera = gameEngine.entities.create().apply {
	put(
		Transform(Vectors.create(0.0, 0.0, 0.0)),
		Lens(
			Vectors.create(32.0, 32.0),
			Vectors.create(Config.get().getProperty("width").toDouble(), Config.get().getProperty("height").toDouble())
		),
		AudioReceiver(),
		Input(
			Reaction.matchType(
				whenKey(GLFW.GLFW_KEY_F1 to Reaction { Action { editor() } })
			)
		),
		ActionQueue()
	)
}

val player = gameEngine.entities.create().apply {
	put(
		Mass(0.01),
		Force(Vectors.create(0.0, 0.0, 0.0)),
		Velocity(Vectors.create(0.0, 0.0, 0.0)),
		VelocityCap(Vectors.create(20.0, 20.0, 20.0)),
		Damping(Vectors.create(0.0, 0.0, 0.0)),
		Transform(Vectors.create(0.0, 0.0, 0.0)),
		AudioEmitter(music),
		Model(
			program,
			rectangle(Vectors.create(1.0, 1.0), Color.AQUA.toVector())
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
