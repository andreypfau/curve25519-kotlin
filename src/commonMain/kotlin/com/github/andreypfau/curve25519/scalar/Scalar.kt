package com.github.andreypfau.curve25519.scalar

import com.github.andreypfau.curve25519.edwards.EdwardsBasepointTable
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import kotlin.experimental.and
import kotlin.jvm.JvmInline
import kotlin.random.Random

/**
 * The [Scalar] holds an integer `s < 2^255` which represents an element of `Z / l`.
 *
 */
@JvmInline
value class Scalar(
    /**
     * [bytes] is a little-endian byte encoding of an integer representing a scalar modulo the group order.
     */
    val bytes: ByteArray
) {
    init {
        // Ensure that s < 2^255 by masking the high bit
        bytes[31] = bytes[31] and 0b0111_1111
    }

    constructor(x: ULong) : this(
        ByteArray(32).also {
            it[0] = x.toByte()
            it[1] = (x shr 8).toByte()
            it[2] = (x shr 16).toByte()
            it[3] = (x shr 24).toByte()
            it[4] = (x shr 32).toByte()
            it[5] = (x shr 40).toByte()
            it[6] = (x shr 48).toByte()
            it[7] = (x shr 56).toByte()
        }
    )

    operator fun times(table: EdwardsBasepointTable): EdwardsPoint = table * this

    operator fun times(point: EdwardsPoint): EdwardsPoint = point * this

    /**
     * Write this scalar in radix 16, with coefficients in \([-8,8)\), i.e.,
     * compute \(a_i\) such that $$ a = a_0 + a_1 16^1 + \cdots + a_{63} 16^{63}, $$
     * with \(-8 \leq a_i < 8\) for \(0 \leq i < 63\) and \(-8 \leq a_{63} \leq 8\).
     */
    fun toRadix16(): ByteArray {
        val output = ByteArray(64)

        // Step 1: change radix.
        // Convert from radix 256 (bytes) to radix 16 (nibbles)
        for (i in 0 until 32) {
            output[2 * i] = ((bytes[i].toInt() shr 0) and 15).toByte()
            output[2 * i + 1] = ((bytes[i].toInt() shr 4) and 15).toByte()
        }

        // Precondition note: since self[31] <= 127, output[63] <= 7
        // Step 2: recenter coefficients from [0,16) to [-8,8)

        for (i in 0 until 63) {
            val carry = (output[i] + 8) shr 4
            output[i] = (output[i] - (carry shl 4).toByte()).toByte()
            output[i + 1] = (output[i + 1] + carry).toByte()
        }
        // Precondition note: output[63] is not recentered.  It
        // increases by carry <= 1.  Thus output[63] <= 8.
        return output
    }

    fun unpack(): UnpackedScalar = UnpackedScalar.fromBytes(bytes)

    fun reduce(): Scalar = TODO()

    companion object {
        val ONE = Scalar(
            byteArrayOf(
                1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            )
        )

        fun random(random: Random): Scalar {
            random.nextBytes(64)
            TODO()
        }

        fun fromBytesModOrderWide(bytes: ByteArray) {
//            UnpackedScalar.fromBytes()
        }

    }
}
