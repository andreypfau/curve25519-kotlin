@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.internal

import kotlinx.cinterop.*
import platform.posix.open
import platform.posix.read
import platform.windows.*

// TODO: Use the native Windows API to calculate SHA512
internal actual fun sha512(
    rawData: ByteArray,
    offset: Int,
    length: Int,
    output: ByteArray,
    outputOffset: Int,
    outputLength: Int
): ByteArray {
    val sha512 = Sha512Pure()
    sha512.update(rawData.copyOfRange(offset, offset + length).toUByteArray())
    sha512.digest().toByteArray().copyInto(output, outputOffset, 0, outputLength)
    return output
}
