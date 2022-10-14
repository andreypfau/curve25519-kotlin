package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.internal.ZeroRandom
import kotlin.test.Test
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
}
