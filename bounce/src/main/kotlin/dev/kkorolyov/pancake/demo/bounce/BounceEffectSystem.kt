package dev.kkorolyov.pancake.demo.bounce

import dev.kkorolyov.pancake.audio.al.AudioBuffer
import dev.kkorolyov.pancake.audio.al.AudioSource
import dev.kkorolyov.pancake.audio.al.AudioStreamer
import dev.kkorolyov.pancake.audio.al.component.AudioEmitter
import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.event.Intersected
import dev.kkorolyov.pancake.demo.toVector
import dev.kkorolyov.pancake.graphics.gl.component.Model
import dev.kkorolyov.pancake.graphics.gl.mesh.oval
import dev.kkorolyov.pancake.graphics.gl.shader.Program
import dev.kkorolyov.pancake.platform.GameSystem
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.entity.Entity
import dev.kkorolyov.pancake.platform.math.Vectors
import javafx.scene.paint.Color

class BounceEffectSystem(private val program: Program) : GameSystem(Transform::class.java, Intersected::class.java) {
	private val source: AudioSource by lazy {
		AudioSource().apply {
			refDistance = 20F

			AudioBuffer().apply {
				fill(Resources.inStream("bounce.wav"))
			}.attach(this)
		}
	}

	override fun update(entity: Entity, dt: Long) {
		source.play()

		create().apply {
			put(
				Transform(Vectors.create(entity[Transform::class.java].globalPosition)),
				Model(
					program,
					oval(Vectors.create(0.5, 0.5), Color.DARKSEAGREEN.toVector())
				),
				AudioEmitter(source)
			)
		}
	}
}
