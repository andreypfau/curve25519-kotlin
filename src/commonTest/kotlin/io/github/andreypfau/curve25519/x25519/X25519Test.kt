package io.github.andreypfau.curve25519.x25519

import io.github.andreypfau.curve25519.ed25519.Ed25519
import io.github.andreypfau.curve25519.internal.ZeroRandom
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals

class X25519Test {
    @Test
    fun testExample() {
        val alicePrivate = Random.nextBytes(X25519.SCALAR_SIZE_BYTES)
        val alicePublic = X25519.x25519(alicePrivate)

        val bobPrivate = Random.nextBytes(X25519.SCALAR_SIZE_BYTES)
        val bobPublic = X25519.x25519(bobPrivate)

        val aliceShared = X25519.x25519(alicePrivate, bobPublic)
        val bobShared = X25519.x25519(bobPrivate, alicePublic)

        assertContentEquals(aliceShared, bobShared)
    }

    @Test
    fun testVectors() {
        X25519_TEST_VECTORS.forEach { x25519TestVector ->
            val bytes = X25519.x25519(x25519TestVector.scalar, x25519TestVector.point)
            assertContentEquals(x25519TestVector.expected, bytes)
        }
    }

    @Test
    fun testX25519Conversion() {
        val privateKey = Ed25519.generateKey(ZeroRandom)
        val publicKey = privateKey.publicKey()

        val xPrivateKey = X25519.toX25519(privateKey)
        val xPublicKey1 = X25519.x25519(xPrivateKey)
        val xPublicKey2 = X25519.toX25519(publicKey)

        assertContentEquals(xPublicKey1, xPublicKey2)
    }
}
