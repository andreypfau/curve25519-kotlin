@file:Suppress("OPT_IN_USAGE", "LocalVariableName", "NAME_SHADOWING")

package com.github.andreypfau.curve25519.internal

import com.github.andreypfau.curve25519.constants.L
import com.github.andreypfau.curve25519.constants.LFACTOR
import com.github.andreypfau.curve25519.constants.LOW_52_BIT_NASK

private inline val ULongArray.hi get() = get(0)
private inline val ULongArray.lo get() = get(1)

internal fun scalarMulInternal(a: ULongArray, b: ULongArray, output: ULongArray = ULongArray(18)): ULongArray =
    output.apply {
        val (z1, z0) = mul64(a[0], b[0])
        val (z3, z2) = add128(
            mul64(a[0], b[1]),
            mul64(a[1], b[0])
        )
        val (z5, z4) = add128(
            mul64(a[0], b[2]),
            add128(mul64(a[1], b[1]), mul64(a[2], b[0]))
        )
        val (z7, z6) = add128(
            add128(mul64(a[0], b[3]), mul64(a[1], b[2])),
            add128(mul64(a[2], b[1]), mul64(a[3], b[0]))
        )
        val (z9, z8) = add128(
            mul64(a[0], b[4]),
            add128(add128(mul64(a[1], b[3]), mul64(a[2], b[2])), add128(mul64(a[3], b[1]), mul64(a[4], b[0])))
        )
        val (z11, z10) = add128(
            add128(mul64(a[1], b[4]), mul64(a[2], b[3])),
            add128(mul64(a[3], b[2]), mul64(a[4], b[1]))
        )
        val (z13, z12) = add128(
            mul64(a[2], b[4]),
            add128(mul64(a[3], b[3]), mul64(a[4], b[2]))
        )
        val (z15, z14) = add128(
            mul64(a[3], b[4]),
            mul64(a[4], b[3])
        )
        val (z17, z16) = mul64(a[4], b[4])

        output[0] = z0; output[1] = z1
        output[2] = z2; output[3] = z3
        output[4] = z4; output[5] = z5
        output[6] = z6; output[7] = z7
        output[8] = z8; output[9] = z9
        output[10] = z10; output[11] = z11
        output[12] = z12; output[13] = z13
        output[14] = z14; output[15] = z15
        output[16] = z16; output[17] = z17
    }

internal fun scalarMontgomeryReduce(limbs: ULongArray, output: ULongArray = ULongArray(5)): ULongArray = output.apply {
    // note: l[3] is zero, so its multiples can be skipped
    // the first half computes the Montgomery adjustment factor n, and begins adding n*l to make limbs divisible by R
    val n0 = part1(limbs[1], limbs[0])
    val n1 = part1(
        add128(
            add128(n0, ulongArrayOf(limbs[3], limbs[2])),
            mul64(n0[2], L[1])
        )
    )
    val n2 = part1(
        add128(
            add128(n1, ulongArrayOf(limbs[5], limbs[4])),
            add128(mul64(n0[2], L[2]), mul64(n1[2], L[1]))
        )
    )
    val n3 = part1(
        add128(
            add128(n2, ulongArrayOf(limbs[7], limbs[6])),
            add128(mul64(n1[2], L[2]), mul64(n2[2], L[1]))
        )
    )
    val n4 = part1(
        add128(
            add128(n3, ulongArrayOf(limbs[9], limbs[8])),
            add128(mul64(n0[2], L[4]), add128(mul64(n2[2], L[2]), mul64(n3[2], L[1])))
        )
    )

    // limbs are divisible by R now, so we can divide by R by simply storing the upper half as the result

    val r0 = part2(
        add128(
            n4,
            add128(
                ulongArrayOf(limbs[11], limbs[10]),
                add128(mul64(n1[2], L[4]), add128(mul64(n3[2], L[2]), mul64(n4[2], L[1])))
            )
        )
    )
    val r1 = part2(
        add128(
            r0,
            add128(ulongArrayOf(limbs[13], limbs[12]), add128(mul64(n2[2], L[4]), mul64(n4[2], L[2])))
        )
    )
    val r2 = part2(
        add128(
            r1,
            add128(ulongArrayOf(limbs[15], limbs[14]), mul64(n3[2], L[4]))
        )
    )
    val (_, r4, r3) = part2(
        add128(
            r2,
            add128(ulongArrayOf(limbs[17], limbs[16]), mul64(n4[2], L[4]))
        )
    )
    output[0] = r0[2]
    output[1] = r1[2]
    output[2] = r2[2]
    output[3] = r3
    output[4] = r4
}

internal fun part1(a: ULong, b: ULong) = part1(ulongArrayOf(a, b))
internal fun part1(sum: ULongArray): ULongArray {
    val p = sum[1] * LFACTOR and LOW_52_BIT_NASK
    val t = mul64(p, L.data[0])
    var (hi, lo) = add128(sum, t)
    lo = (hi shl (64 - 52)) or (lo shr 52)
    hi = hi shr 52
    return ulongArrayOf(hi, lo, p)
}

internal fun part2(sum: ULongArray): ULongArray {
    var (hi, lo) = sum
    val w = lo and LOW_52_BIT_NASK
    lo = (hi shl (64 - 52)) or (lo shr 52)
    hi = hi shr 52
    return ulongArrayOf(hi, lo, w)
}
