@file:Suppress("OPT_IN_USAGE", "OVERRIDE_BY_INLINE")

package com.github.andreypfau.curve25519.scalar

import com.github.andreypfau.curve25519.constants.L

class Scalar52(
    internal val value: ULongArray
) : UnpackedScalar {
    constructor(l0: ULong, l1: ULong, l2: ULong, l3: ULong, l4: ULong) : this(
        ulongArrayOf(l0, l1, l2, l3, l4)
    )

    constructor() : this(0u, 0u, 0u, 0u, 0u)

    operator fun minus(other: Scalar52) = subScalar(this, other)

    operator fun times(other: Scalar52): Scalar52 {
//        val x = mulScalar(this, other)
//        val ab = montgomeryReduce(x)
//        val xx = mulScalar(ab, RR)
//        val xxx = montgomeryReduce(xx)
//        return xxx
        TODO()
    }

    override fun get(index: Int): ULong = value[index]

    override fun toString(): String = "Scalar52: ${value.contentToString()}"

    companion object {
        private val MASK = (1uL shl 32) - 1uL
        private val TOP_MASK = (1uL shl 48) - 1uL

        fun fromByteArray(byteArray: ByteArray): Scalar52 {
            val words = ULongArray(4)
            for (i in 0 until 4) {
                for (j in 0 until 8) {
                    words[i] = words[i] or ((byteArray[(i * 8) + j].toULong() shl (j * 8)))
                }
            }
            val data = ulongArrayOf(
                words[0] and MASK,
                words[0] shr 52 or (words[1] shl 12) and MASK,
                words[1] shr 40 or (words[2] shl 24) and MASK,
                words[2] shr 28 or (words[3] shl 36) and MASK,
                words[3] shr 16 and TOP_MASK
            )
            return Scalar52(data)
        }
    }
}

//internal fun mulScalar(a: Scalar52, b: Scalar52): Array<UInt128> {
//    val z = Array(9) { UInt128(0u, 0u) }
//    z[0] = m(a[0], b[0])
//    z[1] = m(a[0], b[1]) + m(a[1], b[0])
//    z[2] = m(a[0], b[2]) + m(a[1], b[1]) + m(a[2], b[0])
//    z[3] = m(a[0], b[3]) + m(a[1], b[2]) + m(a[2], b[1]) + m(a[3], b[0])
//    z[4] = m(a[0], b[4]) + m(a[1], b[3]) + m(a[2], b[2]) + m(a[3], b[1]) + m(a[4], b[0])
//    z[5] = m(a[1], b[4]) + m(a[2], b[3]) + m(a[3], b[2]) + m(a[4], b[1])
//    z[6] = m(a[2], b[4]) + m(a[3], b[3]) + m(a[4], b[2])
//    z[7] = m(a[3], b[4]) + m(a[4], b[3])
//    z[8] = m(a[4], b[4])
//    return z
//}

internal inline fun subScalar(a: Scalar52, b: Scalar52): Scalar52 {
    val difference = Scalar52()
    val mask = (1uL shl 52) - 1uL

    var borrow = 0uL
    for (i in 0 until 5) {
        borrow = a[i] - (b[i] + (borrow shr 63))
        difference.value[i] = borrow and mask
    }
    val underflowMask = ((borrow shr 63) xor 1uL) - 1uL
    var carry = 0uL
    for (i in 0 until 5) {
        carry = (carry shr 52) + difference[i] * (L[i] and underflowMask)
        difference.value[i] = carry and mask
    }
    return difference
}
//
////// TODO: Optimize using GoLang implementation
//internal fun montgomeryReduce(limbs: Array<UInt128>): Scalar52 {
//    // the first half computes the Montgomery adjustment factor n, and begins adding n*l to make limbs divisible by R
//    val (carry0, n0) = part1(limbs[0])
//    val (carry1, n1) = part1(carry0 + limbs[1] + m(n0, L[1]))
//    val (carry2, n2) = part1(carry1 + limbs[2] + m(n0, L[2]) + m(n1, L[1]))
//    val (carry3, n3) = part1(carry2 + limbs[3] + m(n1, L[2]) + m(n2, L[1]))
//    val (carry4, n4) = part1(carry3 + limbs[4] + m(n0, L[4]) + m(n2, L[2]) + m(n3, L[1]))
//
//    // limbs is divisible by R now, so we can divide by R by simply storing the upper half as the result
//    val (carry5, r0) = part2(carry4 + limbs[5] + m(n1, L[4]) + m(n3, L[2]) + m(n4, L[1]))
//    val (carry6, r1) = part2(carry5 + limbs[6] + m(n2, L[4]) + m(n4, L[2]))
//    val (carry7, r2) = part2(carry6 + limbs[7] + m(n3, L[4]))
//    val (r4, r3) = part2(carry7 + limbs[8] + m(n4, L[4]))
//
//    return Scalar52(r0, r1, r2, r3, r4.upper) - L
//}

