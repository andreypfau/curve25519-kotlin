@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.scalar

import com.github.andreypfau.curve25519.constants.R
import com.github.andreypfau.curve25519.internal.scalarMontgomeryReduce
import com.github.andreypfau.curve25519.internal.scalarMulInternal
import kotlin.experimental.and

class Scalar(
    val data: ByteArray = ByteArray(SIZE_BYTES)
) {
    fun toByteArray(): ByteArray = toByteArray(ByteArray(SIZE_BYTES))
    fun toByteArray(output: ByteArray, offset: Int = 0): ByteArray =
        data.copyInto(output, offset)

    fun rawData(input: ByteArray, offset: Int = 0) {
        input.copyInto(data, 0, offset, offset + SIZE_BYTES)
        data[31] = data[31] and 0x7f
    }

    /**
     * Sets s to the scalar constructed by reducing a 512-bit
     * little-endian integer modulo the group order L.
     */
    fun wideBytes(input: ByteArray, offset: Int = 0) {
        val us = UnpackedScalar().apply {
            bytesWide(input, offset)
        }
        pack(us)
    }

    fun unpack(): UnpackedScalar = UnpackedScalar().also {
        it.bytes(data)
    }

    fun bits(input: ByteArray, offset: Int = 0) {
        input.copyInto(data, 0, offset, offset + SIZE_BYTES)
        // Ensure that s < 2^255 by masking the high bit
        data[31] = data[31] and 0b0111_1111
    }

    fun toRadix16(output: ByteArray = ByteArray(64)): ByteArray {
        // Step 1: change radix.
        // Convert from radix 256 (bytes) to radix 16 (nibbles)
        for (i in 0 until 32) {
            output[2 * i] = botHalf(data[i])
            output[2 * i + 1] = topHalf(data[i])
        }
        // Precondition note: since self[31] <= 127, output[63] <= 7

        // Step 2: recenter coefficients from [0,16) to [-8,8)
        for (i in 0 until 63) {
            val carry = (output[i].toInt() + 8) shr 4
            output[i] = (output[i].toInt() - (carry shl 4)).toByte()
            output[i + 1] = (output[i + 1].toInt() + carry).toByte()
        }
        // Precondition note: output[63] is not recentered.  It
        // increases by carry <= 1.  Thus output[63] <= 8.
        return output
    }

    fun pack(us: UnpackedScalar) {
        us.toByteArray(data)
    }

    /**
     * Sets `s = a * b (mod l)`, and returns s.
     */
    fun mul(a: Scalar, b: Scalar) {
        val unpacked = a.unpack()
        unpacked.mul(unpacked, b.unpack())
        pack(unpacked)
    }

    /**
     * Sets `s= a + b (mod l)`
     */
    fun add(a: Scalar, b: Scalar) {
        val unpacked = a.unpack()

        // The UnpackedScalar.add function produces reduced outputs
        // if the inputs are reduced.  However, these inputs may not
        // be reduced -- they might come from Scalar.SetBits.  So
        // after computing the sum, we explicitly reduce it mod l
        // before repacking.
        unpacked.add(unpacked, b.unpack())
        val z = scalarMulInternal(unpacked.data, R.data)
        scalarMontgomeryReduce(z, unpacked.data)
        pack(unpacked)
    }

    companion object {
        const val SIZE_BYTES = 32
        const val WIDE_SIZE_BYTES = 64

        fun fromByteArray(byteArray: ByteArray): Scalar {
            byteArray[31] = (byteArray[31].toInt() and 0x7F).toByte()
            return Scalar(byteArray)
        }
    }
}

private inline fun botHalf(x: Byte): Byte =
    ((x.toInt() ushr 0) and 15).toByte()

private inline fun topHalf(x: Byte): Byte =
    ((x.toInt() ushr 4) and 15).toByte()
