package com.github.andreypfau.curve25519.scalar

import kotlin.experimental.and

class Scalar(
    val data: ByteArray = ByteArray(SIZE_BYTES)
) {
    fun setRawData(input: ByteArray, offset: Int = 0): Scalar = apply {
        input.copyInto(data, 0, offset, offset + SIZE_BYTES)
        data[31] = data[31] and 0x7f
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
