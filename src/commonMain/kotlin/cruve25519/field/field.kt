@file:Suppress("OPT_IN_USAGE")

package cruve25519.field

import cruve25519.ulong

// Element represents an element of the field GF(2^255-19). Note that this
// is not a cryptographically secure group, and should only be used to interact
// with edwards25519.Point coordinates.
//
// This type works similarly to math/big.Int, and all arguments and receivers
// are allowed to alias.
//
// The zero value is a valid zero element.
internal typealias FieldElement = ULongArray

internal fun feZero(): FieldElement = ulongArrayOf(0u, 0u, 0u, 0u, 0u)
internal fun feOne(): FieldElement = ulongArrayOf(1u, 0u, 0u, 0u, 0u)

internal val maskLow51Bits = (1uL shl 51) - 1uL

internal operator fun FieldElement.plus(b: FieldElement) = feZero().let { v ->
    val a = this
    v[0] = a[0] + b[0]
    v[1] = a[1] + b[1]
    v[2] = a[2] + b[2]
    v[3] = a[3] + b[3]
    v[4] = a[4] + b[4]
    v.carryPropagate()
}

internal operator fun FieldElement.minus(b: FieldElement) = feZero().let { v ->
    val a = this
    // We first add 2 * p, to guarantee the subtraction won't underflow, and
    // then subtract b (which can be up to 2^255 + 2^13 * 19).
    v[0] = (a[0] + 0xFFFFFFFFFFFDAuL) - b[0]
    v[1] = (a[1] + 0xFFFFFFFFFFFFEuL) - b[1]
    v[2] = (a[2] + 0xFFFFFFFFFFFFEuL) - b[2]
    v[3] = (a[3] + 0xFFFFFFFFFFFFEuL) - b[3]
    v[4] = (a[4] + 0xFFFFFFFFFFFFEuL) - b[4]
    v.carryPropagate()
}

// sets v = -a, and returns v.
internal operator fun FieldElement.unaryMinus() = feZero() - this

internal fun FieldElement.inv() = let { v ->
    val z = this
    // Inversion is implemented as exponentiation with exponent p − 2. It uses the
    // same sequence of 255 squarings and 11 multiplications as [Curve25519].
    val z2 = feZero()
    val z9 = feZero()
    val z11 = feZero()
    val z2_5_0 = feZero()
    val z2_10_0 = feZero()
    val z2_20_0 = feZero()
    val z2_50_0 = feZero()
    val z2_100_0 = feZero()
    val t = feZero()

    feSquare(z2, z) // 2
    feSquare(t, z2) // 4
    feSquare(t, t) // 8
    feMul(z9, t, z) // 9
    feMul(z11, z9, z2) // 11
    feSquare(t, z11) // 22
    feMul(z2_5_0, t, z9) // 31 = 2^5 - 2^0

    feSquare(t, z2_5_0) // 2^6 - 2^1
    repeat(5) {
        feSquare(t, t) // 2^10 - 2^5
    }
    feMul(z2_10_0, t, z2_5_0) // 2^10 - 2^0

    feSquare(t, z2_10_0) //  2^11 - 2^1
    repeat(10) {
        feSquare(t, t) // 2^20 - 2^10
    }
    feMul(z2_20_0, t, z2_10_0) // 2^20 - 2^0

    feSquare(t, z2_20_0) // 2^21 - 2^1
    repeat(20) {
        feSquare(t, t) // 2^40 - 2^20
    }
    feMul(t, t, z2_20_0) // 2^40 - 2^0

    feSquare(t, t) // 2^41 - 2^1
    repeat(10) {
        feSquare(t, t) // 2^50 - 2^10
    }
    feMul(z2_50_0, t, z2_10_0) // 2^50 - 2^0

    feSquare(t, z2_50_0) // 2^51 - 2^1
    repeat(50) {
        feSquare(t, t) // 2^100 - 2^50
    }
    feMul(z2_100_0, t, z2_50_0) // 2^100 - 2^0

    feSquare(t, z2_100_0) // 2^101 - 2^1
    repeat(100) {
        feSquare(t, t) // 2^200 - 2^100
    }
    feMul(t, t, z2_100_0) // 2^200 - 2^0

    feSquare(t, t) // 2^201 - 2^1
    repeat(50) {
        feSquare(t, t) // 2^250 - 2^50
    }
    feMul(t, t, z2_50_0) // 2^250 - 2^0

    feSquare(t, t) // 2^251 - 2^1
    feSquare(t, t)  // 2^252 - 2^2
    feSquare(t, t) // 2^253 - 2^3
    feSquare(t, t) // 2^254 - 2^4
    feSquare(t, t) // 2^255 - 2^5

    feMul(v, t, z11) // 2^255 - 21
}

