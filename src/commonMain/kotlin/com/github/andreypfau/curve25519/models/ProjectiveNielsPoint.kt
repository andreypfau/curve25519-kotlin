package com.github.andreypfau.curve25519.models

import com.github.andreypfau.curve25519.Identity
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.field.FieldElement
import com.github.andreypfau.curve25519.window.LookupTable
import com.github.andreypfau.kotlinio.crypto.ct.Choise
import com.github.andreypfau.kotlinio.crypto.ct.negate.ConditionallyNegatable
import com.github.andreypfau.kotlinio.crypto.ct.select.ConditionallySelectable

data class ProjectiveNielsPoint(
    val yPlusX: FieldElement,
    val yMinusX: FieldElement,
    val z: FieldElement,
    val t2d: FieldElement
) : Identity<ProjectiveNielsPoint>, ConditionallySelectable<ProjectiveNielsPoint>,
    ConditionallyNegatable<ProjectiveNielsPoint> {
    constructor() : this(FieldElement.ONE, FieldElement.ONE, FieldElement.ONE, FieldElement.ZERO)

    override fun ctEquals(other: ProjectiveNielsPoint): Choise =
        yPlusX.ctEquals(other.yPlusX) and
                yMinusX.ctEquals(other.yMinusX) and
                z.ctEquals(other.z) and
                t2d.ctEquals(other.t2d)

    override fun identity(): ProjectiveNielsPoint = IDENTITY

    override fun conditionalSelect(other: ProjectiveNielsPoint, choise: Choise): ProjectiveNielsPoint =
        ProjectiveNielsPoint(
            yPlusX.conditionalSelect(other.yPlusX, choise),
            yMinusX.conditionalSelect(other.yMinusX, choise),
            z.conditionalSelect(other.z, choise),
            t2d.conditionalSelect(other.t2d, choise)
        )

    override fun conditionalNegate(choise: Choise): ProjectiveNielsPoint =
        conditionalSelect(unaryMinus(), choise)

    override fun unaryMinus(): ProjectiveNielsPoint =
        ProjectiveNielsPoint(
            yPlusX = yMinusX,
            yMinusX = yPlusX,
            z = z,
            t2d = -t2d
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProjectiveNielsPoint) return false
        return ctEquals(other) == Choise.TRUE
    }

    override fun hashCode(): Int {
        var result = yPlusX.hashCode()
        result = 31 * result + yMinusX.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + t2d.hashCode()
        return result
    }

    companion object {
        val IDENTITY = ProjectiveNielsPoint()

        fun lookupTable(point: EdwardsPoint): LookupTable<ProjectiveNielsPoint> {
            val projectiveNielsPoint = point.toProjectiveNiels()
            val points = Array(8) { projectiveNielsPoint }
            for (j in 0 until 7) {
                points[j + 1] = (point + points[j]).toExtended().toProjectiveNiels()
            }
            return LookupTable(points, IDENTITY)
        }
    }
}
