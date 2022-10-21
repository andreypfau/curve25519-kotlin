package io.github.andreypfau.curve25519.ed25519

import io.github.andreypfau.curve25519.constants.tables.ED25519_BASEPOINT_TABLE
import io.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import io.github.andreypfau.curve25519.edwards.EdwardsPoint
import io.github.andreypfau.curve25519.internal.sha512
import io.github.andreypfau.curve25519.scalar.Scalar
import io.github.andreypfau.curve25519.x25519.X25519
import kotlin.jvm.JvmStatic
import kotlin.random.Random

object Ed25519 {
    /**
     * Size in bytes, of public keys as used in this package.
     */
    const val PUBLIC_KEY_SIZE_BYTES = 32

    /**
     * Size in bytes, of private keys as used in this package.
     */
    const val PRIVATE_KEY_SIZE_BYTES = 64

    /**
     * Size in bytes, of signatures generated and verified by this package.
     */
    const val SIGNATURE_SIZE_BYTES = 64

    /**
     * Size in bytes, of private key seeds.
     * These are the private key representations used by RFC 8032.
     */
    const val SEED_SIZE_BYTES = 32

    @JvmStatic
    fun generateKey(random: Random): Ed25519PrivateKey {
        val seed = random.nextBytes(SEED_SIZE_BYTES)
        return keyFromSeed(seed)
    }

    @JvmStatic
    fun keyFromSeed(seed: ByteArray): Ed25519PrivateKey {
        val privateKey = ByteArray(Ed25519PrivateKey.SIZE_BYTES)
        keyFromSeed(seed, privateKey)
        return Ed25519PrivateKey(privateKey)
    }

    @JvmStatic
    fun keyFromSeed(seed: ByteArray, output: ByteArray, offset: Int = 0): ByteArray {
        require(seed.size == SEED_SIZE_BYTES) { "ed25519: bad length: ${seed.size}" }
        val digest = sha512(seed)
        digest[0] = (digest[0].toInt() and 248).toByte()
        digest[31] = (digest[31].toInt() and 127).toByte()
        digest[31] = (digest[31].toInt() or 64).toByte()

        val a = Scalar()
        a.setByteArray(digest)

        val aCompressed = CompressedEdwardsY.from(EdwardsPoint.mul(ED25519_BASEPOINT_TABLE, a))
        seed.copyInto(output, offset)
        aCompressed.data.copyInto(output, offset + 32)
        return output
    }

    @JvmStatic
    fun sharedKey(
        privateKey: Ed25519PrivateKey,
        publicKey: Ed25519PublicKey,
        output: ByteArray = ByteArray(32),
        offset: Int = 0
    ): ByteArray {
        val xPrivateKey = X25519.toX25519(privateKey)
        val xPublicKey = X25519.toX25519(publicKey)
        return X25519.x25519(xPrivateKey, xPublicKey, output, offset)
    }
}
