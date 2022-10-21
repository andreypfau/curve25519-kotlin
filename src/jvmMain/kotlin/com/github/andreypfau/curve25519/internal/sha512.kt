package com.github.andreypfau.curve25519.internal

import java.security.MessageDigest

internal actual fun sha512(
    rawData: ByteArray,
    offset: Int,
    length: Int,
    output: ByteArray,
    outputOffset: Int,
    outputLength: Int
): ByteArray {
    val messageDigest = MessageDigest.getInstance("SHA-512")
    messageDigest.update(rawData, offset, length)
    messageDigest.digest(output, outputOffset, outputLength)
    return output
}
