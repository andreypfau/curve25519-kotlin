@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.scalar

import com.github.andreypfau.curve25519.mul64

class UnpackedScalar(
    val data: ULongArray
) {
    operator fun get(index: Int): ULong = data[index]

    companion object {
        fun zero() = UnpackedScalar(ulongArrayOf(0u, 0u, 0u, 0u, 0u))

        fun fromBytes(bytes: ByteArray): UnpackedScalar {
            val words = ULongArray(4)
            for (i in 0 until 4) {
                for (j in 0 until 8) {
                    words[i] = words[i] or (bytes[(i * 8) + j].toULong() shl (j * 8))
                }
            }
            val mask = (1uL shl 52) - 1u
            val topMask = (1uL shl 48) - 1u
            return UnpackedScalar(
                ulongArrayOf(
                    words[0] and mask,
                    ((words[0] shr 52) or (words[1] shl 12)) and mask,
                    ((words[1] shr 40) or (words[2] shl 24)) and mask,
                    ((words[2] shr 28) or (words[2] shl 36)) and mask,
                    (words[3] shr 16) and topMask
                )
            )
        }
    }
}

internal inline fun mulInternal(a: UnpackedScalar, b: UnpackedScalar) {
    mul64(a[0], b[0])
    mul64(a[0], b[1]) + mul64(a[1], b[0])

}
