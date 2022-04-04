package x25519

import curve25519.MontgomeryPoint
import kotlin.jvm.JvmInline

@JvmInline
value class PublicKey(
    val value: MontgomeryPoint,
) {
    constructor(byteArray: ByteArray) : this(MontgomeryPoint(byteArray))
    constructor(privateKey: PrivateKey) : this()

    fun toByteArray(output: ByteArray = ByteArray(32)): ByteArray =
        value.toByteArray(output)
}