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

    operator fun unaryMinus(): FieldElement = zero() - this

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

    fun invert(): FieldElement {
        val z = this
        val z2 = z.square() // 2
        var t = z2.square() // 4
        t = t.square() // 8
        val z9 = t * z // 9
        val z11 = z9 * z2 // 11
        t = z11.square() // 22
        val z2_5_0 = t * z9 // 32 = 2^5 - 2^0

        t = z2_5_0.square() // 2^6 - 2^1
        repeat(4) {
            t = t.square() // 2^10 - 2^5
        }
        val z2_10_0 = t * z2_5_0 // 2^10 - 2^0

        t = z2_10_0.square() // 2^11 - 2^1
        repeat(9) {
            t = t.square() // 2^20 - 2^10
        }
        val z2_20_0 = t * z2_10_0 // 2^20 - 2^0

        t = z2_20_0.square() // 2^21 - 2^1
        repeat(19) {
            t = t.square() // 2^40 - 2^20
        }
        t = t * z2_20_0 // 2^40 - 2^0

        t = t.square() // 2^41 - 2^1
        repeat(9) {
            t = t.square() // 2^50 - 2^10
        }
        val z2_50_0 = t * z2_10_0 // 2^50 - 2^0

        t = z2_50_0.square() // 2^51 - 2^1
        repeat(49) {
            t = t.square() // 2^10 - 2^50
        }
        val z2_100_0 = t * z2_50_0 // 2^100 - 2^0

        t = z2_100_0.square() // 2^101 - 2^1
        repeat(99) {
            t = t.square() // 2^200 - 2^100
        }
        t = t * z2_100_0 // 2^200 - 2^0

        t = t.square() // 2^201 - 2^1
        repeat(49) {
            t = t.square() // 2^250 - 2^50
        }
        t = t * z2_50_0 // 2^250 - 2^0

        t = t.square() // 2^251 - 2^1
        t = t.square() // 2^252 - 2^2
        t = t.square() // 2^253 - 2^3
        t = t.square() // 2^254 - 2^4
        t = t.square() // 2^255 - 2^5
        return t * z11
    }

    /**
     * Raise this field element to the power (p-5)/8 = 2^252 -3.
     */
    fun pow25523(): FieldElement {
        val x = this
        var t0 = x.square() // x^2
        var t1 = t0.square() // x^4
        t1 = t1.square() // x^8
        t1 = x * t1 // x^9
        t0 = t0 * t1 // x^11
        t0 = t0.square() // x^22
        t0 = t1 * t0 // x^31
        t1 = t0.square() // x^62
        t1 = t1.square() // x^124
        t1 = t1.square() // x^248
        t1 = t1.square() // x^496
        t1 = t1.square() // x^992
        t0 = t1 * t0 // x^1023 -> 1023 = 2^10 - 1
        t1 = t0.square() // 2^11 - 2
        repeat(9) { // 2^20 - 2^10
            t1 = t1.square()
        }
        t1 = t1 * t0 // 2^20 - 1
        var t2 = t1.square() // // 2^21 - 2
        repeat(19) { // 2^40 - 2^20
            t2 = t2.square()
        }
        t1 = t2 * t1 // 2^40 - 1
        t1 = t1.square() // 2^41 - 2
        repeat(9) { // 2^50 - 2^10
            t1 = t1.square()
        }
        t0 = t1 * t0 // 2^50 - 1
        t1 = t0.square() // 2^51 - 2
        repeat(49) { // 2^100 - 2^50
            t1 = t1.square()
        }
        t1 = t1 * t0 // 2^100 - 1
        t2 = t1.square() // 2^101 - 2
        repeat(99) { // 2^200 - 2^100
            t2 = t2.square()
        }
        t1 = t2 * t1 // 2^200 - 1
        t1 = t1.square() // 2^201 - 2
        repeat(49) { // 2^250 - 2^50
            t1 = t1.square()
        }
        t0 = t1 * t0 // 2^250 - 1
        t0 = t0.square() // 2^251 - 2
        t0 = t0.square() // 2^252 - 4
        return t0 * x // 2^252 - 3 -> x^(2^252-3)
    }

    fun toBytes(): ByteArray {
        // First, reduce the limbs to ensure h < 2*p.
        val limbs = reduce(data)

        var q = (limbs[0] + 19u) shr 51
        q = (limbs[1] + q) shr 51
        q = (limbs[2] + q) shr 51
        q = (limbs[3] + q) shr 51
        q = (limbs[4] + q) shr 51

        // Now we can compute r as r = h - pq = r - (2^255-19)q = r + 19q - 2^255q
        limbs[0] += q * 19u

        // Now carry the result to compute r + 19q ...
        limbs[1] += limbs[0] shr 51
        limbs[0] = limbs[0] and low_51_bit_mask
        limbs[2] += limbs[1] shr 51
        limbs[1] = limbs[1] and low_51_bit_mask
        limbs[3] += limbs[2] shr 51
        limbs[2] = limbs[2] and low_51_bit_mask
        limbs[4] += limbs[3] shr 51
        limbs[3] = limbs[3] and low_51_bit_mask
        // ... but instead of carrying (limbs[4] >> 51) = 2^255q
        // into another limb, discard it, subtracting the value
        limbs[4] = limbs[4] and low_51_bit_mask

        // Now arrange the bits of the limbs.
        val s = ByteArray(32)
        s[0] = limbs[0].toByte()
        s[1] = (limbs[0] shr 8).toByte()
        s[2] = (limbs[0] shr 16).toByte()
        s[3] = (limbs[0] shr 24).toByte()
        s[4] = (limbs[0] shr 32).toByte()
        s[5] = (limbs[0] shr 40).toByte()
        s[6] = ((limbs[0] shr 48) or (limbs[1] shl 3)).toByte()
        s[7] = (limbs[1] shr 5).toByte()
        s[8] = (limbs[1] shr 13).toByte()
        s[9] = (limbs[1] shr 21).toByte()
        s[10] = (limbs[1] shr 29).toByte()
        s[11] = (limbs[1] shr 37).toByte()
        s[12] = ((limbs[1] shr 45) or (limbs[2] shl 6)).toByte()
        s[13] = (limbs[2] shr 2).toByte()
        s[14] = (limbs[2] shr 10).toByte()
        s[15] = (limbs[2] shr 18).toByte()
        s[16] = (limbs[2] shr 26).toByte()
        s[17] = (limbs[2] shr 34).toByte()
        s[18] = (limbs[2] shr 42).toByte()
        s[19] = ((limbs[2] shr 50) or (limbs[3] shl 1)).toByte()
        s[20] = (limbs[3] shr 7).toByte()
        s[21] = (limbs[3] shr 15).toByte()
        s[22] = (limbs[3] shr 23).toByte()
        s[23] = (limbs[3] shr 31).toByte()
        s[24] = (limbs[3] shr 39).toByte()
        s[25] = ((limbs[3] shr 47) or (limbs[4] shl 4)).toByte()
        s[26] = (limbs[4] shr 4).toByte()
        s[27] = (limbs[4] shr 12).toByte()
        s[28] = (limbs[4] shr 20).toByte()
        s[29] = (limbs[4] shr 28).toByte()
        s[30] = (limbs[4] shr 36).toByte()
        s[31] = (limbs[4] shr 44).toByte()

        return s
    }

    fun isNegative() = (toBytes()[0].toInt() and 1) != 0

    fun absolute() = select(-this, isNegative())

    fun select(a: FieldElement, condition: Boolean): FieldElement {
        val b = this
        val m = if (condition) 0xFFFFFFFFFFFFFFFFu else 0uL

        val ma0 = (m and a[0])
        val mb0 = (m.inv() and b[0])

        val v = ulongArrayOf(
            ma0 or mb0,
            (m and a[1]) or (m.inv() and b[1]),
            (m and a[2]) or (m.inv() and b[2]),
            (m and a[3]) or (m.inv() and b[3]),
            (m and a[4]) or (m.inv() and b[4])
        )
        return FieldElement(v)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FieldElement

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode() = data.contentHashCode()

    override fun toString() = "FieldElement(${data.joinToString()})"

    companion object {
        val low_51_bit_mask = (1uL shl 51) - 1u
        val mask32 = (1uL shl 32) - 1u

        // 2^((p-1)/4), which squared is equal to -1 by Euler's Criterion.
        val SQRT_M1 = FieldElement(
            ulongArrayOf(
                1718705420411056u, 234908883556509u, 2233514472574048u, 2117202627021982u, 765476049583133u
            )
        )

        /**
         * Construct zero.
         */
        fun zero() = FieldElement(ulongArrayOf(0u, 0u, 0u, 0u, 0u))

        /**
         * Construct one.
         */
        fun one() = FieldElement(ulongArrayOf(1u, 0u, 0u, 0u, 0u))

        /**
         * Construct -1.
         */
        fun minusOne() = FieldElement(
            ulongArrayOf(
                2251799813685228u,
                2251799813685247u,
                2251799813685247u,
                2251799813685247u,
                2251799813685247u
            )
        )

        fun sqrtRatio(u: FieldElement, v: FieldElement): Pair<FieldElement, Boolean> {
            // Using the same trick as in ed25519 decoding, we merge the
            // inversion, the square root, and the square test as follows.
            //
            // To compute sqrt(α), we can compute β = α^((p+3)/8).
            // Then β^2 = ±α, so multiplying β by sqrt(-1) if necessary
            // gives sqrt(α).
            //
            // To compute 1/sqrt(α), we observe that
            //    1/β = α^(p-1 - (p+3)/8) = α^((7p-11)/8)
            //                            = α^3 * (α^7)^((p-5)/8).
            //
            // We can therefore compute sqrt(u/v) = sqrt(u)/sqrt(v)
            // by first computing
            //    r = u^((p+3)/8) v^(p-1-(p+3)/8)
            //      = u u^((p-5)/8) v^3 (v^7)^((p-5)/8)
            //      = (u * v^3) (u * v^7)^((p-5)/8).
            //
            // If v is nonzero and u/v is square, then r^2 = ±u/v,
            //                                     so vr^2 = ±u.
            // If vr^2 =  u, then sqrt(u/v) = r.
            // If vr^2 = -u, then sqrt(u/v) = r*sqrt(-1).
            //
            // If v is zero, r is also zero.

            // r = (u * v^3) (u * v^7)^((p-5)/8)
            val v3 = v.square() * v
            val v7 = v3.square() * v
            var r = (u * v3) * (u * v7).pow25523()

            val check = v * r.square()

            val uNeg = -u
            val correctSignSqrt = check == u
            val flippedSignSqrt = check == uNeg
            val flippedSignSqrtI = check == (uNeg * SQRT_M1)

            val rPrime = r * SQRT_M1
            r = r.select(rPrime, flippedSignSqrt || flippedSignSqrtI)
            r = r.absolute()

            return r to (correctSignSqrt || flippedSignSqrt)
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
        private fun reduce(a: ULongArray): ULongArray {
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

            return a
        }
    }
}

