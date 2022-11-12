package dev.kkorolyov.pancake.demo.bounce

import dev.kkorolyov.pancake.audio.al.AudioBuffer
import dev.kkorolyov.pancake.audio.al.AudioSource
import dev.kkorolyov.pancake.audio.al.component.AudioEmitter
import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.event.Intersected
import dev.kkorolyov.pancake.core.component.tag.Collidable
import dev.kkorolyov.pancake.demo.toVector
import dev.kkorolyov.pancake.graphics.component.Model
import dev.kkorolyov.pancake.graphics.ellipse
import dev.kkorolyov.pancake.graphics.gl.resource.GLMesh
import dev.kkorolyov.pancake.graphics.gl.resource.GLVertexBuffer
import dev.kkorolyov.pancake.graphics.resource.Program
import dev.kkorolyov.pancake.platform.GameSystem
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.entity.Entity
import dev.kkorolyov.pancake.platform.math.Vector2
import java.awt.Color

class BounceEffectSystem(private val program: Program) : GameSystem(Transform::class.java, Intersected::class.java, Collidable::class.java) {
	private val source: AudioSource by lazy {
		AudioSource().apply {
			refDistance = 20F

			AudioBuffer().apply {
				fill(Resources.inStream("bounce.wav"))
			}.attach(this)
		}
	}
	private val mesh = GLMesh(
		GLVertexBuffer {
			val color = Color.GREEN.toVector()

			ellipse(Vector2.of(0.5, 0.5)) { position, _ ->
				add(position, color)
			}
		},
		mode = GLMesh.Mode.TRIANGLE_FAN,
		textures = listOf(blankTexture)
	)

	private val events = mutableSetOf<Intersected>()

	override fun update(entity: Entity, dt: Long) {
		val event = entity[Intersected::class.java]
		if (events.add(event)) {
			create().apply {
				put(
					Transform(entity[Transform::class.java].globalPosition),
					Model(program, mesh),
					AudioEmitter(source)
				)
			}
			source.play()
		}
	}

	override fun after() {
		events.clear()
	}
}
