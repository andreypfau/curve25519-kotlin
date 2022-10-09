package com.github.andreypfau.curve25519.edwards

import com.github.andreypfau.curve25519.AffineNielsPoint
import com.github.andreypfau.curve25519.CompletedPoint
import com.github.andreypfau.curve25519.Identity
import com.github.andreypfau.curve25519.ProjectivePoint
import com.github.andreypfau.curve25519.constants.EDWARDS_D2
import com.github.andreypfau.curve25519.field.FieldElement
import com.github.andreypfau.curve25519.models.ProjectiveNielsPoint
import com.github.andreypfau.curve25519.montgomery.MontgomeryPoint
import com.github.andreypfau.curve25519.scalar.Scalar
import com.github.andreypfau.curve25519.scalar.mul
import com.github.andreypfau.kotlinio.crypto.ct.Choise
import kotlin.experimental.xor

data class EdwardsPoint(
    val x: FieldElement = FieldElement.zero(),
    val y: FieldElement = FieldElement.one(),
    val z: FieldElement = FieldElement.one(),
    val t: FieldElement = FieldElement.zero(),
) : Identity<EdwardsPoint> {

    /**
     * Convert to a [ProjectiveNielsPoint]
     */
    fun toProjectiveNiels(): ProjectiveNielsPoint =
        ProjectiveNielsPoint(
            yPlusX = y + x,
            yMinusX = y - x,
            z = z,
            t2d = t * EDWARDS_D2
        )

    /**
     * Convert this `EdwardsPoint` on the Edwards model to the
     * corresponding `MontgomeryPoint` on the Montgomery model.
     *
     * This function has one exceptional case; the identity point of
     * the Edwards curve is sent to the 2-torsion point \\((0,0)\\)
     * on the Montgomery curve.
     *
     * Note that this is a one-way conversion, since the Montgomery
     * model does not retain sign information.
     */
    fun toMontgomeryPoint(): MontgomeryPoint {
        // We have u = (1+y)/(1-y) = (Z+Y)/(Z-Y).
        //
        // The denominator is zero only when y=1, the identity point of
        // the Edwards curve.  Since 0.invert() = 0, in this case we
        // compute the 2-torsion point (0,0).
        val u = z + y
        val w = z - y
        val uw = u * w.invert()
        return MontgomeryPoint(uw.toByteArray())
    }

    fun toProjective(): ProjectivePoint = ProjectivePoint(x, y, z)

    fun toAffineNiels(): AffineNielsPoint {
        val recip = z.invert()
        val x = x * recip
        val y = y * recip
        val xy2d = (x * y) * EDWARDS_D2
        return AffineNielsPoint(
            yPlusX = y + x,
            yMinusX = y - x,
            xy2d = xy2d
        )
    }

    /**
     * Compress this point to CompressedEdwardsY format.
     */
    fun compress(): CompressedEdwardsY {
        val recip = z.invert()
        val x = x * recip
        val y = y * recip
        val s = y.toByteArray()
        s[31] = s[31] xor (x.isNegative().toUByte().toInt() shl 7).toByte()
        return CompressedEdwardsY(s)
    }

    fun double(): EdwardsPoint =
        toProjective().double().toExtended()

    operator fun plus(other: AffineNielsPoint): CompletedPoint {
        val yPlusX = y + x
        val yMinusX = y - x
        val pp = yPlusX * other.yPlusX
        val mm = yMinusX * other.yMinusX
        val txy2d = t * other.xy2d
        val z2 = z + z
        return CompletedPoint(
            x = pp - mm,
            y = pp + mm,
            z = z2 + txy2d,
            t = z2 - txy2d
        )
    }

    operator fun plus(other: ProjectiveNielsPoint): CompletedPoint {
        val yPlusX = y + x
        val yMinusX = y - x
        val pp = yPlusX * other.yPlusX
        val mm = yMinusX * other.yMinusX
        val tt2d = t * other.t2d
        val zz = z * other.z
        val zz2 = zz + zz
        return CompletedPoint(
            x = pp - mm,
            y = pp + mm,
            z = zz2 + tt2d,
            t = zz2 - tt2d
        )
    }

    operator fun minus(other: AffineNielsPoint): CompletedPoint {
        val yPlusX = y + x
        val yMinusX = y - x
        val pm = yPlusX * other.yMinusX
        val mp = yMinusX * other.yPlusX
        val txy2d = t * other.xy2d
        val z2 = z * z
        return CompletedPoint(
            x = pm - mp,
            y = pm + mp,
            z = z2 - txy2d,
            t = z2 + txy2d
        )
    }

    /**
     * Compute \(2^k P \) by successive doublings. Requires \( k > 0 \).
     */
    fun mulByPow2(k: Int): EdwardsPoint {
        var r: CompletedPoint
        var s = toProjective()
        for (i in 0 until (k-1)) {
            r = s.double()
            s = r.toProjective()
        }
        // Unroll last iteration, so we can go directly toExtended()
        return s.double().toExtended()
    }

    override fun identity() = IDENTITY

    override fun isIdentity(): Boolean = ctEquals(identity()) == Choise.TRUE

    /**
     * We would like to check that the point (X/Z, Y/Z) is equal to
     * the point (X'/Z', Y'/Z') without converting into affine
     * coordinates (x, y) and (x', y'), which requires two inversions.
     * We have that X = xZ and X' = x'Z'. Thus, x = x' is equivalent to
     * (xZ)Z' = (x'Z')Z, and similarly for the y-coordinate.
     **/
    override fun ctEquals(other: EdwardsPoint): Choise {
        val a1 = (x * other.z)
        val a2 = other.x * z
        val a =  (x * other.z).ctEquals(other.x * z)
        val b = (y * other.z).ctEquals(other.y * z)
        return a and b
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EdwardsPoint) return false
        return ctEquals(other) == Choise.TRUE
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + t.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("EdwardsPoint{").appendLine()
        append("  X: ").append(x).appendLine()
        append("  Y: ").append(y).appendLine()
        append("  Z: ").append(z).appendLine()
        append("  T: ").append(t).appendLine()
        append("}")
    }

    operator fun times(scalar: Scalar): EdwardsPoint = mul(this, scalar)
    operator fun plus(other: EdwardsPoint): EdwardsPoint =
        (this + other.toProjectiveNiels()).toExtended()

    companion object {
        val IDENTITY = EdwardsPoint()
    }
}
