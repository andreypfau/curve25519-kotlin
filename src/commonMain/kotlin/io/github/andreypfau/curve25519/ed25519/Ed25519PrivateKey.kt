package io.github.andreypfau.curve25519.ed25519

import io.github.andreypfau.curve25519.constants.tables.ED25519_BASEPOINT_TABLE
import io.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import io.github.andreypfau.curve25519.edwards.EdwardsPoint
import io.github.andreypfau.curve25519.internal.sha512
import io.github.andreypfau.curve25519.scalar.Scalar

class Ed25519PrivateKey constructor(
    val data: ByteArray,
    val offset: Int,
) {
    constructor(rawData: ByteArray) : this(rawData.copyOf(SIZE_BYTES), 0)

    init {
        require(data.size - offset >= SIZE_BYTES) { "ed25519: bad length: ${data.size}" }
    }

    fun toByteArray(): ByteArray = toByteArray(ByteArray(Ed25519.PRIVATE_KEY_SIZE_BYTES))
    fun toByteArray(destination: ByteArray, destinationOffset: Int = 0): ByteArray =
        data.copyInto(destination, destinationOffset, offset, offset + SIZE_BYTES)

    fun seed(): ByteArray = seed(ByteArray(Ed25519.SEED_SIZE_BYTES))
    fun seed(destination: ByteArray, destinationOffset: Int = 0): ByteArray {
        data.copyInto(destination, destinationOffset, offset, offset + Ed25519.SEED_SIZE_BYTES)
        return destination
    }

    fun publicKey(): Ed25519PublicKey =
        Ed25519PublicKey(data, offset + Ed25519.SEED_SIZE_BYTES)

    fun sign(message: ByteArray): ByteArray = sign(message, ByteArray(Ed25519.SIGNATURE_SIZE_BYTES))
    fun sign(message: ByteArray, destination: ByteArray, destinationOffset: Int = 0): ByteArray {
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
        rCompressed.data.copyInto(destination, destinationOffset)
        s.toByteArray(destination, destinationOffset + 32)

        return destination
    }

    fun sharedKey(
        publicKey: Ed25519PublicKey
    ): ByteArray = sharedKey(publicKey, ByteArray(32))

    fun sharedKey(
        publicKey: Ed25519PublicKey,
        destination: ByteArray,
        destinationOffset: Int = 0
    ): ByteArray = Ed25519.sharedKey(this, publicKey, destination, destinationOffset)

    companion object {
        const val SIZE_BYTES = Ed25519.PRIVATE_KEY_SIZE_BYTES
    }
}
