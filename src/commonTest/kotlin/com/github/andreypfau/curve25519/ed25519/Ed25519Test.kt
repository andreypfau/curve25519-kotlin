package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.internal.ZeroRandom
import com.github.andreypfau.curve25519.internal.hex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class Ed25519Test {
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

    @OptIn(ExperimentalTime::class)
    @Test
    fun testGolden() {
        GOLDEN_ED25519.lines().forEach { line ->
            measureTime {
                testGolden(line)
            }.let {
                println("$it - $line")
            }
        }
    }

    fun testGolden(line: String) {
        var (
            privateBytes,
            pubKey,
            msg,
            sig
        ) = line.split(":").map { hex(it) }

        sig = sig.copyOf(Ed25519.SIGNATURE_SIZE_BYTES)
        val priv = Ed25519PrivateKey(ByteArray(Ed25519.PRIVATE_KEY_SIZE_BYTES))
        privateBytes.copyInto(priv.data)
        pubKey.copyInto(priv.data, 32)

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
