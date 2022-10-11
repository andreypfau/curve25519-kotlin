package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.constants.tables.ED25519_BASEPOINT_TABLE
import com.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.internal.sha512
import com.github.andreypfau.curve25519.scalar.Scalar
import kotlin.random.Random

class Ed25519PrivateKey private constructor(
    val data: ByteArray
) {
    companion object {
        const val SIZE_BYTES = 64
        const val SEED_SIZE = 32

        fun generateKey(random: Random): Pair<Ed25519PrivateKey, Ed25519PublicKey> {
            val seed = random.nextBytes(SEED_SIZE)
            val privateKey = keyFromSeed(seed)
            val publicKey = Ed25519PublicKey(privateKey.data.copyOfRange(32, 64))
            return privateKey to publicKey
        }

        fun keyFromSeed(seed: ByteArray): Ed25519PrivateKey {
            val privateKey = ByteArray(SIZE_BYTES)
            keyFromSeed(privateKey, seed)
            return Ed25519PrivateKey(privateKey)
        }

        fun keyFromSeed(privateKey: ByteArray, seed: ByteArray) {
            require(seed.size == SEED_SIZE) { "ed25519: bad length: ${seed.size}" }
            val digest = sha512(seed)
            digest[0] = (digest[0].toInt() and 248).toByte()
            digest[31] = (digest[31].toInt() and 127).toByte()
            digest[31] = (digest[31].toInt() or 64).toByte()

            val a = Scalar()
            a.setRawData(digest)

            val aa = EdwardsPoint()
            val aaCompressed = CompressedEdwardsY()
            aaCompressed.set(aa.mul(ED25519_BASEPOINT_TABLE, a))
            seed.copyInto(privateKey)
            aaCompressed.data.copyInto(privateKey, 32)
        }
    }
}
