@file:Suppress("OPT_IN_USAGE")

package encoding.binary

sealed interface ByteOrder {
    fun uLong(bytes: ByteArray, offset: Int = 0): ULong
    fun putULong(bytes: ByteArray, value: ULong, offset: Int = 0)
}

object LittleEndian : ByteOrder {
    override fun uLong(bytes: ByteArray, offset: Int): ULong {
        return (bytes[offset].toULong() and 0xFFu) or
                ((bytes[offset + 1].toULong() and 0xFFu) shl 8) or
                ((bytes[offset + 2].toULong() and 0xFFu) shl 16) or
                ((bytes[offset + 3].toULong() and 0xFFu) shl 24) or
                ((bytes[offset + 4].toULong() and 0xFFu) shl 32) or
                ((bytes[offset + 5].toULong() and 0xFFu) shl 40) or
                ((bytes[offset + 6].toULong() and 0xFFu) shl 48) or
                ((bytes[offset + 7].toULong() and 0xFFu) shl 56)
    }

    override fun putULong(bytes: ByteArray, value: ULong, offset: Int) {
        bytes[offset] = value.toByte()
        bytes[offset + 1] = (value shr 8).toByte()
        bytes[offset + 2] = (value shr 16).toByte()
        bytes[offset + 3] = (value shr 24).toByte()
        bytes[offset + 4] = (value shr 32).toByte()
        bytes[offset + 5] = (value shr 40).toByte()
        bytes[offset + 6] = (value shr 48).toByte()
        bytes[offset + 7] = (value shr 56).toByte()
    }
}
