package io.github.andreypfau.curve25519.internal

import kotlin.random.Random

internal object ZeroRandom : Random() {
    override fun nextBits(bitCount: Int): Int = 0
}
