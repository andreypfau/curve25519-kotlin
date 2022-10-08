package com.github.andreypfau.curve25519

import com.github.andreypfau.curve25519.field.FieldElement

/**
 * A ProjectivePoint is a point \((X:Y:Z)\) on the \(\mathbb P^2\) model of the curve.
 * A point \((x,y)\) in the affine model corresponds to \((x:y:1)\).
 */
data class ProjectivePoint(
    val x: FieldElement,
    val y: FieldElement,
    val z: FieldElement
) {
    /**
     * Double this point:
     * @return `this + this`
     */
    fun double(): CompletedPoint {
        val xx = x.square()
        val yy = y.square()
        val zz2 = z.square2()
        val xPlusY = x + y
        val xPlusYsq = xPlusY.square()
        val yyPlusXX = yy + xx
        val yyMinusXX = yy - xx
        return CompletedPoint(
            x = xPlusYsq - yyPlusXX,
            y = yyPlusXX,
            z = yyMinusXX,
            t = zz2 - yyMinusXX
        )
    }
}
