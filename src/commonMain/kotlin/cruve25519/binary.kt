package cruve25519

internal fun ByteArray.ulong(range: IntRange = 0..ULong.SIZE_BITS): ULong {
    return this[range.first + 0].toULong() or
            this[range.first + 1].toULong() shl 8 or
            this[range.first + 2].toULong() shl 16 or
            this[range.first + 3].toULong() shl 24 or
            this[range.first + 4].toULong() shl 32 or
            this[range.first + 5].toULong() shl 40 or
            this[range.first + 6].toULong() shl 48 or
            this[range.first + 7].toULong() shl 56
}
