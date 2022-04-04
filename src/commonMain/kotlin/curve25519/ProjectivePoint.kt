package curve25519

/**
 * A ProjectivePoint holds a point on the projective line (P (Fp)),
 * which we identify with the Kummer line of the Montgomery curve.
 */
data class ProjectivePoint(
    val u: FieldElement = FieldElement.one(),
    val w: FieldElement = FieldElement.zero(),
) {
    /**
     * Dehomogenize this point to affine coordinates.
     */
    fun toAffine(): MontgomeryPoint = MontgomeryPoint((u * w.invert()).toByteArray())
}

internal fun conditionalSelect(a: ProjectivePoint, b: ProjectivePoint, condition: Boolean): ProjectivePoint =
    ProjectivePoint(
        u = conditionalSelect(a.u, b.u, condition),
        w = conditionalSelect(a.w, b.u, condition)
    )
