package io.github.andreypfau.curve25519.ed25519

import io.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import io.github.andreypfau.curve25519.edwards.EdwardsPoint
import io.github.andreypfau.curve25519.internal.sha512
import io.github.andreypfau.curve25519.internal.varTimeDoubleScalarBaseMul
import io.github.andreypfau.curve25519.scalar.Scalar

class Ed25519PublicKey internal constructor(
    internal val data: ByteArray,
    internal val offset: Int = 0
) {
    constructor(data: ByteArray) : this(data.copyOf(SIZE_BYTES), 0)

    fun toByteArray(): ByteArray = toByteArray(ByteArray(SIZE_BYTES))
    fun toByteArray(destination: ByteArray, destinationOffset: Int = 0) =
        data.copyInto(destination, destinationOffset, offset, offset + SIZE_BYTES)

    fun verify(
        message: ByteArray,
        signature: ByteArray
    ): Boolean {
        val aCompressed = CompressedEdwardsY(data.copyOfRange(offset, offset + SIZE_BYTES))
        val a = EdwardsPoint.from(aCompressed)

        // hram = H(R,A,m)
        val hash = sha512(signature.copyOfRange(0, 32) + data.copyOfRange(offset, SIZE_BYTES) + message)
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
