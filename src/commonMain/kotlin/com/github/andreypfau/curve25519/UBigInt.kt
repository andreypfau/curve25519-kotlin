@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519

private val MASK_32 = (1uL shl 32) - 1u

internal typealias UBigInt = ULongArray

val UBigInt.lo get() = this[0]
val UBigInt.hi get() = this[1]

/**
 * @return the sum with carry of x, y and carry: sum = x + y + carry.
 * The carry input must be 0 or 1; otherwise the behavior is undefined.
 * The carryOut output is guaranteed to be 0 or 1.
 *
 * This function's execution time does not depend on the inputs.
 */
internal fun add64(x: ULong, y: ULong, carry: ULong): ULongArray {
    val sum = x + y + carry
    // The sum will overflow if both top bits are set (x & y) or if one of them
    // is (x | y), and a carry from the lower place happened. If such a carry
    // happens, the top bit will be 1 + 0 + 1 = 0 (&^ sum).
    val carryOut = ((x and y) or ((x or y) and sum.inv())) shr 63
    return ulongArrayOf(sum, carryOut)
}

internal fun addMul64(v: UBigInt, a: ULong, b: ULong): UBigInt {
    val (lo1, hi1) = mul64(a, b)
    val (lo2, c) = add64(lo1, v.lo, 0u)
    val (hi2, _) = add64(hi1, v.hi, c)
    return ulongArrayOf(lo2, hi2)
}

internal fun mul64(x: ULong, y: ULong): UBigInt {
    val x0 = x and MASK_32
    val x1 = x shr 32
    val y0 = y and MASK_32
    val y1 = y shr 32
    val w0 = x0 * y0
    val t = x1 * y0 + (w0 shr 32)
    var w1 = t and MASK_32
    val w2 = t shr 32
    w1 += x0 * y1
    return ulongArrayOf(
        x * y,
        x1 * y1 + w2 + (w1 shr 32)
    )
}

internal fun shiftRightBy51(a: UBigInt): ULong {
    return (a.hi shl (64 - 51)) or (a.lo shr 51)
}
