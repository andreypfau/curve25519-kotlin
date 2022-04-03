package curve25519

class MontgomeryPoint(
    val value: ByteArray = ByteArray(32)
) {
    fun toBytes(bytes: ByteArray = ByteArray(32)) = bytes.apply {
        value.copyInto(bytes)
    }
}