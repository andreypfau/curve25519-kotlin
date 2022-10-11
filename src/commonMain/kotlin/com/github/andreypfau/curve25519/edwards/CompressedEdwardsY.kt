package com.github.andreypfau.curve25519.edwards

import com.github.andreypfau.curve25519.field.FieldElement
import kotlin.experimental.xor

class CompressedEdwardsY private constructor(
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

    companion object {
        const val SIZE_BYTES = 32
    }
}
