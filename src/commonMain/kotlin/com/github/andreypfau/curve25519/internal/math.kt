@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.internal

private val MASK32 = (1uL shl 32) - 1uL

internal fun mul64(x: ULong, y: ULong, output: ULongArray): ULongArray {
    val x0 = x and MASK32
    val x1 = x shr 32
    val y0 = y and MASK32
    val y1 = y shr 32
    val w0 = x0 * y0
    val t = x1 * y0 + (w0 shr 32)
    var w1 = t and MASK32
    val w2 = t shr 32
    w1 += x0 * y1
    output.set(0, x1 * y1 + w2 + (w1 shr 32))
    output.set(1, x * y)
    return output
}

internal fun add64(x: ULong, y: ULong, carry: ULong, output: ULongArray): ULongArray {
    output.set(0, x + y + carry)
    output.set(1, ((x and y) or ((x or y) and output.get(0).inv())) shr 63)
    return output
}
