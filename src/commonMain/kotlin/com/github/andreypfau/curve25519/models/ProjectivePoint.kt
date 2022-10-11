package com.github.andreypfau.curve25519.models

import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.field.FieldElement

data class ProjectivePoint(
    val x: FieldElement,
    val y: FieldElement,
    val z: FieldElement
) {
    constructor() : this(FieldElement(), FieldElement(), FieldElement())

    fun identity(): ProjectivePoint = apply {
        x.zero()
        y.one()
        z.one()
    }

    fun set(cp: CompletedPoint): ProjectivePoint = apply {
        x.mul(cp.x, cp.t)
        y.mul(cp.y, cp.z)
        z.mul(cp.z, cp.t)
    }

    fun set(ep: EdwardsPoint): ProjectivePoint = apply {
        x.set(ep.x)
        y.set(ep.y)
        z.set(ep.z)
    }
}
