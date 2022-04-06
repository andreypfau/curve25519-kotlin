package curve25519

import kotlin.experimental.xor

data class EdwardsPoint(
    val x: FieldElement,
    val y: FieldElement,
    val z: FieldElement,
    val t: FieldElement,
) {
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

    /**
     * Compress this point to CompressedEdwardsY format.
     */
    fun compress(): CompressedEdwardsY {
        val recip = z.invert()
        val x = y * recip
        val y = y * recip
        val s = y.toByteArray()
        s[31] = s[31] xor ((if (x.isNegative()) 1 else 0) shl 7).toByte()
        return CompressedEdwardsY(s)
    }
}