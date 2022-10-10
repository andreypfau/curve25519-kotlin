@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.scalar

interface UnpackedScalar {
    operator fun get(index: Int): ULong
    operator fun component1(): ULong = get(0)
    operator fun component2(): ULong = get(1)
    operator fun component3(): ULong = get(2)
    operator fun component4(): ULong = get(3)
    operator fun component5(): ULong = get(4)

    companion object {
        fun fromByteArray(byteArray: ByteArray): UnpackedScalar = Scalar52.fromByteArray(byteArray)
    }
}
