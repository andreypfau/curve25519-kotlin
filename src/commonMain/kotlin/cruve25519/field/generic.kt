@file:Suppress("OPT_IN_USAGE")

package cruve25519.field

import math.bits.*

fun ULong.mul64(b: ULong): UBigInt {
    val (hi, lo) = mul64(this, b)
    return lo to hi
}

/**
 * @return `a >> 51`. `a` is assumed to be at most 115 bits.
 */
internal fun shiftRight51(a: UBigInt): ULong = (a.hi shl (64 - 51)) or (a.lo shr 51)

/**
 *  Limb multiplication works like pen-and-paper columnar multiplication, but
 *  with 51-bit limbs instead of digits.
 *
 *                           a4   a3   a2   a1   a0  x
 *                           b4   b3   b2   b1   b0  =
 *                          ------------------------
 *                         a4b0 a3b0 a2b0 a1b0 a0b0  +
 *                    a4b1 a3b1 a2b1 a1b1 a0b1       +
 *               a4b2 a3b2 a2b2 a1b2 a0b2            +
 *          a4b3 a3b3 a2b3 a1b3 a0b3                 +
 *     a4b4 a3b4 a2b4 a1b4 a0b4                      =
 *    ----------------------------------------------
 *       r8   r7   r6   r5   r4   r3   r2   r1   r0
 *
 *  We can then use the reduction identity (a * 2²⁵⁵ + b = a * 19 + b) to
 *  reduce the limbs that would overflow 255 bits. r5 * 2²⁵⁵ becomes 19 * r5,
 *  r6 * 2³⁰⁶ becomes 19 * r6 * 2⁵¹, etc.
 *
 *  Reduction can be carried out simultaneously to multiplication. For
 *  example, we do not compute r5: whenever the result of a multiplication
 *  belongs to r5, like a1b4, we multiply it by 19 and add the result to r0.
 *
 *             a4b0    a3b0    a2b0    a1b0    a0b0  +
 *             a3b1    a2b1    a1b1    a0b1 19×a4b1  +
 *             a2b2    a1b2    a0b2 19×a4b2 19×a3b2  +
 *             a1b3    a0b3 19×a4b3 19×a3b3 19×a2b3  +
 *             a0b4 19×a4b4 19×a3b4 19×a2b4 19×a1b4  =
 *            --------------------------------------
 *               r4      r3      r2      r1      r0
 *
 *  Finally we add up the columns into wide, overlapping limbs.
 */
internal fun feMul(v: FieldElement, a: FieldElement, b: FieldElement) {
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
    r1 = addMul64(r1, a4_19, b4)

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

    val c0 = shiftRight51(r0)
    val c1 = shiftRight51(r1)
    val c2 = shiftRight51(r2)
    val c3 = shiftRight51(r3)
    val c4 = shiftRight51(r4)

    // Now all coefficients fit into 64-bit registers but are still too large to
    // be passed around as a Element. We therefore do one last carry chain,
    // where the carries will be small enough to fit in the wiggle room above 2⁵¹.
    v[0] = (r0.lo and MASK_LOW_51_BITS) + c4 * 19u
    v[1] = (r1.lo and MASK_LOW_51_BITS) + c0
    v[2] = (r2.lo and MASK_LOW_51_BITS) + c1
    v[3] = (r3.lo and MASK_LOW_51_BITS) + c2
    v[4] = (r4.lo and MASK_LOW_51_BITS) + c3

    v.carryPropagate()
}

/**
 * Squaring works precisely like multiplication above, but thanks to its
 * symmetry we get to group a few terms together.
 *
 *                           l4   l3   l2   l1   l0  x
 *                           l4   l3   l2   l1   l0  =
 *                          ------------------------
 *                         l4l0 l3l0 l2l0 l1l0 l0l0  +
 *                    l4l1 l3l1 l2l1 l1l1 l0l1       +
 *               l4l2 l3l2 l2l2 l1l2 l0l2            +
 *          l4l3 l3l3 l2l3 l1l3 l0l3                 +
 *     l4l4 l3l4 l2l4 l1l4 l0l4                      =
 *    ----------------------------------------------
 *       r8   r7   r6   r5   r4   r3   r2   r1   r0
 *
 *             l4l0    l3l0    l2l0    l1l0    l0l0  +
 *             l3l1    l2l1    l1l1    l0l1 19×l4l1  +
 *             l2l2    l1l2    l0l2 19×l4l2 19×l3l2  +
 *             l1l3    l0l3 19×l4l3 19×l3l3 19×l2l3  +
 *             l0l4 19×l4l4 19×l3l4 19×l2l4 19×l1l4  =
 *            --------------------------------------
 *               r4      r3      r2      r1      r0
 *
 *  With precomputed 2×, 19×, and 2×19× terms, we can compute each limb with
 *  only three [mul64] and four [add64], instead of five and eight.
 */
internal fun feSquare(v: FieldElement, a: FieldElement) {
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

    val c0 = shiftRight51(r0)
    val c1 = shiftRight51(r1)
    val c2 = shiftRight51(r2)
    val c3 = shiftRight51(r3)
    val c4 = shiftRight51(r4)

    v[0] = (r0.lo and MASK_LOW_51_BITS) + c4 * 19u
    v[1] = (r1.lo and MASK_LOW_51_BITS) + c0
    v[2] = (r2.lo and MASK_LOW_51_BITS) + c1
    v[3] = (r3.lo and MASK_LOW_51_BITS) + c2
    v[4] = (r4.lo and MASK_LOW_51_BITS) + c3
    v.carryPropagate()
}

/**
 * @ return the limbs below 52 bits by applying the reduction
 * identity (a * 2²⁵⁵ + b = a * 19 + b) to the l4 carry.
 */
internal fun FieldElement.carryPropagate() = apply {
    val c0 = this[0] shr 51
    val c1 = this[1] shr 51
    val c2 = this[2] shr 51
    val c3 = this[3] shr 51
    val c4 = this[4] shr 51

    // c4 is at most 64 - 51 = 13 bits, so c4*19 is at most 18 bits, and
    // the final l0 will be at most 52 bits. Similarly for the rest.
    this[0] = (this[0] and MASK_LOW_51_BITS) + c4 * 19u
    this[1] = (this[1] and MASK_LOW_51_BITS) + c0
    this[2] = (this[2] and MASK_LOW_51_BITS) + c1
    this[3] = (this[3] and MASK_LOW_51_BITS) + c2
    this[4] = (this[4] and MASK_LOW_51_BITS) + c3
}