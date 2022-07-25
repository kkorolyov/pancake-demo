package dev.kkorolyov.pancake.demo

import dev.kkorolyov.pancake.platform.math.Vector3
import java.awt.Color

/**
 * Returns the `RGB` components of this color as a vector.
 */
fun Color.toVector(): Vector3 = Vector3.of(red.toDouble(), green.toDouble(), blue.toDouble())
