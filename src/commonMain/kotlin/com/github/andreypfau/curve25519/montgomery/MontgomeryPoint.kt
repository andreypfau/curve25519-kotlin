@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.montgomery

import com.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.field.FieldElement
import com.github.andreypfau.curve25519.scalar.Scalar
import kotlin.experimental.xor
import kotlin.jvm.JvmStatic

class MontgomeryPoint(
    val data: ByteArray = ByteArray(SIZE_BYTES)
) {
    fun toEdwards(sign: Int, output: EdwardsPoint = EdwardsPoint()): EdwardsPoint {
        // To decompress the Montgomery u coordinate to an
        // `EdwardsPoint`, we apply the birational map to obtain the
        // Edwards y coordinate, then do Edwards decompression.
        //
        // The birational map is y = (u-1)/(u+1).
        //
        // The exceptional points are the zeros of the denominator,
        // i.e., u = -1.
        //
        // But when u = -1, v^2 = u*(u^2+486662*u+1) = 486660.
        //
        // Since this is nonsquare mod p, u = -1 corresponds to a point
        // on the twist, not the curve, so we can reject it early.

        val u = FieldElement.fromBytes(data)
        if (u.inner.contentEquals(MINUS_ONE.inner)) throw IllegalStateException("Montgomery u-coordinate is on twist")

        val uMinusOne = FieldElement.sub(u, ONE)
        val uPlusOne = FieldElement.add(u, ONE)
        uPlusOne.invert(uPlusOne)
        val y = FieldElement.mul(uMinusOne, uPlusOne)
        val yCompressed = CompressedEdwardsY()
        y.toBytes(yCompressed.data)
        yCompressed.data[31] = yCompressed.data[31] xor (sign shl 7).toByte()
        output.set(yCompressed)
        return output
    }

    fun mul(point: MontgomeryPoint, scalar: Scalar) = mul(point, scalar, this)

    companion object {
        const val SIZE_BYTES = 32
        private val ONE = FieldElement.one()
        private val MINUS_ONE = FieldElement.minusOne()

        @JvmStatic
        fun from(
            ep: EdwardsPoint,
            output: MontgomeryPoint = MontgomeryPoint()
        ): MontgomeryPoint {
            val u = FieldElement.add(ep.z, ep.y)
            val w = FieldElement.sub(ep.z, ep.y)
            w.invert(w)
            u.mul(u, w)
            u.toBytes(output.data)
            return output
        }

        fun mul(
            point: MontgomeryPoint,
            scalar: Scalar,
            output: MontgomeryPoint = MontgomeryPoint()
        ): MontgomeryPoint {
            val affineU = FieldElement.fromBytes(point.data)
            val x0 = MontgomeryProjectivePoint.identity()
            val x1 = MontgomeryProjectivePoint(affineU, ONE)

            val bits = scalar.data
            for (i in 255 downTo 0) {
                val choise = (bits[i + 1] xor bits[i]).toInt()

                x0.conditionalSwap(x1, choise)

            }

            return output
        }

        private fun montgomeryDifferentialAddAndDouble(
            p: MontgomeryProjectivePoint,
            q: MontgomeryProjectivePoint,
            affinePmQ: FieldElement
        ) {
            val t0 = FieldElement.add(p.u, p.w) // t0 = (u0 + w0)
            val t1 = FieldElement.sub(p.u, p.w) // t1 = (u0 - w0)
            val t2 = FieldElement.add(q.u, q.w) // t2 = (u1 + w1)
            val t3 = FieldElement.sub(q.u, q.w) // t3 = (u1 - w1)

            val t4 = FieldElement.square(t0) // t4 = (u0 + w0)^2
            val t5 = FieldElement.square(t1) // t5 = (u0 - w0)^2

            val t6 = FieldElement.sub(t4, t5) // t6 = (u0 + w0)^2 - (u0 - w0)^2


        }
    }
}