internal fun FieldElement.setBytes(x: ByteArray) = also { v ->
    // Bits 0:51 (bytes 0:8, bits 0:64, shift 0, mask 51).
    v[0] = x.ulong(0..8)
    v[0] = v[0] and maskLow51Bits
    // Bits 51:102 (bytes 6:14, bits 48:112, shift 3, mask 51).
    v[1] = x.ulong(6..14) shr 3
    v[1] = v[1] and maskLow51Bits
    // Bits 102:153 (bytes 12:20, bits 96:160, shift 6, mask 51).
    v[2] = x.ulong(12..20) shr 6
    v[2] = v[2] and maskLow51Bits
    // Bits 153:204 (bytes 19:27, bits 152:216, shift 1, mask 51).
    v[3] = x.ulong(19..27) shr 1
    v[3] = v[3] and maskLow51Bits
    // Bits 204:255 (bytes 24:32, bits 192:256, shift 12, mask 51).
    // Note: not bytes 25:33, shift 4, to avoid overread.
    v[4] = x.ulong(24..32) shr 12
    v[4] = v[4] and maskLow51Bits
}

internal fun FieldElement.times(y: FieldElement): FieldElement = feZero().also { v ->
    feMul(v, this, y)
}

internal fun FieldElement.square() = feZero().also { v ->
    feSquare(v, this)
}

internal fun FieldElement.times(y: Int) = feZero().also { v ->
    val x = this
    val (x0lo, x0hi) = mul51(x[0], y)
    val (x1lo, x1hi) = mul51(x[1], y)
    val (x2lo, x2hi) = mul51(x[2], y)
    val (x3lo, x3hi) = mul51(x[3], y)
    val (x4lo, x4hi) = mul51(x[4], y)
    v[0] = x0lo + 19u * x4hi // carried over per the reduction identity
    v[1] = x1lo + x0hi
    v[2] = x2lo + x1hi
    v[3] = x3lo + x2hi
    v[4] = x4lo + x3hi
    // The hi portions are going to be only 32 bits, plus any previous excess,
    // so we can skip the carry propagation.
}

// reduces v modulo 2^255 - 19 and returns it.
internal fun FieldElement.reduce() = apply {
    carryPropagate()

    // After the light reduction we now have a field element representation
    // v < 2^255 + 2^13 * 19, but need v < 2^255 - 19.

    // If v >= 2^255 - 19, then v + 19 >= 2^255, which would overflow 2^255 - 1,
    // generating a carry. That is, c will be 0 if v < 2^255 - 19, and 1 otherwise.
    var c = (this[0] + 19u) shr 51
    c = (this[1] + c) shr 51
    c = (this[2] + c) shr 51
    c = (this[3] + c) shr 51
    c = (this[4] + c) shr 51

    // If v < 2^255 - 19 and c = 0, this will be a no-op. Otherwise, it's
    // effectively applying the reduction identity to the carry.
    this[0] += 19u * c

    this[1] += this[0] shr 51
    this[0] = this[0] and maskLow51Bits
    this[2] += this[1] shr 51
    this[1] = this[1] and maskLow51Bits
    this[3] += this[2] shr 51
    this[2] = this[2] and maskLow51Bits
    this[4] += this[3] shr 51
    this[3] = this[3] and maskLow51Bits
    // no additional carry
    this[4] = this[4] and maskLow51Bits
}

internal fun mul51(a: ULong, b: Int): UBigInt {
    val (mh, ml) = mul64(a, b.toULong())
    val lo = ml and maskLow51Bits
    val hi = (mh shl 13) or (ml shr 51)
    return lo to hi
}