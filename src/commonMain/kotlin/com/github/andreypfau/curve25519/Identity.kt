package com.github.andreypfau.curve25519

import com.github.andreypfau.kotlinio.crypto.ct.Choise
import com.github.andreypfau.kotlinio.crypto.ct.equals.ConstantTimeEqualable

interface Identity<T : Identity<T>> : ConstantTimeEqualable<T> {
    fun identity(): T
    fun isIdentity(): Boolean = ctEquals(identity()) == Choise.TRUE
}
