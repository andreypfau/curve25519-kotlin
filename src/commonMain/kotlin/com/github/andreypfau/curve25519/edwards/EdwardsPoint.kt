package com.github.andreypfau.curve25519.edwards

import com.github.andreypfau.curve25519.constants.tables.EdwardsBasepointTable
import com.github.andreypfau.curve25519.field.FieldElement
import com.github.andreypfau.curve25519.models.AffineNielsPoint
import com.github.andreypfau.curve25519.models.CompletedPoint
import com.github.andreypfau.curve25519.models.ProjectivePoint
import com.github.andreypfau.curve25519.scalar.Scalar

data class EdwardsPoint(
    val x: FieldElement,
    val y: FieldElement,
    val z: FieldElement,
    val t: FieldElement
) {
    constructor() : this(FieldElement(), FieldElement(), FieldElement(), FieldElement())

    fun identity(): EdwardsPoint = apply {
        x.zero()
        y.one()
        z.one()
        t.zero()
    }

    fun set(pp: ProjectivePoint): EdwardsPoint = apply {
        x.mul(pp.x, pp.z)
        y.mul(pp.y, pp.z)
        z.square(pp.z)
        t.mul(pp.x, pp.y)
    }

    fun set(ap: AffineNielsPoint): EdwardsPoint = apply {
        identity()
        val sum = CompletedPoint()
        set(sum.add(this, ap))
    }

    fun set(cp: CompletedPoint): EdwardsPoint = apply {
        x.mul(cp.x, cp.t)
        y.mul(cp.y, cp.z)
        z.mul(cp.z, cp.t)
        t.mul(cp.x, cp.y)
    }

    fun multByPow2(t: EdwardsPoint, k: Int) {
        val r = CompletedPoint()
        val s = ProjectivePoint()
        s.set(t)
        for (i in 0 until k - 1) {
            s.set(r.double(s))
        }
        // Unroll last iteration, so we can directly convert back to an EdwardsPoint.
        set(r.double(s))
    }

    fun mul(basepoint: EdwardsBasepointTable, scalar: Scalar): EdwardsPoint = apply {
        basepoint.mul(this, scalar)
    }
}
