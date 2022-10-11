package com.github.andreypfau.curve25519.ed25519

import com.github.andreypfau.curve25519.internal.ZeroRandom
import kotlin.test.Test

class Ed25519Test {
    @Test
    fun testSignVerify() {
        val privateKey = Ed25519.generateKey(ZeroRandom)
        val message = "test message".encodeToByteArray()
        val signature = privateKey.sign(message)
        val publicKey = privateKey.publicKey()
        println(signature)
    }
}
