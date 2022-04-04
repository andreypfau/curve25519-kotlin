package curve25519

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.jvm.JvmInline

@JvmInline
value class Scalar(
    val value: ByteArray,
) {
    /**
     * Get the bits of the scalar.
     */
    fun bits(): BooleanArray = BooleanArray(256) { index ->
        ((value[index shr 3].toInt() shr (index and 7)) and 1) != 0
    }

    companion object {
        /**
         * "Decode" a scalar from a 32-byte array.
         * By "decode" here, what is really meant is applying key clamping by twiddling some bits.
         */
        fun clamp(input: ByteArray): Scalar {
            input[0] = input[0] and 248.toByte()
            input[31] = input[31] and 127.toByte()
            input[31] = input[31] or 64
            return Scalar(input)
        }
    }
}

operator fun Scalar.times(point: MontgomeryPoint) = mul(point, this)
operator fun MontgomeryPoint.times(scalar: Scalar) = mul(this, scalar)

internal fun mul(a: MontgomeryPoint, b: Scalar): MontgomeryPoint {
    val affineU = FieldElement(a.toByteArray())
    var p = ProjectivePoint()
    var q = ProjectivePoint(u = affineU, w = FieldElement.one())

    fun conditionalSwap(condition: Boolean) {
        val temp = p
        p = conditionalSelect(p, q, condition)
        q = conditionalSelect(q, temp, condition)
    }

    val bits = b.bits()

    for (i in 255 downTo 0) {
        val choise = bits[i + 1] xor bits[i]
        conditionalSwap(choise)

        val t0 = p.u + p.w
        val t1 = p.u - p.w
        val t2 = q.u + q.w
        val t3 = q.u - q.w

        val t4 = t0.square() // (U_P + W_P)^2 = U_P^2 + 2 U_P W_P + W_P^2
        val t5 = t1.square() // (U_P - W_P)^2 = U_P^2 - 2 U_P W_P + W_P^2

        val t6 = t4 - t5 // 4 U_P W_P

        val t7 = t0 * t3 // (U_P + W_P) (U_Q - W_Q) = U_P U_Q + W_P U_Q - U_P W_Q - W_P W_Q
        val t8 = t1 * t2 // (U_P - W_P) (U_Q + W_Q) = U_P U_Q - W_P U_Q + U_P W_Q - W_P W_Q

        val t9 = t7 + t8 // 2 (U_P U_Q - W_P W_Q)
        val t10 = t7 - t8 // 2 (W_P U_Q - U_P W_Q)

        val t11 = t9.square() // 4 (U_P U_Q - W_P W_Q)^2
        val t12 = t10.square() // 4 (W_P U_Q - U_P W_Q)^2

        val t13 = APLUS2_OVER_FOUR * t6 // (A + 2) U_P U_Q

        val t14 = t4 * t5 // ((U_P + W_P)(U_P - W_P))^2 = (U_P^2 - W_P^2)^2
        val t15 = t13 + t5 // (U_P - W_P)^2 + (A + 2) U_P W_P

        val t16 = t6 * t15 // 4 (U_P W_P) ((U_P - W_P)^2 + (A + 2) U_P W_P)

        val t17 = affineU * t12 // U_D * 4 (W_P U_Q - U_P W_Q)^2

        p = ProjectivePoint(
            u = t14, // U_{P'} = (U_P + W_P)^2 (U_P - W_P)^2
            w = t16  // W_{P'} = (4 U_P W_P) ((U_P - W_P)^2 + ((A + 2)/4) 4 U_P W_P)
        )
        q = ProjectivePoint(
            u = t11, // U_{Q'} = W_D * 4 (U_P U_Q - W_P W_Q)^2
            w = t17  // W_{Q'} = U_D * 4 (W_P U_Q - U_P W_Q)^2
        )
    }
    conditionalSwap(bits[0])
    return p.toAffine()
}