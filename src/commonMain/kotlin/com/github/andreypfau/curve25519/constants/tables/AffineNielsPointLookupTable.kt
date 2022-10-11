package com.github.andreypfau.curve25519.constants.tables

import com.github.andreypfau.curve25519.models.AffineNielsPoint
import com.github.andreypfau.curve25519.subtle.constantTimeEquals

class AffineNielsPointLookupTable(
    val data: Array<AffineNielsPoint> = Array(8) { AffineNielsPoint() }
) {
    operator fun get(index: Int) = data[index]

    fun lookup(x: Byte): AffineNielsPoint {
        // Compute xabs = |x|
        val xmask = x.toInt() ushr 7
        val xabs = ((x + xmask) xor xmask).toByte()

        // t == |x| * P
        val t = AffineNielsPoint()
        lookupAffineNiels(t, xabs)

        // t == x * P.
        val negMask = (xmask and 1).toByte().toInt()
        t.conditionalNegate(negMask)

        return t
    }

    fun lookupAffineNiels(out: AffineNielsPoint, xabs: Byte): AffineNielsPoint {
        out.identity()
        for (j in 1 until 9) {
            // Copy `points[j-1] == j*P` onto `t` in constant time if `|x| == j`.
            val c = xabs.constantTimeEquals(j.toByte())
            out.conditionalAssign(get(j - 1), c)
        }
        return out
    }
}
