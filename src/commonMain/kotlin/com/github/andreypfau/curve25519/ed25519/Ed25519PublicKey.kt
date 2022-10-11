package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.exceptioin.InvalidYCoordinateException

class Ed25519PublicKey internal constructor(
    private val data: ByteArray
) {
    @Suppress("UNREACHABLE_CODE")
    fun verify(message: ByteArray, signature: ByteArray): Boolean {
        TODO()

        if (signature.size != Ed25519.SIGNATURE_SIZE_BYTES) return false
        val aCompressed = CompressedEdwardsY().apply {
            set(data)
        }
        // Unpack and ensure the public key is well-formed (A).
        val a = EdwardsPoint().apply {
            try {
                set(aCompressed)
            } catch (e: InvalidYCoordinateException) {
                return false
            }
        }
    }

    companion object {
        const val SIZE_BYTES = Ed25519.PUBLIC_KEY_SIZE_BYTES
    }
}
