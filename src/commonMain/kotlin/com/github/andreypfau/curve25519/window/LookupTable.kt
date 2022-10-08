package com.github.andreypfau.curve25519.window

import com.github.andreypfau.curve25519.Identity
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.kotlinio.crypto.ct.Choise
import com.github.andreypfau.kotlinio.crypto.ct.equals.ctEquals
import com.github.andreypfau.kotlinio.crypto.ct.negate.ConditionallyNegatable
import com.github.andreypfau.kotlinio.crypto.ct.select.ConditionallySelectable

class LookupTable<T>(
    val data: Array<out T>,
    val identity: T
) where T : ConditionallySelectable<T>, T : ConditionallyNegatable<T>, T : Identity<T> {
    operator fun get(x: Byte): T {
        val xmask = x.toInt() ushr 7
        val xabs = (x.toInt() + xmask) xor xmask

        // Set t = 0 * P = identity
        var t = identity
        for (j in 1..8) {
            val c = xabs.ctEquals(j)
            t = t.conditionalSelect(data[j - 1], c)
        }
        // Now t == |x| * P.
        val negMask = Choise((xmask and 1).toUByte())
        // Now t == x * P.
        return t.conditionalNegate(negMask)
    }
}
