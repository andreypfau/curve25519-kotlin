package io.github.andreypfau.curve25519.internal

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA512

internal actual fun sha512(
    rawData: ByteArray,
    offset: Int,
    length: Int,
    output: ByteArray,
    outputOffset: Int,
    outputLength: Int
): ByteArray {
    if (length == 0) return output
    rawData.usePinned { rawDataPinned ->
        output.usePinned { outputPinned ->
            CC_SHA512(
                rawDataPinned.addressOf(offset),
                length.convert(),
                outputPinned.addressOf(outputOffset).reinterpret()
            )
        }
    }
    return output
}
