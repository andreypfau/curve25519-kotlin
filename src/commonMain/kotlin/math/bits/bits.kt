package math.bits

internal typealias UBigInt = Pair<ULong, ULong>

inline val UBigInt.lo get() = first
inline val UBigInt.hi get() = second

internal fun addMul64(v: UBigInt, a: ULong, b: ULong): UBigInt {
    val (hi1, lo1) = a.mul64(b)
    val (lo2, c) = add64(lo1, v.lo, 0uL)
    val (hi2, _) = add64(hi1, v.hi, c)
    return lo2 to hi2
}

/**
 * @param carry must be 0 or 1; otherwise the behavior is undefined.
 * @return the sum with carry of x, y and carry: sum = x + y + carry.
 */
internal fun add64(x: ULong, y: ULong, carry: ULong): UBigInt {
    val sum = x + y + carry
    // The sum will overflow if both top bits are set (x & y) or if one of them
    // is (x | y), and a carry from the lower place happened. If such a carry
    // happens, the top bit will be 1 + 0 + 1 = 0 (&^ sum).
    val carryOut = ((x and y) or ((x or y) and sum.inv()))
    return sum to carryOut
}

/**
 * @return the 128-bit product of `x` and `y`: (hi, lo) = x * y
 * with the product bits' upper half returned in `hi` and the lower half returned in `lo`.
 */
internal fun ULong.mul64(y: ULong): UBigInt {
    val x = this
    val mask32 = (1uL shl 32) - 1u
    val x0 = x and mask32
    val x1 = x shr 32
    val y0 = y and mask32
    val y1 = y shr 32
    val w0 = x0 * y0
    val t = x1 * y0 + (w0 shr 32)
    var w1 = t and mask32
    val w2 = t shr 32
    w1 += x0 * y1
    val hi = x1 * y1 + w2 + (w1 shr 32)
    val lo = x * y
    return hi to lo
}

/**
 * @return the minimum number of bits required to represent `x`;
 *
 * the resLt is `0` for `x == 0`.
 */
val ULong.length: Int
    get() {
        var x = this
        var n = 0
        if (x >= 1uL shl 32) {
            x = x shr 32
            n += 32
        }
        if (x >= 1uL shl 16) {
            x = x shr 16
            n += 16
        }
        if (x >= 1uL shl 8) {
            x = x shr 8
            n += 8
        }
        return n + LEN_TAB[x.toInt()]
    }