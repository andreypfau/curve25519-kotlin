@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.scalar

import com.github.andreypfau.curve25519.internal.getULongLE
import com.github.andreypfau.curve25519.internal.hex

internal val basepointOrder
    get() =
        hex("edd3f55c1a631258d69cf7a2def9de1400000000000000000000000000000010")

internal val order
    get() = ULongArray(4) {
        basepointOrder.getULongLE(it * 8)
    }

internal fun scalarMinimalVarTime(scalar: ByteArray, offset: Int = 0): Boolean {
    if (scalar[offset + 31].toInt() and 240 == 0) return true
    if (scalar[offset + 31].toInt() and 224 != 0) return false

    val order = order
    var i = 3
    while (true) {
        val v = scalar.getULongLE(offset + i * 8)
        if (v > order[i]) return false
        else if (v < order[i]) break
        else if (i == 0) return false
        i--
    }
    return true
}
