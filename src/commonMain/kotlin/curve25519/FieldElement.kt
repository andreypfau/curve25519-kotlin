@file:Suppress("OPT_IN_USAGE")

package curve25519

class FieldElement(
    val data: ULongArray
) {
    operator fun get(index: Int) = data[index]
    operator fun set(index: Int, value: ULong) {
        data[index] = value
    }

    operator fun plus(b: FieldElement): FieldElement {
        val a = this
        val v = ulongArrayOf(
            a[0] + b[0],
            a[1] + b[1],
            a[2] + b[2],
            a[3] + b[3],
            a[4] + b[4],
        )
        reduce(v)
        return FieldElement(v)
    }

    operator fun minus(b: FieldElement): FieldElement {
        val a = this
        val v = ULongArray(5)

        // We first add 2 * p, to guarantee the subtraction won't underflow, and
        // then subtract b (which can be up to 2^255 + 2^13 * 19).
        v[0] = (a[0] + 0xFFFFFFFFFFFDAu) - b[0]
        v[1] = (a[1] + 0xFFFFFFFFFFFFEu) - b[1]
        v[2] = (a[2] + 0xFFFFFFFFFFFFEu) - b[2]
        v[3] = (a[3] + 0xFFFFFFFFFFFFEu) - b[3]
        v[4] = (a[4] + 0xFFFFFFFFFFFFEu) - b[4]
        reduce(v)

        return FieldElement(v)
    }

    // Limb multiplication works like pen-and-paper columnar multiplication, but
    // with 51-bit limbs instead of digits.
    //
    //                          a4   a3   a2   a1   a0  x
    //                          b4   b3   b2   b1   b0  =
    //                         ------------------------
    //                        a4b0 a3b0 a2b0 a1b0 a0b0  +
    //                   a4b1 a3b1 a2b1 a1b1 a0b1       +
    //              a4b2 a3b2 a2b2 a1b2 a0b2            +
    //         a4b3 a3b3 a2b3 a1b3 a0b3                 +
    //    a4b4 a3b4 a2b4 a1b4 a0b4                      =
    //   ----------------------------------------------
    //      r8   r7   r6   r5   r4   r3   r2   r1   r0
    //
    // We can then use the reduction identity (a * 2²⁵⁵ + b = a * 19 + b) to
    // reduce the limbs that would overflow 255 bits. r5 * 2²⁵⁵ becomes 19 * r5,
    // r6 * 2³⁰⁶ becomes 19 * r6 * 2⁵¹, etc.
    //
    // Reduction can be carried out simultaneously to multiplication. For
    // example, we do not compute r5: whenever the result of a multiplication
    // belongs to r5, like a1b4, we multiply it by 19 and add the result to r0.
    //
    //            a4b0    a3b0    a2b0    a1b0    a0b0  +
    //            a3b1    a2b1    a1b1    a0b1 19×a4b1  +
    //            a2b2    a1b2    a0b2 19×a4b2 19×a3b2  +
    //            a1b3    a0b3 19×a4b3 19×a3b3 19×a2b3  +
    //            a0b4 19×a4b4 19×a3b4 19×a2b4 19×a1b4  =
    //           --------------------------------------
    //              r4      r3      r2      r1      r0
    //
    // Finally we add up the columns into wide, overlapping limbs.
    operator fun times(b: FieldElement): FieldElement {
        val a = this
        val a0 = a[0]
        val a1 = a[1]
        val a2 = a[2]
        val a3 = a[3]
        val a4 = a[4]

        val b0 = b[0]
        val b1 = b[1]
        val b2 = b[2]
        val b3 = b[3]
        val b4 = b[4]

        val a1_19 = a1 * 19u
        val a2_19 = a2 * 19u
        val a3_19 = a3 * 19u
        val a4_19 = a4 * 19u

        // r0 = a0×b0 + 19×(a1×b4 + a2×b3 + a3×b2 + a4×b1)
        var r0 = mul64(a0, b0)
        r0 = addMul64(r0, a1_19, b4)
        r0 = addMul64(r0, a2_19, b3)
        r0 = addMul64(r0, a3_19, b2)
        r0 = addMul64(r0, a4_19, b1)

        // r1 = a0×b1 + a1×b0 + 19×(a2×b4 + a3×b3 + a4×b2)
        var r1 = mul64(a0, b1)
        r1 = addMul64(r1, a1, b0)
        r1 = addMul64(r1, a2_19, b4)
        r1 = addMul64(r1, a3_19, b3)
        r1 = addMul64(r1, a4_19, b2)

        // r2 = a0×b2 + a1×b1 + a2×b0 + 19×(a3×b4 + a4×b3)
        var r2 = mul64(a0, b2)
        r2 = addMul64(r2, a1, b1)
        r2 = addMul64(r2, a2, b0)
        r2 = addMul64(r2, a3_19, b4)
        r2 = addMul64(r2, a4_19, b3)

        // r3 = a0×b3 + a1×b2 + a2×b1 + a3×b0 + 19×a4×b4
        var r3 = mul64(a0, b3)
        r3 = addMul64(r3, a1, b2)
        r3 = addMul64(r3, a2, b1)
        r3 = addMul64(r3, a3, b0)
        r3 = addMul64(r3, a4_19, b4)

        // r4 = a0×b4 + a1×b3 + a2×b2 + a3×b1 + a4×b0
        var r4 = mul64(a0, b4)
        r4 = addMul64(r4, a1, b3)
        r4 = addMul64(r4, a2, b2)
        r4 = addMul64(r4, a3, b1)
        r4 = addMul64(r4, a4, b0)

        // After the multiplication, we need to reduce (carry) the five coefficients
        // to obtain a result with limbs that are at most slightly larger than 2⁵¹,
        // to respect the Element invariant.
        //
        // Overall, the reduction works the same as carryPropagate, except with
        // wider inputs: we take the carry for each coefficient by shifting it right
        // by 51, and add it to the limb above it. The top carry is multiplied by 19
        // according to the reduction identity and added to the lowest limb.
        //
        // The largest coefficient (r0) will be at most 111 bits, which guarantees
        // that all carries are at most 111 - 51 = 60 bits, which fits in a uint64.
        //
        //     r0 = a0×b0 + 19×(a1×b4 + a2×b3 + a3×b2 + a4×b1)
        //     r0 < 2⁵²×2⁵² + 19×(2⁵²×2⁵² + 2⁵²×2⁵² + 2⁵²×2⁵² + 2⁵²×2⁵²)
        //     r0 < (1 + 19 × 4) × 2⁵² × 2⁵²
        //     r0 < 2⁷ × 2⁵² × 2⁵²
        //     r0 < 2¹¹¹
        //
        // Moreover, the top coefficient (r4) is at most 107 bits, so c4 is at most
        // 56 bits, and c4 * 19 is at most 61 bits, which again fits in a uint64 and
        // allows us to easily apply the reduction identity.
        //
        //     r4 = a0×b4 + a1×b3 + a2×b2 + a3×b1 + a4×b0
        //     r4 < 5 × 2⁵² × 2⁵²
        //     r4 < 2¹⁰⁷
        //

        val c0 = shiftRightBy51(r0)
        val c1 = shiftRightBy51(r1)
        val c2 = shiftRightBy51(r2)
        val c3 = shiftRightBy51(r3)
        val c4 = shiftRightBy51(r4)

        val v = ulongArrayOf(
            (r0.lo and low_51_bit_mask) + c4 * 19u,
            (r1.lo and low_51_bit_mask) + c0,
            (r2.lo and low_51_bit_mask) + c1,
            (r3.lo and low_51_bit_mask) + c2,
            (r4.lo and low_51_bit_mask) + c3,
        )
        // Now all coefficients fit into 64-bit registers but are still too large to
        // be passed around as a Element. We therefore do one last carry chain,
        // where the carries will be small enough to fit in the wiggle room above 2⁵¹.
        reduce(v)
        return FieldElement(v)
    }

    // Squaring works precisely like multiplication above, but thanks to its
    // symmetry we get to group a few terms together.
    //
    //                          l4   l3   l2   l1   l0  x
    //                          l4   l3   l2   l1   l0  =
    //                         ------------------------
    //                        l4l0 l3l0 l2l0 l1l0 l0l0  +
    //                   l4l1 l3l1 l2l1 l1l1 l0l1       +
    //              l4l2 l3l2 l2l2 l1l2 l0l2            +
    //         l4l3 l3l3 l2l3 l1l3 l0l3                 +
    //    l4l4 l3l4 l2l4 l1l4 l0l4                      =
    //   ----------------------------------------------
    //      r8   r7   r6   r5   r4   r3   r2   r1   r0
    //
    //            l4l0    l3l0    l2l0    l1l0    l0l0  +
    //            l3l1    l2l1    l1l1    l0l1 19×l4l1  +
    //            l2l2    l1l2    l0l2 19×l4l2 19×l3l2  +
    //            l1l3    l0l3 19×l4l3 19×l3l3 19×l2l3  +
    //            l0l4 19×l4l4 19×l3l4 19×l2l4 19×l1l4  =
    //           --------------------------------------
    //              r4      r3      r2      r1      r0
    //
    // With precomputed 2×, 19×, and 2×19× terms, we can compute each limb with
    // only three Mul64 and four Add64, instead of five and eight.
    fun square(): FieldElement {
        val a = this

        val l0 = a[0]
        val l1 = a[1]
        val l2 = a[2]
        val l3 = a[3]
        val l4 = a[4]

        val l0_2 = l0 * 2u
        val l1_2 = l1 * 2u

        val l1_38 = l1 * 38u
        val l2_38 = l2 * 38u
        val l3_38 = l3 * 38u

        val l3_19 = l3 * 19u
        val l4_19 = l4 * 19u

        // r0 = l0×l0 + 19×(l1×l4 + l2×l3 + l3×l2 + l4×l1) = l0×l0 + 19×2×(l1×l4 + l2×l3)
        var r0 = mul64(l0, l0)
        r0 = addMul64(r0, l1_38, l4)
        r0 = addMul64(r0, l2_38, l3)

        // r1 = l0×l1 + l1×l0 + 19×(l2×l4 + l3×l3 + l4×l2) = 2×l0×l1 + 19×2×l2×l4 + 19×l3×l3
        var r1 = mul64(l0_2, l1)
        r1 = addMul64(r1, l2_38, l4)
        r1 = addMul64(r1, l3_19, l3)

        // r2 = l0×l2 + l1×l1 + l2×l0 + 19×(l3×l4 + l4×l3) = 2×l0×l2 + l1×l1 + 19×2×l3×l4
        var r2 = mul64(l0_2, l2)
        r2 = addMul64(r2, l1, l1)
        r2 = addMul64(r2, l3_38, l4)

        // r3 = l0×l3 + l1×l2 + l2×l1 + l3×l0 + 19×l4×l4 = 2×l0×l3 + 2×l1×l2 + 19×l4×l4
        var r3 = mul64(l0_2, l3)
        r3 = addMul64(r3, l1_2, l2)
        r3 = addMul64(r3, l4_19, l4)

        // r4 = l0×l4 + l1×l3 + l2×l2 + l3×l1 + l4×l0 = 2×l0×l4 + 2×l1×l3 + l2×l2
        var r4 = mul64(l0_2, l4)
        r4 = addMul64(r4, l1_2, l3)
        r4 = addMul64(r4, l2, l2)

        val c0 = shiftRightBy51(r0)
        val c1 = shiftRightBy51(r1)
        val c2 = shiftRightBy51(r2)
        val c3 = shiftRightBy51(r3)
        val c4 = shiftRightBy51(r4)

        val v = ULongArray(5)
        v[0] = (r0.lo and low_51_bit_mask) + c4 * 19u
        v[1] = (r1.lo and low_51_bit_mask) + c0
        v[2] = (r2.lo and low_51_bit_mask) + c1
        v[3] = (r3.lo and low_51_bit_mask) + c2
        v[4] = (r4.lo and low_51_bit_mask) + c3
        reduce(v)

        return FieldElement(v)
    }

    companion object {
        val low_51_bit_mask = (1uL shl 51) - 1u
        val mask32 = (1uL shl 32) - 1u

        fun zero() = FieldElement(ulongArrayOf(0u, 0u, 0u, 0u, 0u))
        fun one() = FieldElement(ulongArrayOf(1u, 0u, 0u, 0u, 0u))

        fun sqrtRatio(u: FieldElement, v: FieldElement): Pair<FieldElement, Boolean> {
            TODO()
        }

        fun fromBytes(byteArray: ByteArray) = FieldElement(
            ulongArrayOf(
                load8(byteArray, 0) and low_51_bit_mask,
                (load8(byteArray, 6) shr 3) and low_51_bit_mask,
                (load8(byteArray, 12) shr 6) and low_51_bit_mask,
                (load8(byteArray, 19) shr 1) and low_51_bit_mask,
                (load8(byteArray, 24) shr 12) and low_51_bit_mask,
            )
        )

        private fun load8(byteArray: ByteArray, offset: Int): ULong {
            return (byteArray[offset].toULong() and 0xFFu) or
                    ((byteArray[offset + 1].toULong() and 0xFFu) shl 8) or
                    ((byteArray[offset + 2].toULong() and 0xFFu) shl 16) or
                    ((byteArray[offset + 3].toULong() and 0xFFu) shl 24) or
                    ((byteArray[offset + 4].toULong() and 0xFFu) shl 32) or
                    ((byteArray[offset + 5].toULong() and 0xFFu) shl 40) or
                    ((byteArray[offset + 6].toULong() and 0xFFu) shl 48) or
                    ((byteArray[offset + 7].toULong() and 0xFFu) shl 56)
        }

        private fun mul64(x: ULong, y: ULong): UBigInt {
            val x0 = x and mask32
            val x1 = x shr 32
            val y0 = y and mask32
            val y1 = y shr 32
            val w0 = x0 * y0
            val t = x1 * y0 + (w0 shr 32)
            var w1 = t and mask32
            val w2 = t shr 32
            w1 += x0 * y1
            return ulongArrayOf(
                x * y,
                x1 * y1 + w2 + (w1 shr 32)
            )
        }

        // Add64 returns the sum with carry of x, y and carry: sum = x + y + carry.
        // The carry input must be 0 or 1; otherwise the behavior is undefined.
        // The carryOut output is guaranteed to be 0 or 1.
        //
        // This function's execution time does not depend on the inputs.
        private fun add64(x: ULong, y: ULong, carry: ULong): ULongArray {
            val sum = x + y + carry
            // The sum will overflow if both top bits are set (x & y) or if one of them
            // is (x | y), and a carry from the lower place happened. If such a carry
            // happens, the top bit will be 1 + 0 + 1 = 0 (&^ sum).
            val carryOut = ((x and y) or ((x or y) and sum.inv())) shr 63
            return ulongArrayOf(sum, carryOut)
        }

        private fun addMul64(v: UBigInt, a: ULong, b: ULong): UBigInt {
            val (lo1, hi1) = mul64(a, b)
            val (lo2, c) = add64(lo1, v.lo, 0u)
            val (hi2, _) = add64(hi1, v.hi, c)
            return ulongArrayOf(lo2, hi2)
        }

        private fun shiftRightBy51(a: UBigInt): ULong {
            return (a.hi shl (64 - 51)) or (a.lo shr 51)
        }

        // Since the input limbs are bounded by 2^64, the biggest
        // carry-out is bounded by 2^13.
        //
        // The biggest carry-in is c4 * 19, resulting in
        //
        // 2^51 + 19*2^13 < 2^51.0000000001
        //
        // Because we don't need to canonicalize, only to reduce the
        // limb sizes, it's OK to do a "weak reduction", where we
        // compute the carry-outs in parallel.
        private fun reduce(a: ULongArray) {
            val c0 = a[0] shr 51
            val c1 = a[1] shr 51
            val c2 = a[2] shr 51
            val c3 = a[3] shr 51
            val c4 = a[4] shr 51

            // c4 is at most 64 - 51 = 13 bits, so c4*19 is at most 18 bits, and
            // the final l0 will be at most 52 bits. Similarly for the rest.
            a[0] = (a[0] and low_51_bit_mask) + c4 * 19u
            a[1] = (a[1] and low_51_bit_mask) + c0
            a[2] = (a[2] and low_51_bit_mask) + c1
            a[3] = (a[3] and low_51_bit_mask) + c2
            a[4] = (a[4] and low_51_bit_mask) + c3
        }
    }
}

internal typealias UBigInt = ULongArray

val UBigInt.lo get() = this[0]
val UBigInt.hi get() = this[1]