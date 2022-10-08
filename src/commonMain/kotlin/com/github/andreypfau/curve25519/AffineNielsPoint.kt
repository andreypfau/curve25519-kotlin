package com.github.andreypfau.curve25519

import com.github.andreypfau.curve25519.field.FieldElement
import com.github.andreypfau.curve25519.window.LookupTable
import com.github.andreypfau.kotlinio.crypto.ct.Choise
import com.github.andreypfau.kotlinio.crypto.ct.negate.ConditionallyNegatable
import com.github.andreypfau.kotlinio.crypto.ct.select.ConditionallySelectable

data class AffineNielsPoint(
    val yPlusX: FieldElement,
    val yMinusX: FieldElement,
    val xy2d: FieldElement,
) : Identity<AffineNielsPoint>, ConditionallySelectable<AffineNielsPoint>, ConditionallyNegatable<AffineNielsPoint> {
    constructor() : this(FieldElement.one(), FieldElement.one(), FieldElement.zero())

    override operator fun unaryMinus(): AffineNielsPoint = AffineNielsPoint(
        yPlusX = yMinusX,
        yMinusX = yPlusX,
        xy2d = -xy2d
    )

    override fun identity(): AffineNielsPoint = IDENTITY

    override fun ctEquals(other: AffineNielsPoint): Choise =
        yPlusX.ctEquals(other.yPlusX) and yMinusX.ctEquals(other.yMinusX) and xy2d.ctEquals(other.xy2d)

    override fun conditionalSelect(other: AffineNielsPoint, choise: Choise): AffineNielsPoint =
        AffineNielsPoint(
            yPlusX = yPlusX.conditionalSelect(other.yPlusX, choise),
            yMinusX = yMinusX.conditionalSelect(other.yMinusX, choise),
            xy2d = xy2d.conditionalSelect(other.xy2d, choise)
        )

    override fun conditionalNegate(choise: Choise): AffineNielsPoint =
        conditionalSelect(-this, choise)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AffineNielsPoint) return false
        return ctEquals(other) == Choise.TRUE
    }

    override fun hashCode(): Int {
        var result = yPlusX.hashCode()
        result = 31 * result + yMinusX.hashCode()
        result = 31 * result + xy2d.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("AffineNielsPoint{").appendLine()
        append("  y_plus_x: ").append(yPlusX).appendLine()
        append("  y_minus_x: ").append(yMinusX).appendLine()
        append("  xy2d: ").append(xy2d).appendLine()
        append("}")
    }

    companion object {
        val IDENTITY = AffineNielsPoint()

        internal fun lookupTable(
            vararg points: AffineNielsPoint
        ): LookupTable<AffineNielsPoint> = LookupTable(points, IDENTITY)
    }
}
