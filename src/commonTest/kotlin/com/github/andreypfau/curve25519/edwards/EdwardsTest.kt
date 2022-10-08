package com.github.andreypfau.curve25519.edwards

import com.github.andreypfau.curve25519.constants.BASEPOINT_ORDER
import com.github.andreypfau.curve25519.constants.ED25519_BASEPOINT_COMPRESSED
import com.github.andreypfau.curve25519.constants.ED25519_BASEPOINT_POINT
import com.github.andreypfau.curve25519.scalar.Scalar
import com.github.andreypfau.curve25519.utils.byteArrayOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EdwardsTest {
    // 4493907448824000747700850167940867464579944529806937181821189941592931634714
    val A_SCALAR = Scalar(
        byteArrayOf(
            0x1a, 0x0e, 0x97, 0x8a, 0x90, 0xf6, 0x62, 0x2d,
            0x37, 0x47, 0x02, 0x3f, 0x8a, 0xd8, 0x26, 0x4d,
            0xa7, 0x58, 0xaa, 0x1b, 0x88, 0xe0, 0x40, 0xd1,
            0x58, 0x9e, 0x7b, 0x7f, 0x23, 0x76, 0xef, 0x09,
        )
    )

    // A_SCALAR * basepoint, computed with ed25519.py
    val A_TIMES_BASEPOINT = CompressedEdwardsY(
        byteArrayOf(
            0xea, 0x27, 0xe2, 0x60, 0x53, 0xdf, 0x1b, 0x59,
            0x56, 0xf1, 0x4d, 0x5d, 0xec, 0x3c, 0x34, 0xc3,
            0x84, 0xa2, 0x69, 0xb7, 0x4c, 0xc3, 0x80, 0x3e,
            0xa8, 0xe2, 0xe7, 0xc9, 0x42, 0x5e, 0x40, 0xa5
        )
    )

    @Test
    fun basepointMultOneVsBasepoint() {
        val bp = EdwardsBasepointTable * Scalar.ONE
        val compressed = bp.compress()
        assertEquals(ED25519_BASEPOINT_COMPRESSED, compressed)
    }

    @Test
    fun toAffineNielsClearsDenominators() {
        val aB = EdwardsBasepointTable * A_SCALAR
        val aBAffineNiles = aB.toAffineNiels()
        val alsoAb = (EdwardsPoint() + aBAffineNiles).toExtended()
        assertEquals(aB.compress(), alsoAb.compress())
    }

    @Test
    fun basepointMultVsEd25519py() {
        val aB = EdwardsBasepointTable * A_SCALAR
        assertEquals(A_TIMES_BASEPOINT, aB.compress())
    }

    @Test
    fun basepointMultByBasepointOrder() {
        val b = EdwardsBasepointTable
        val shouldBeId = b * BASEPOINT_ORDER
        assertTrue(shouldBeId.isIdentity())
    }

    @Test
    fun testPrecomputedBasepointMult() {
        val aB1 = EdwardsBasepointTable * A_SCALAR
        val aB2 = ED25519_BASEPOINT_POINT * A_SCALAR
        assertEquals(aB1.compress(), aB2.compress())
    }
}
