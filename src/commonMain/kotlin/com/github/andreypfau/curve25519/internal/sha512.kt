package com.github.andreypfau.curve25519.internal

internal expect fun sha512(
    rawData: ByteArray, offset: Int = 0, length: Int = rawData.size - offset,
    output: ByteArray = ByteArray(64), outputOffset: Int = 0, outputLength: Int = 64
): ByteArray
