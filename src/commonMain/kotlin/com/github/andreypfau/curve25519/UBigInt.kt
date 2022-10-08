@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519

import kotlin.jvm.JvmInline

private val MASK_32 = (1uL shl 32) - 1u

@JvmInline
value class UBigInt(
    val data: ULongArray
) {
    var upper
        get() = this.data[0]
        private set(value) {
            this.data[0] = value
        }
    var lower
        get() = this.data[1]
        private set(value) {
            this.data[1] = value
        }

    operator fun component1() = data[0]
    operator fun component2() = data[1]

    operator fun plus(rhs: UBigInt): UBigInt =
        UBigInt(
            ulongArrayOf(
                upper + rhs.upper + if ((lower + rhs.lower) < lower) 1uL else 0uL,
                lower + rhs.lower
            )
        )

    operator fun plusAssign(rhs: UBigInt) {
        upper += rhs.upper + if ((lower + rhs.lower) < lower) 1uL else 0uL
        lower += rhs.lower
    }

    operator fun times(rhs: UBigInt): UBigInt {
        // split values into 4 32-bit parts
        val top = ulongArrayOf(upper shr 32, upper and 0xffffffffuL, lower shr 32, lower and 0xffffffffuL)
        val bottom =
            ulongArrayOf(rhs.upper shr 32, rhs.upper and 0xffffffffuL, rhs.lower shr 32, rhs.lower and 0xffffffffuL)
        val products = Array(4) { ULongArray(4) }

        // multiply each component of the values
        for (y in 3 downTo -1 + 1) {
            for (x in 3 downTo -1 + 1) {
                products[3 - x][y] = top[x] * bottom[y]
            }
        }

        // first row
        var fourth32 = products[0][3] and 0xffffffffuL
        var third32 = (products[0][2] and 0xffffffffuL) + (products[0][3] shr 32)
        var second32 = (products[0][1] and 0xffffffffuL) + (products[0][2] shr 32)
        var first32 = (products[0][0] and 0xffffffffuL) + (products[0][1] shr 32)

        // second row
        third32 += products[1][3] and 0xffffffffuL
        second32 += (products[1][2] and 0xffffffffuL) + (products[1][3] shr 32)
        first32 += (products[1][1] and 0xffffffffuL) + (products[1][2] shr 32)

        // third row
        second32 += products[2][3] and 0xffffffffuL
        first32 += (products[2][2] and 0xffffffffuL) + (products[2][3] shr 32)

        // remove carry from current digit
        first32 += products[3][3] and 0xffffffffuL

        // move carry to next digit
        third32 += fourth32 shr 32
        second32 += third32 shr 32
        first32 += second32 shr 32

        // remove carry from current digit
        fourth32 = fourth32 and 0xffffffffuL
        third32 = third32 and 0xffffffffuL
        second32 = second32 and 0xffffffffuL
        first32 = first32 and 0xffffffffuL

        return UBigInt(ulongArrayOf((first32 shl 32) or second32, (third32 shl 32) or fourth32))
    }
}


/**
 * @return the sum with carry of x, y and carry: sum = x + y + carry.
 * The carry input must be 0 or 1; otherwise the behavior is undefined.
 * The carryOut output is guaranteed to be 0 or 1.
 *
 * This function's execution time does not depend on the inputs.
 */
internal inline fun add64(x: ULong, y: ULong, carry: ULong): UBigInt {
    val sum = x + y + carry
    // The sum will overflow if both top bits are set (x & y) or if one of them
    // is (x | y), and a carry from the lower place happened. If such a carry
    // happens, the top bit will be 1 + 0 + 1 = 0 (&^ sum).
    val carryOut = ((x and y) or ((x or y) and sum.inv())) shr 63
    return UBigInt(
        ulongArrayOf(sum, carryOut)
    )
}

internal inline fun addMul64(v: UBigInt, a: ULong, b: ULong): UBigInt {
    val (lo1, hi1) = mul64(a, b)
    val (lo2, c) = add64(lo1, v.upper, 0u)
    val (hi2, _) = add64(hi1, v.lower, c)
    return UBigInt(
        ulongArrayOf(lo2, hi2)
    )
}

internal inline fun mul64(x: ULong, y: ULong): UBigInt {
    val x0 = x and MASK_32
    val x1 = x shr 32
    val y0 = y and MASK_32
    val y1 = y shr 32
    val w0 = x0 * y0
    val t = x1 * y0 + (w0 shr 32)
    var w1 = t and MASK_32
    val w2 = t shr 32
    w1 += x0 * y1
    return UBigInt(
        ulongArrayOf(
            x * y,
            x1 * y1 + w2 + (w1 shr 32)
        )
    )
}

internal inline fun shiftRightBy51(a: UBigInt): ULong {
    return (a.lower shl (64 - 51)) or (a.upper shr 51)
}
