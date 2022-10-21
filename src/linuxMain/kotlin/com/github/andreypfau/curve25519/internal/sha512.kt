package com.github.andreypfau.curve25519.internal

internal actual fun sha512(
    rawData: ByteArray,
    offset: Int,
    length: Int,
    output: ByteArray,
    outputOffset: Int,
    outputLength: Int
): ByteArray {
    // TODO: Use the native Linux API to calculate SHA512
    val sha512 = Sha512Pure()
    sha512.update(rawData.copyOfRange(offset, offset + length).toUByteArray())
    sha512.digest().toByteArray().copyInto(output, outputOffset, 0, outputLength)
    return output
}
