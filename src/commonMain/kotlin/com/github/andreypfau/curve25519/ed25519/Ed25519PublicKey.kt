package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.internal.varTimeDoubleScalarBaseMul
import com.github.andreypfau.curve25519.scalar.Scalar
import com.github.andreypfau.kotlinio.crypto.digest.Sha512
import com.github.andreypfau.kotlinio.pool.useInstance

class Ed25519PublicKey internal constructor(
    internal val data: ByteArray
) {
    fun verify(
        message: ByteArray,
        signature: ByteArray
    ): Boolean {
        val aCompressed = CompressedEdwardsY(data)
        val a = EdwardsPoint.from(aCompressed)

        // hram = H(R,A,m)
        val hash = Sha512.POOL.useInstance {
            it.update(signature, 0, 32)
            it.update(data)
            it.update(message)
            it.digest()
        }
        val k = Scalar.fromWideByteArray(hash)
        val s = Scalar.fromByteArray(signature, 32)

        // A = -A (Since we want SB - H(R,A,m)A)
        a.negate(a)

        // Check that [8]R == [8](SB - H(R,A,m)A)), by computing
        // [delta S]B - [delta A]H(R,A,m) - [delta]R, multiplying the
        // result by the cofactor, and checking if the result is
        // small order.
        //
        // Note: IsSmallOrder includes a cofactor multiply.
        val r = varTimeDoubleScalarBaseMul(k, a, s)
        val rCompressed = CompressedEdwardsY.from(r)

        return rCompressed.data.contentEquals(signature.copyOf(32))
    }

    fun sharedKey(
        privateKey: Ed25519PrivateKey,
        output: ByteArray = ByteArray(32),
        offset: Int = 0
    ): ByteArray = Ed25519.sharedKey(privateKey, this, output, offset)

    companion object {
        const val SIZE_BYTES = Ed25519.PUBLIC_KEY_SIZE_BYTES
    }
}
