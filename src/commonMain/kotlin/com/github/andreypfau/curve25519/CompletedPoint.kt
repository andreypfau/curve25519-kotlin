package com.github.andreypfau.curve25519

import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.field.FieldElement

/**
 * A CompletedPoint is a point \(((X:Z), (Y:T))\) on the
 * \(\mathbb P^1 \times \mathbb P^1 \) model of the curve.
 * A point (x,y) in the affine model corresponds to \( ((x:1),(y:1)) \).
 */
data class CompletedPoint(
    val x: FieldElement,
    val y: FieldElement,
    val z: FieldElement,
    val t: FieldElement
) {
    fun toProjective(): ProjectivePoint = ProjectivePoint(
        x = x * t,
        y = y * z,
        z = z * t
    )

    /**
     * Convert this point from the \( \mathbb P^1 \times \mathbb P^1 \) model to the \( \mathbb P^3 \) model.
     *
     * This costs \(4 \mathrm M \).
     */
    fun toExtended(): EdwardsPoint = EdwardsPoint(
        x = x * t,
        y = y * z,
        z = z * t,
        t = x * y
    )

    override fun toString(): String = buildString {
        append("CompletedPoint{").appendLine()
        append("  X: ").append(x).appendLine()
        append("  Y: ").append(y).appendLine()
        append("  Z: ").append(z).appendLine()
        append("  T: ").append(t).appendLine()
        append("}")
    }
}
