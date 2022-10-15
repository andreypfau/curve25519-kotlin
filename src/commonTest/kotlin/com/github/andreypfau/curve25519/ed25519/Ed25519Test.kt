package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.internal.ZeroRandom
import com.github.andreypfau.curve25519.internal.hex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

open class Ed25519Test {
    @Test
    fun testSignVerify() {
        val privateKey = Ed25519.generateKey(ZeroRandom)
        val message = "test message".encodeToByteArray()
        val signature = privateKey.sign(message)
        val publicKey = privateKey.publicKey()
        assertTrue(publicKey.verify(message, signature))
        val wrongMessage = "wrong message".encodeToByteArray()
        assertFalse(publicKey.verify(wrongMessage, signature))
    }

    @Test
    fun testGolden() {
        GOLDEN_ED25519.lines().forEach { line ->
            val goldenData = Ed25519GoldenData.parse(line)
            testGolden(goldenData)
        }
    }

    fun testGolden(goldenData: Ed25519GoldenData) {
        val sig = goldenData.signature
        val priv = goldenData.privateKey
        val pubKey = goldenData.publicBytes
        val msg = goldenData.message

        val sig2 = priv.sign(msg)
        assertContentEquals(sig, sig2)

        val priv2 = Ed25519.keyFromSeed(priv.data.copyOf(32))
        assertContentEquals(priv.data, priv2.data)

        val pubKey2 = priv2.publicKey()
        assertContentEquals(pubKey, pubKey2.data)

        val seed = priv2.seed()
        assertContentEquals(priv.data.copyOf(32), seed)
    }
}

data class Ed25519GoldenData(
    val privateBytes: ByteArray,
    val publicBytes: ByteArray,
    val message: ByteArray,
    val signatureBytes: ByteArray,
) {
    val signature = signatureBytes.copyOf(Ed25519.SIGNATURE_SIZE_BYTES)
    val privateKey = Ed25519PrivateKey(ByteArray(Ed25519.PRIVATE_KEY_SIZE_BYTES)).also {
        privateBytes.copyInto(it.data)
        publicBytes.copyInto(it.data, 32)
    }

    companion object {
        fun parse(line: String): Ed25519GoldenData {
            val (
                privateBytes,
                pubKey,
                msg,
                sig
            ) = line.split(":").map { hex(it) }
            return Ed25519GoldenData(
                privateBytes,
                pubKey,
                msg,
                sig
            )
        }
    }
}
