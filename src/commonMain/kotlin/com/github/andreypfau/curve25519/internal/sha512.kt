package com.github.andreypfau.curve25519.internal

import com.github.andreypfau.kotlinio.crypto.digest.Sha512
import com.github.andreypfau.kotlinio.pool.useInstance

internal fun sha512(byteArray: ByteArray): ByteArray = Sha512.POOL.useInstance {
    it.update(byteArray)
    it.digest()
}
