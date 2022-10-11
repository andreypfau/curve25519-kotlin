package com.github.andreypfau.curve25519.models

import com.github.andreypfau.curve25519.constants.EDWARDS_D2
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.field.FieldElement

data class ProjectiveNielsPoint(
    val yPlusX: FieldElement,
    val yMinusX: FieldElement,
    val z: FieldElement,
    val t2d: FieldElement
) {
    constructor() : this(FieldElement(), FieldElement(), FieldElement(), FieldElement())

    fun identity(): ProjectiveNielsPoint = apply {
        yPlusX.one()
        yMinusX.one()
        z.one()
        t2d.zero()
    }

    fun setEdwards(ep: EdwardsPoint): ProjectiveNielsPoint = apply {
        yPlusX.add(ep.y, ep.x)
        yMinusX.sub(ep.y, ep.x)
        z.set(ep.z)
        t2d.mul(ep.t, EDWARDS_D2)
    }

    fun conditionalSelect(
        a: ProjectiveNielsPoint,
        b: ProjectiveNielsPoint,
        choise: Int
    ) {
        yPlusX.conditionalSelect(a.yPlusX, b.yPlusX, choise)
        yMinusX.conditionalSelect(a.yMinusX, b.yMinusX, choise)
        z.conditionalSelect(a.z, b.z, choise)
        t2d.conditionalSelect(a.t2d, b.t2d, choise)
    }

    fun conditionalAssign(
        other: ProjectiveNielsPoint,
        choise: Int
    ) {
        yPlusX.conditionalAssign(other.yPlusX, choise)
        yMinusX.conditionalAssign(other.yMinusX, choise)
        z.conditionalAssign(other.z, choise)
        t2d.conditionalAssign(other.t2d, choise)
    }

    fun conditionalNegate(
        choise: Int
    ) {
        yPlusX.conditionalSwap(yMinusX, choise)
        t2d.conditionalNegate(choise)
    }
}
