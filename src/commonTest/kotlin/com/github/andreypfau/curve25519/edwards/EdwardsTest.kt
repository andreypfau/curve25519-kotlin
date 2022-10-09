@file:OptIn(ExperimentalContracts::class)

package com.github.andreypfau.curve25519.edwards

import com.github.andreypfau.curve25519.constants.*
import com.github.andreypfau.curve25519.field.FieldElement
import com.github.andreypfau.curve25519.models.AffineNielsPoint
import com.github.andreypfau.curve25519.models.ProjectivePoint
import com.github.andreypfau.curve25519.scalar.Scalar
import com.github.andreypfau.curve25519.utils.byteArrayOf
import com.github.andreypfau.kotlinio.crypto.ct.Choise
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.experimental.or
import kotlin.test.*

class EdwardsTest {
    // X coordinate of the basepoint.
    // = 15112221349535400772501151409588531511454012693041857206046113283949847762202
    val BASE_X_COORD_BYTES = byteArrayOf(
        0x1a, 0xd5, 0x25, 0x8f, 0x60, 0x2d, 0x56, 0xc9, 0xb2, 0xa7, 0x25, 0x95, 0x60, 0xc7, 0x2c, 0x69,
        0x5c, 0xdc, 0xd6, 0xfd, 0x31, 0xe2, 0xa4, 0xc0, 0xfe, 0x53, 0x6e, 0xcd, 0xd3, 0x36, 0x69, 0x21
    )

    /// Compressed Edwards Y form of 2*basepoint.
    val BASE2_CMPRSSD = CompressedEdwardsY(
        byteArrayOf(
            0xc9, 0xa3, 0xf8, 0x6a, 0xae, 0x46, 0x5f, 0xe,
            0x56, 0x51, 0x38, 0x64, 0x51, 0x0f, 0x39, 0x97,
            0x56, 0x1f, 0xa2, 0xc9, 0xe8, 0x5e, 0xa2, 0x1d,
            0xc2, 0x29, 0x23, 0x09, 0xf3, 0xcd, 0x60, 0x22
        )
    )

    // Compressed Edwards Y form of 16*basepoint.
    val BASE16_CMPRSSD = CompressedEdwardsY(
        byteArrayOf(
            0xeb, 0x27, 0x67, 0xc1, 0x37, 0xab, 0x7a, 0xd8,
            0x27, 0x9c, 0x07, 0x8e, 0xff, 0x11, 0x6a, 0xb0,
            0x78, 0x6e, 0xad, 0x3a, 0x2e, 0x0f, 0x98, 0x9f,
            0x72, 0xc3, 0x7f, 0x82, 0xf2, 0x96, 0x96, 0x70
        )
    )

    // 4493907448824000747700850167940867464579944529806937181821189941592931634714
    val A_SCALAR = Scalar(
        byteArrayOf(
            0x1a, 0x0e, 0x97, 0x8a, 0x90, 0xf6, 0x62, 0x2d,
            0x37, 0x47, 0x02, 0x3f, 0x8a, 0xd8, 0x26, 0x4d,
            0xa7, 0x58, 0xaa, 0x1b, 0x88, 0xe0, 0x40, 0xd1,
            0x58, 0x9e, 0x7b, 0x7f, 0x23, 0x76, 0xef, 0x09,
        )
    )

