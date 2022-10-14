package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.internal.ZeroRandom
import com.github.andreypfau.curve25519.internal.hex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun testGolden() {
        GOLDEN_ED25519.lines().forEach { line ->
            testGolden(line)
        }
    }

    @Test
    fun a() {
        testGolden("b780381a65edf8b78f6945e8dbec7941ac049fd4c61040cf0c324357975a293ce253af0766804b869bb1595be9765b534886bbaab8305bf50dbc7f899bfb5f01:e253af0766804b869bb1595be9765b534886bbaab8305bf50dbc7f899bfb5f01:18b6bec097:b2fc46ad47af464478c199e1f8be169f1be6327c7f9a0a6689371ca94caf04064a01b22aff1520abd58951341603faed768cf78ce97ae7b038abfe456aa17c0918b6bec097:")
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
        assertContentEquals(sig, sig2, "Failed check: $line")
    }
}
