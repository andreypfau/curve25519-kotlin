package io.github.andreypfau.curve25519.ed25519

import io.github.andreypfau.curve25519.constants.tables.ED25519_BASEPOINT_TABLE
import io.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import io.github.andreypfau.curve25519.edwards.EdwardsPoint
import io.github.andreypfau.curve25519.internal.sha512
import io.github.andreypfau.curve25519.scalar.Scalar

class Ed25519PrivateKey internal constructor(
    internal val data: ByteArray
) {
    fun toByteArray(): ByteArray = toByteArray(ByteArray(Ed25519.PRIVATE_KEY_SIZE_BYTES))
    fun toByteArray(output: ByteArray, offset: Int = 0): ByteArray =
        data.copyInto(output, offset)

    fun seed(): ByteArray = seed(ByteArray(Ed25519.SEED_SIZE_BYTES))
    fun seed(output: ByteArray, offset: Int = 0): ByteArray {
        data.copyInto(output, offset, 0, Ed25519.SEED_SIZE_BYTES)
        return output
    }

    fun publicKey(): Ed25519PublicKey =
        Ed25519PublicKey(data.copyOfRange(32, 64))

    fun sign(message: ByteArray): ByteArray = sign(message, ByteArray(Ed25519.SIGNATURE_SIZE_BYTES))
    fun sign(message: ByteArray, output: ByteArray, offset: Int = 0): ByteArray {
        val extsk = sha512(data, 0, 32)
        extsk[0] = (extsk[0].toInt() and 248).toByte()
        extsk[31] = (extsk[31].toInt() and 127).toByte()
        extsk[31] = (extsk[31].toInt() or 64).toByte()

        val hashR = sha512(extsk.copyOfRange(32, extsk.size) + message)
        val r = Scalar.fromWideByteArray(hashR)

        // R = rB
        val R = EdwardsPoint().mulBasepoint(ED25519_BASEPOINT_TABLE, r)
        val rCompressed = CompressedEdwardsY.from(R)

        // S = H(R,A,m)
        val s = Scalar()
        val hashRam = sha512(rCompressed.data + data.copyOfRange(32, data.size) + message)
        s.setWideByteArray(hashRam)

        val a = Scalar.fromByteArray(extsk)
        s.mul(s, a)

        // S = (r + H(R,A,m)a)
        s.add(s, r)

        // S = (r + H(R,A,m)a) mod L
        rCompressed.data.copyInto(output, offset)
        s.toByteArray(output, offset + 32)

        return output
    }

    fun sharedKey(
        publicKey: Ed25519PublicKey,
        output: ByteArray = ByteArray(32),
        offset: Int = 0
    ): ByteArray = Ed25519.sharedKey(this, publicKey, output, offset)

    companion object {
        const val SIZE_BYTES = Ed25519.PRIVATE_KEY_SIZE_BYTES
    }
}
