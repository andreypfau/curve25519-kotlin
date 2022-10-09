@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.edwards

import com.github.andreypfau.curve25519.*
import com.github.andreypfau.curve25519.constants.EDWARDS_D
import com.github.andreypfau.curve25519.field.FieldElement
import com.github.andreypfau.curve25519.field.sqrtRatio
import com.github.andreypfau.kotlinio.crypto.ct.Choise

data class CompressedEdwardsY constructor(
    val data: ByteArray = ByteArray(32),
) {
    fun decompress(): EdwardsPoint? {
        val y = FieldElement(data)
        val z = FieldElement.ONE
        val yy = y.square()
        // u = y²-1
        val u = yy - z
        // v = dy²+1
        val v = (yy * EDWARDS_D) + z
        var (x, isValidCoordY) = FieldElement.sqrtRatio(u, v)

        if (!isValidCoordY.toBoolean()) return null

        val compressedSignBit = Choise((data[31].toUByte().toInt() shr 7).toUByte())
        x = x.conditionalNegate(compressedSignBit)
        val t = x * y
        return EdwardsPoint(x, y, z, t)
    }

    fun toByteArray(): ByteArray = toByteArray(ByteArray(32))
    fun toByteArray(output: ByteArray, offset: Int = 0): ByteArray = data.copyInto(output, offset)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompressedEdwardsY) return false

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int = data.contentHashCode()
}
