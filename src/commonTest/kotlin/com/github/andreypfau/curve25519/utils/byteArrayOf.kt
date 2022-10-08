package com.github.andreypfau.curve25519.utils

internal fun byteArrayOf(vararg bytes: Short) = bytes.map { it.toByte() }.toByteArray()
