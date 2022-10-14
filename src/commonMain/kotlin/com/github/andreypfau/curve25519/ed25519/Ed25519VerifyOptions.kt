package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.scalar.Scalar
import com.github.andreypfau.curve25519.scalar.scalarMinimalVarTime

/**
 * Can be used to specify verification behavior for compatibility
 * with other Ed25519 implementations.
 */
data class Ed25519VerifyOptions(
    /**
     * Allows signatures with a small order A.
     *
     * Note: This disables the check that makes the scheme strongly binding.
     */
    val allowSmallOrderA: Boolean = false,
    /**
     * Allows signatures with a small order R.
     *
     * Note: Rejecting small order R is NOT required for binding.
     */
    val allowSmallOrderR: Boolean = false,
    /**
     * Allows signatures with a non-canonical encoding of A.
     */
    val allowNonCanonicalA: Boolean = false,
    /**
     * Allows signatures with a non-canonical encoding of R
     */
    val allowNonCanonicalR: Boolean = true,
) {
    val verifyNeedsDecompressedR get() = !allowSmallOrderR

    fun unpackPublicKey(publicKey: Ed25519PublicKey, a: EdwardsPoint = EdwardsPoint()): Boolean {
        // Unpack A.
        val aCompressed = CompressedEdwardsY()
        try {
            aCompressed.set(publicKey.data)
            a.set(aCompressed)
        } catch (e: Exception) {
            return false
        }

        // Check A order (required for strong binding).
        if (!allowSmallOrderA && a.isSmallOrder()) return false
        if (!allowNonCanonicalR && !aCompressed.isCannonicalVartime()) return false

        return true
    }

    fun unpackSignature(signature: ByteArray, r: EdwardsPoint, s: Scalar): Boolean {
        if (signature.size != Ed25519.SIGNATURE_SIZE_BYTES) return false

        if (!scalarMinimalVarTime(signature, 32)) return false

        val rCompressed = CompressedEdwardsY(signature)
        if (verifyNeedsDecompressedR) {
            try {
                r.set(rCompressed)
            } catch (e: Exception) {
                return false
            }

            // Check R order.
            if (!allowSmallOrderR && r.isSmallOrder()) {
                return false
            }
        } else {
            r.identity()
        }

        // Check if R is canonical.
        if (!allowNonCanonicalR && !rCompressed.isCannonicalVartime()) return false

        try {
            s.setByteArray(signature, 32)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    companion object {
        val DEFAULT = Ed25519VerifyOptions(
            allowNonCanonicalR = true
        )

        /**
         * Specifies verification behavior that is compatible with FIPS 186-5.
         * The behavior provided by this preset also matches RFC 8032 (with cofactored verification).
         */
        val FIPS_186_5 = Ed25519VerifyOptions(
            allowNonCanonicalA = true,
            allowNonCanonicalR = true
        )

        /**
         * Specifies verification behavior that is compatible with ZIP-215.
         */
        val ZIP_215 = Ed25519VerifyOptions(
            allowSmallOrderA = true,
            allowSmallOrderR = true,
            allowNonCanonicalA = true,
            allowNonCanonicalR = true,
        )
    }
}
