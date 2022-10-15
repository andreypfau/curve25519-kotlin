package com.github.andreypfau.curve25519.internal

import com.github.andreypfau.kotlinio.crypto.digest.Sha512
import com.github.andreypfau.kotlinio.pool.useInstance

internal fun sha512(
    rawData: ByteArray, offset: Int = 0, length: Int = rawData.size - offset,
    output: ByteArray = ByteArray(64), outputOffset: Int = 0, outputLength: Int = 64
): ByteArray =
    Sha512.POOL.useInstance {
        it.update(rawData, offset, length)
        it.digest(output, outputOffset, outputLength)
    }
