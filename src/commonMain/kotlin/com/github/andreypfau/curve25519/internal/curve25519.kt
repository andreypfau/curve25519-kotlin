@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.internal

internal inline fun curve25519Add(out: ULongArray, arg1: ULongArray, arg2: ULongArray) {
    out[0] = arg1[0] + arg2[0]
    out[1] = arg1[1] + arg2[1]
    out[2] = arg1[2] + arg2[2]
    out[3] = arg1[3] + arg2[3]
    out[4] = arg1[4] + arg2[4]
}

internal inline fun curve25519Carry(out1: ULongArray, arg1: ULongArray) {
    val x1 = (arg1[0])
    val x2 = ((x1 shr 51) + (arg1[1]))
    val x3 = ((x2 shr 51) + (arg1[2]))
    val x4 = ((x3 shr 51) + (arg1[3]))
    val x5 = ((x4 shr 51) + (arg1[4]))
    val x6 = ((x1 and 0x7ffffffffffffuL) + ((x5 shr 51) * 0x13uL))
    val x7 = ((((x6 shr 51))) + (x2 and 0x7ffffffffffffuL))
    val x8 = (x6 and 0x7ffffffffffffuL)
    val x9 = (x7 and 0x7ffffffffffffuL)
    val x10 = ((((x7 shr 51))) + (x3 and 0x7ffffffffffffuL))
    val x11 = (x4 and 0x7ffffffffffffuL)
    val x12 = (x5 and 0x7ffffffffffffuL)
    out1[0] = x8
    out1[1] = x9
    out1[2] = x10
    out1[3] = x11
    out1[4] = x12
}