    // 2506056684125797857694181776241676200180934651973138769173342316833279714961
    val B_SCALAR = Scalar(
        byteArrayOf(
            0x91, 0x26, 0x7a, 0xcf, 0x25, 0xc2, 0x09, 0x1b,
            0xa2, 0x17, 0x74, 0x7b, 0x66, 0xf0, 0xb3, 0x2e,
            0x9d, 0xf2, 0xa5, 0x67, 0x41, 0xcf, 0xda, 0xc4,
            0x56, 0xa7, 0xd4, 0xaa, 0xb8, 0x60, 0x8a, 0x05,
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

    /// A_SCALAR * (A_TIMES_BASEPOINT) + B_SCALAR * BASEPOINT
    /// computed with ed25519.py
    val DOUBLE_SCALAR_MULT_RESULT = CompressedEdwardsY(
        byteArrayOf(
            0x7d, 0xfd, 0x6c, 0x45, 0xaf, 0x6d, 0x6e, 0x0e,
            0xba, 0x20, 0x37, 0x1a, 0x23, 0x64, 0x59, 0xc4,
            0xc0, 0x46, 0x83, 0x43, 0xde, 0x70, 0x4b, 0x85,
            0x09, 0x6f, 0xfe, 0x35, 0x4f, 0x13, 0x2b, 0x42
        )
    )

    /**
     * Test round-trip decompression for the basepoint.
     */
    @Test
    fun basepointDecompressionCompression() {
        val baseX = FieldElement(BASE_X_COORD_BYTES)
        val bp = ED25519_BASEPOINT_COMPRESSED.decompress()
        assertNotNull(bp)
        assertTrue(bp.isValid())
        // Check that decompression actually gives the correct X coordinate
        assertEquals(bp.x, baseX)
        assertEquals(ED25519_BASEPOINT_COMPRESSED, bp.compress())
    }

    /**
     * Test sign handling in decompression
     */
    @Test
    fun decompressionSignHandling() {
        // Manually set the high bit of the last byte to flip the sign
        val minusBytepointBytes = ED25519_BASEPOINT_COMPRESSED.toByteArray()
        minusBytepointBytes[31] = minusBytepointBytes[31] or (1 shl 7).toByte()
        val minusBasepoint = CompressedEdwardsY(minusBytepointBytes).decompress()
        // Test projective coordinates exactly since we know they should
        // only differ by a flipped sign.
        assertNotNull(minusBasepoint)
        assertEquals(-ED25519_BASEPOINT_POINT.x, minusBasepoint.x)
        assertEquals(ED25519_BASEPOINT_POINT.y, minusBasepoint.y)
        assertEquals(ED25519_BASEPOINT_POINT.z, minusBasepoint.z)
        assertEquals(-ED25519_BASEPOINT_POINT.t, minusBasepoint.t)
    }

    /**
     * Test that computing 1*basepoint gives the correct basepoint.
     */
    @Test
    fun basepointMultOneVsBasepoint() {
        val bp = EdwardsBasepointTable * Scalar.ONE
        val compressed = bp.compress()
        assertEquals(ED25519_BASEPOINT_COMPRESSED, compressed)
    }

    /**
     * Test that [EdwardsBasepointTable.basepoint] gives the correct basepoint.
     */
    @Test
    fun basepointTableBasepointFunctionCorrect() {
        val bp = EdwardsBasepointTable.basepoint()
        assertEquals(ED25519_BASEPOINT_COMPRESSED, bp.compress())
    }

    /**
     * Test [EdwardsPoint.plus]
     * using basepoint + basepoint versus the 2*basepoint constant.
     */
    @Test
    fun basepointPlusBasepointVsBasepoint2() {
        val bp = ED25519_BASEPOINT_POINT
        val bpAdded = bp + bp
        assertEquals(BASE2_CMPRSSD, bpAdded.compress())
    }

    /**
     * Test [EdwardsPoint.plus]
     * using the basepoint, basepoint2 constants
     */
    @Test
    fun basepointPlusBasepointProjectiveNielsVsBasepoint2() {
        val bp = ED25519_BASEPOINT_POINT
        val bpAdded = (bp + bp.toProjectiveNiels()).toExtended()
        assertEquals(BASE2_CMPRSSD, bpAdded.compress())
    }

    /**
     * Test [EdwardsPoint.plus]
     * using the basepoint, basepoint2 constants
     */
    @Test
    fun basepointPlusBasepointAffineNielsVsBasepoint2() {
        val bp = ED25519_BASEPOINT_POINT
        val bpAffineNiels = bp.toAffineNiels()
        val bpAdded = (bp + bpAffineNiels).toExtended()
        assertEquals(BASE2_CMPRSSD, bpAdded.compress())
    }

    /**
     * Check that equality of [EdwardsPoint] handles projective
     * coordinates correctly.
     */
    @Test
    fun extendedPointEqualityScaling() {
        val twoBytes = ByteArray(32)
        twoBytes[0] = 2
        val id1 = EdwardsPoint.IDENTITY
        val id2 = EdwardsPoint(
            x = FieldElement.ZERO,
            y = FieldElement(twoBytes),
            z = FieldElement(twoBytes),
            t = FieldElement.ZERO
        )
        assertEquals(Choise.TRUE, id1.ctEquals(id2))
    }

    /**
     * Sanity check for conversion to precomputed points
     */
    @Test
    fun toAffineNielsClearsDenominators() {
        // construct a point as aB so it has denominators (ie. Z != 1)
        val aB = EdwardsBasepointTable * A_SCALAR
        val aBAffineNiles = aB.toAffineNiels()
        val alsoAb = (EdwardsPoint.IDENTITY + aBAffineNiles).toExtended()
        assertEquals(aB.compress(), alsoAb.compress())
    }

    /**
     * Test basepoint_mult versus a known scalar multiple from ed25519.py
     */
    @Test
    fun basepointMultVsEd25519py() {
        val aB = EdwardsBasepointTable * A_SCALAR
        assertEquals(A_TIMES_BASEPOINT, aB.compress())
    }

    @Ignore
    @Test
    fun basepointMultByBasepointOrder() {
        val b = EdwardsBasepointTable
        val shouldBeId = b * BASEPOINT_ORDER
        assertTrue(shouldBeId.isIdentity())
    }

    /**
     * Test precomputed basepoint multiply
     */
    @Test
    fun testPrecomputedBasepointMult() {
        val aB1 = EdwardsBasepointTable * A_SCALAR
        val aB2 = ED25519_BASEPOINT_POINT * A_SCALAR
        assertEquals(aB1.compress(), aB2.compress())
    }

    /**
     * Test scalar_mul versus a known scalar multiple from ed25519.py
     */
    @Test
    fun scalarMulVsEd25519py() {
        val ab = ED25519_BASEPOINT_POINT * A_SCALAR
        assertEquals(A_TIMES_BASEPOINT, ab.compress())
    }

    /**
     * Test [EdwardsPoint.double] versus the 2*basepoint constant
     */
    @Test
    fun basepointDoubleVsBasepoint2() {
        val double = ED25519_BASEPOINT_POINT.double().compress()
        assertEquals(BASE2_CMPRSSD, double)
    }

    /**
     * Test that computing 2*basepoint is the same as [EdwardsPoint.double]
     */
    @Test
    fun basepointMultTwoVsBasepoint2() {
        val two = Scalar(2u)
        val bp2 = EdwardsBasepointTable * two
        assertEquals(BASE2_CMPRSSD, bp2.compress())
    }

    /**
     * Check that converting to projective and then back to extended round-trips.
     */
    @Test
    fun basepointProjectiveExtendedRoundTrip() {
        assertEquals(
            ED25519_BASEPOINT_COMPRESSED,
            ED25519_BASEPOINT_POINT.toProjective().toExtended().compress()
        )
    }

    /**
     * Test computing 16*basepoint vs mulByPow2(4)
     */
    @Test
    fun basepoint16VsMulBy16() {
        val bp16 = ED25519_BASEPOINT_POINT.mulByPow2(4)
        assertEquals(BASE16_CMPRSSD, bp16.compress())
    }

    @Test
    fun conditionalSelectForAffineNielsPoint() {
        val id = AffineNielsPoint.IDENTITY
        var p1 = AffineNielsPoint.IDENTITY
        val bp = ED25519_BASEPOINT_POINT.toAffineNiels()
        p1 = p1.conditionalSelect(bp, Choise.FALSE)
        assertEquals(p1, id)
        p1 = p1.conditionalSelect(bp, Choise.TRUE)
        assertEquals(p1, bp)
    }

    @Test
    fun compressedIdentity() {
        assertEquals(CompressedEdwardsY.IDENTITY, EdwardsPoint.IDENTITY.compress())
    }

    @Test
    fun isIdentity() {
        assertTrue(EdwardsPoint.IDENTITY.isIdentity())
        assertTrue(!ED25519_BASEPOINT_POINT.isIdentity())
    }

    @Test
    fun scalarMultExtendedPointWorksBothWays() {
        val g = ED25519_BASEPOINT_POINT
        val s = A_SCALAR
        val p1 = g * s
        val p2 = s * g
        assertContentEquals(
            p1.compress().toByteArray(),
            p2.compress().toByteArray()
        )
    }
}

fun EdwardsPoint?.isValid(): Boolean {
    contract {
        returns(false) implies (this@isValid != null)
    }
    if (this == null) return false
    val pointOnCurve = toProjective().isValid()
    val onSegreImage = (x * y) == (z * t)
    return pointOnCurve && onSegreImage
}

fun ProjectivePoint?.isValid(): Boolean {
    contract {
        returns(false) implies (this@isValid != null)
    }
    if (this == null) return false
    val xx = x.square()
    val yy = y.square()
    val zz = z.square()
    val zzzz = zz.square()
    val lhs = (yy - xx) * zz
    val rhs = zzzz + (EDWARDS_D * (xx * yy))
    return lhs == rhs
}
