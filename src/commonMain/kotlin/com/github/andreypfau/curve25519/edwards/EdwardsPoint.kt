package com.github.andreypfau.curve25519.edwards

import com.github.andreypfau.curve25519.constants.EDWARDS_D
import com.github.andreypfau.curve25519.constants.tables.EdwardsBasepointTable
import com.github.andreypfau.curve25519.exceptioin.InvalidYCoordinateException
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

    fun identity() {
        x.zero()
        y.one()
        z.one()
        t.zero()
    }

    fun set(pp: ProjectivePoint) {
        x.mul(pp.x, pp.z)
        y.mul(pp.y, pp.z)
        z.square(pp.z)
        t.mul(pp.x, pp.y)
    }

    fun set(ap: AffineNielsPoint) {
        identity()
        val sum = CompletedPoint()
        set(sum.add(this, ap))
    }

    fun set(cp: CompletedPoint) {
        x.mul(cp.x, cp.t)
        y.mul(cp.y, cp.z)
        z.mul(cp.z, cp.t)
        t.mul(cp.x, cp.y)
    }

    @Throws(InvalidYCoordinateException::class)
    fun set(compressedY: CompressedEdwardsY) {
        val y = FieldElement().apply {
            set(compressedY)
        }
        val z = FieldElement.one()
        val yy = FieldElement().apply {
            square(y)
        }
        val u = FieldElement().apply {
            sub(yy, z) // u = y^2 - 1
        }
        val v = FieldElement().apply {
            mul(yy, EDWARDS_D) // v = dy^2 + 1
            add(this, z)
        }
        val (_, isValidYCoord) = x.sqrtRationI(u, v)
        if (isValidYCoord != 1)
            require(isValidYCoord == 1) {
                throw InvalidYCoordinateException()
            }

        // sqrtRationI always returns the non-negative square root,
        // so we negate according to the supplied sign bit.
        val compressedSignBit = (compressedY.data[31].toInt() shr 7)
        this.x.conditionalNegate(compressedSignBit)
        this.y.set(y)
        this.z.set(z)
        this.t.mul(x, y)
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

    fun mulBasepoint(basepoint: EdwardsBasepointTable, scalar: Scalar) {
        basepoint.mul(this, scalar)
    }
}
