@file:Suppress("OPT_IN_USAGE")

package io.github.andreypfau.curve25519.scalar

import kotlin.test.Test
import kotlin.test.assertContentEquals

class ScalarTest {
    // Note: x is 2^253-1 which is slightly larger than the largest scalar produced by
    // this implementation (l-1), and should show there are no overflows for valid scalars
    //
    // x = 14474011154664524427946373126085988481658748083205070504932198000989141204991
    // x = 7237005577332262213973186563042994240801631723825162898930247062703686954002 mod l
    // x = 3057150787695215392275360544382990118917283750546154083604586903220563173085*R mod l in Montgomery form
    val X = UnpackedScalar(
        0x000fffffffffffffu, 0x000fffffffffffffu, 0x000fffffffffffffu, 0x000fffffffffffffu,
        0x00001fffffffffffu
    )

    // y = 6145104759870991071742105800796537629880401874866217824609283457819451087098
    val Y = UnpackedScalar(
        0x000b75071e1458fau, 0x000bf9d75e1ecdacu, 0x000433d2baf0672bu, 0x0005fffcc11fad13u,
        0x00000d96018bb825u
    )

    // x*y = 36752150652102274958925982391442301741 mod l
    val XY = UnpackedScalar(
        0x000ee6d76ba7632du, 0x000ed50d71d84e02u, 0x00000000001ba634u, 0x0000000000000000u,
        0x0000000000000000u
    )

    // x*y = 658448296334113745583381664921721413881518248721417041768778176391714104386*R mod l in Montgomery form
    val XY_MONT = UnpackedScalar(
        0x0006d52bf200cfd5u, 0x00033fb1d7021570u, 0x000f201bc07139d8u, 0x0001267e3e49169eu,
        0x000007b839c00268u
    )

    @Test
    fun montgomeryMul() {
        val res = UnpackedScalar()
        res.montgomeryMul(X, Y)
        assertContentEquals(XY_MONT.data, res.data)
    }
}
