package x25519

import curve25519.Scalar
import curve25519.times
import kotlin.jvm.JvmInline
import kotlin.random.Random

/**
 * A Diffie-Hellman private key which may be used more than once,
 * but is purposefully not serialiseable in order to discourage key-reuse.
 * This is implemented to facilitate protocols such as Noise (e.g. Noise IK key usage, etc.)
 * and X3DH which require an "ephemeral" key to conduct the Diffie-Hellman operation multiple times throughout the protocol,
 * while the protocol run at a higher level is only conducted once per key.
 */
@JvmInline
value class PrivateKey(
    val scalar: Scalar,
) {
    constructor(byteArray: ByteArray) : this(Scalar.clamp(byteArray))
    constructor(random: Random = Random.Default) : this(random.nextBytes(32))

    fun toByteArray(byteArray: ByteArray = ByteArray(32)): ByteArray {
        scalar.value.copyInto(byteArray)
        return byteArray
    }

    fun diffieHellman(publicKey: PublicKey): SharedKey =
        SharedKey(scalar * publicKey.value)

    operator fun times(publicKey: PublicKey): SharedKey =
        diffieHellman(publicKey)
}