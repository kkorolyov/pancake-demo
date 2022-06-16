package dev.kkorolyov.pancake.demo

import dev.kkorolyov.pancake.platform.math.Vector3
import dev.kkorolyov.pancake.platform.math.Vectors
import javafx.scene.paint.Color

/**
 * Returns the `RGB` components of this color as a vector.
 */
fun Color.toVector(): Vector3 = Vectors.create(red, green, blue)
