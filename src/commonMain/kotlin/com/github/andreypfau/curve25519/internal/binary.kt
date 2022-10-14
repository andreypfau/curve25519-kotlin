package com.github.andreypfau.curve25519.internal

fun ByteArray.getLongLE(offset: Int = 0): Long {
    return ((this[offset].toUByte().toLong()) or
            (this[offset + 1].toUByte().toLong() shl 8) or
            (this[offset + 2].toUByte().toLong() shl 16) or
            (this[offset + 3].toUByte().toLong() shl 24) or
            (this[offset + 4].toUByte().toLong() shl 32) or
            (this[offset + 5].toUByte().toLong() shl 40) or
            (this[offset + 6].toUByte().toLong() shl 48) or
            (this[offset + 7].toUByte().toLong() shl 56))
}

fun ByteArray.getULongLE(index: Int = 0): ULong = getLongLE(index).toULong()

@Suppress("SpellCheckingInspection")
private const val HEX_CHARS = "0123456789abcdef"

internal fun hex(
    hexString: String
): ByteArray {
    require(hexString.length % 2 == 0) { hexString }
    return ByteArray(hexString.length / 2).apply {
        var i = 0
        while (i < hexString.length) {
            this[i / 2] = ((hex(hexString[i]) shl 4) + hex(hexString[i + 1])).toByte()
            i += 2
        }
    }
}

private inline fun hex(ch: Char): Int = when (ch) {
    in '0'..'9' -> ch - '0'
    in 'A'..'F' -> ch - 'A' + 10
    in 'a'..'f' -> ch - 'a' + 10
    else -> throw (IllegalArgumentException("'$ch' is not a valid hex character"))
}
