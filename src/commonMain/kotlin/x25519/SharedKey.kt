package x25519

import curve25519.MontgomeryPoint

/**
 * The result of a Diffie-Hellman key exchange.
 */
value class SharedKey(
    val value: MontgomeryPoint,
) {
    fun toByteArray(output: ByteArray = ByteArray(32)): ByteArray =
        value.toByteArray(output)
}