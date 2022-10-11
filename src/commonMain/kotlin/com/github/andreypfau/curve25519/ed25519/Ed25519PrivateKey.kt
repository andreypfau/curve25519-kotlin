package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.constants.tables.ED25519_BASEPOINT_TABLE
import com.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.internal.sha512
import com.github.andreypfau.curve25519.scalar.Scalar
import com.github.andreypfau.kotlinio.crypto.digest.Sha512
import com.github.andreypfau.kotlinio.pool.useInstance

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

        val hashR = Sha512.POOL.useInstance {
            it.update(extsk, 32)
            it.update(message)
            it.digest()
        }
        val r = Scalar().apply {
            wideBytes(hashR)
        }

        // R = rB
        val bigRPoint = EdwardsPoint().apply {
            mulBasepoint(ED25519_BASEPOINT_TABLE, r)
        }
        val rCompressed = CompressedEdwardsY().apply {
            set(bigRPoint)
        }

        // S = H(R,A,m)
        val s = Scalar()
        val hashRam = Sha512.POOL.useInstance {
            it.update(rCompressed.data)
            it.update(data, 32)
            it.update(message)
            it.digest()
        }
        s.wideBytes(hashRam)

        val a = Scalar().apply {
            bits(extsk)
        }
        s.mul(s, a)

        // S = (r + H(R,A,m)a)
        s.add(s, r)

        // S = (r + H(R,A,m)a) mod L
        rCompressed.data.copyInto(output, offset)
        s.toByteArray(output, offset + 32)

        return output
    }

    companion object {
        const val SIZE_BYTES = Ed25519.PRIVATE_KEY_SIZE_BYTES
    }
}
