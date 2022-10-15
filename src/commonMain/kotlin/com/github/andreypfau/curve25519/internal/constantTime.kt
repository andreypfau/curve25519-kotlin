@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.internal

internal inline fun Int.constantTimeSelect(x: Int, y: Int): Int =
    ((this - 1).inv() and x) or ((this - 1) and y)

internal inline fun Int.constantTimeSelect(a: Byte, b: Byte): Byte =
    constantTimeSelect(a.toInt(), b.toInt()).toByte()

internal inline fun Int.constantTimeSelect(a: Long, b: Long): Long {
    val mask = (-this).toLong()
    return b xor (mask and (a xor b))
}

internal inline infix fun Byte.constantTimeEquals(y: Byte): Int =
    (((this.toInt() xor y.toInt()).toUInt() - 1u) shr 31).toInt()

internal inline infix fun Int.constantTimeEquals(y: Int): Int =
    (((this xor y).toUInt().toULong() - 1u) shr 63).toInt()

internal inline fun Int.constantTimeSwap(a: Long, b: Long, block: (Long, Long) -> Unit) {
    val mask = (-this).toLong()
    val t = mask and (a xor b)
    block(a xor t, b xor t)
}

internal inline infix fun ByteArray.constantTimeEquals(other: ByteArray): Int {
    if (size != other.size) return 0
    var v = 0
    for (i in indices) {
        v = v or (this[i].toInt() xor other[i].toInt())
    }
    return v.constantTimeEquals(0)
}
