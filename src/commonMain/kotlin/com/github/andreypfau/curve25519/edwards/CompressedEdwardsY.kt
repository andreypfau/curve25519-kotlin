package com.github.andreypfau.curve25519.edwards

import com.github.andreypfau.curve25519.constants.NON_CANONICAL_SIGN_BITS
import com.github.andreypfau.curve25519.field.FieldElement
import kotlin.experimental.xor

class CompressedEdwardsY constructor(
    val data: ByteArray
) {
    constructor() : this(ByteArray(SIZE_BYTES))

    fun set(byteArray: ByteArray, offset: Int = 0) {
        byteArray.copyInto(data, 0, offset, offset + SIZE_BYTES)
    }

    fun set(point: EdwardsPoint): CompressedEdwardsY = apply {
        val x = FieldElement()
        val y = FieldElement()
        val recip = FieldElement()
        recip.invert(point.z)
        x.mul(point.x, recip)
        y.mul(point.y, recip)

        y.toBytes(data)
        data[31] = data[31] xor (x.isNegative() shl 7).toByte()
    }

    fun isCannonicalVartime(): Boolean {
        if (!yCanonnical()) return false
        for (invalidEncodings in NON_CANONICAL_SIGN_BITS) {
            if (data.contentEquals(invalidEncodings.data)) return false
        }
        return true
    }

    private inline fun yCanonnical(): Boolean {
        if (data[0].toUInt() < 237u) return true
        for (i in 1 until 31) {
            if (data[i].toUInt() != 255u) return true
        }
        return (data[31].toUInt() or 128u) != 255u
    }

    companion object {
        const val SIZE_BYTES = 32
    }
}
