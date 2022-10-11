package com.github.andreypfau.curve25519.ed25519

class Ed25519PublicKey(
    val data: ByteArray
) {
    companion object {
        const val SIZE_BYTES = 32
    }
}