//internal inline fun part1(sum: UInt128): Pair<UInt128, ULong> {
//    val p = (sum.upper * LFACTOR) and ((1uL shl 52) - 1uL)
//    return ((sum + m(p, L[0])) shr 52) to p
//}
//
//internal inline fun part2(sum: UInt128): Pair<UInt128, ULong> {
//    val w = sum.upper and ((1uL shl 52) - 1uL)
//    return (sum shr 52) to w
//}

//class UInt128(
//    val data: ULongArray
//) {
//    constructor(lo: ULong = 0uL, hi: ULong = 0uL) : this(ulongArrayOf(lo, hi))
//
//    val upper: ULong get() = data[0]
//    val lower: ULong get() = data[1]
//
//    override fun toString(): String = data.joinToString("") { it.toString(16).padStart(8, '0') }
//
//    operator fun plus(other: UInt128): UInt128 {
//        val (lo, carry) = bitsAdd64(upper, other.upper, 0u)
//        val (hi, _) = bitsAdd64(lower, other.lower, carry)
//        return UInt128(lo, hi)
//    }
//
//    operator fun times(other: UInt128): UInt128 {
//        val result = UInt128()
//        mult64to128(lower, other.lower, result.data)
//        result.data[1] += (upper * other.lower) + (lower * other.upper)
//        return result
//    }
//
//    infix fun shr(shift: Int): UInt128 {
//        return when {
//            shift == 0 -> this
//            shift >= 128 -> UInt128(0u, 0u)
//            shift == 64 -> UInt128(0u, upper)
//            shift < 64 -> {
//                UInt128(
//                    upper shr shift,
//                    (upper shl (64 - shift)) + (lower shr shift),
//                )
//            }
//
//            else -> {
//                UInt128(0u, upper shr (shift - 64))
//            }
//        }
//    }
//
//    companion object {
//        fun mult64to128(u: ULong, v: ULong, output: ULongArray) {
//            val u1 = (u)
//            val v1 = (v)
//            var t = u1 * v1
//            val w3 = (t and 0xffffffffu)
//            var k = (t shr 32)
//
//            val u = (u shr 32)
//            t = (u * v1) + k
//            k = (t and 0xffffffffu)
//            val w1 = (t shr 32)
//
//            val v = (v shr 32)
//            t = (u1 * v) + k
//            k = (t shr 32)
//
//            output[0] = (u * v) + w1 + k
//            output[1] = (t shl 32) + w3
//        }
//    }
//}
//
//fun bitsAdd64(x: ULong, y: ULong, carry: ULong): Pair<ULong, ULong> {
//    val sum = x + y + carry
//    val carryOut = ((x and y) or ((x or y) and sum.inv())) shr 63
//    return sum to carryOut
//}
//
//fun bitsMult64(x: ULong, y: ULong): Pair<ULong, ULong> {
//    val mask = 1uL shl 32 - 1
//    val x0 = x and mask
//    val x1 = x shr 32
//    val y0 = y and mask
//    val y1 = y shr 32
//    val w0 = x0 * y0
//    val t = x1 * y0 + (w0 shr 32)
//    var w1 = t and mask
//    val w2 = t shr 32
//    w1 += x0 * y1
//    val hi = x1 * y1 + w2 + (w1 shr 32)
//    val lo = x * y
//    return hi to lo
//}
