@file:Suppress("OPT_IN_USAGE")

package curve25519.field

import encoding.binary.LittleEndian
import math.bits.UBigInt
import math.bits.mul64
import kotlin.experimental.or
import kotlin.jvm.JvmInline

// Element represents an element of the field GF(2^255-19). Note that this
// is not a cryptographically secure group, and should only be used to interact
// with edwards25519.Point coordinates.
//
// This type works similarly to math/big.Int, and all arguments and receivers
// are allowed to alias.
//
// The zero value is a valid zero element.
@JvmInline
value class FieldElement(val value: ULongArray = ULongArray(5)) : Iterable<ULong> {
    operator fun get(index: Int) = value[index]
    operator fun set(index: Int, ULong: ULong) {
        value[index] = ULong
    }

    override fun iterator(): Iterator<ULong> = value.iterator()

    companion object {
        fun one() = FieldElement(ulongArrayOf(1u, 0u, 0u, 0u, 0u))
    }
}

fun FieldElement(bytes: ByteArray) = FieldElement().apply { setBytes(bytes) }

internal val MASK_LOW_51_BITS = (1uL shl 51) - 1u

operator fun FieldElement.plus(b: FieldElement): FieldElement {
    val v = FieldElement()
    val a = this
    v[0] = a[0] + b[0]
    v[1] = a[1] + b[1]
    v[2] = a[2] + b[2]
    v[3] = a[3] + b[3]
    v[4] = a[4] + b[4]
    v.carryPropagate()
    return v
}

operator fun FieldElement.minus(b: FieldElement) = FieldElement().let { v ->
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
operator fun FieldElement.unaryMinus() = FieldElement() - this

fun FieldElement.inv() = let { v ->
    val z = this
    // Inversion is implemented as exponentiation with exponent p − 2. It uses the
    // same sequence of 255 squarings and 11 multiplications as [Curve25519].
    val z2 = FieldElement()
    val z9 = FieldElement()
    val z11 = FieldElement()
    val z2_5_0 = FieldElement()
    val z2_10_0 = FieldElement()
    val z2_20_0 = FieldElement()
    val z2_50_0 = FieldElement()
    val z2_100_0 = FieldElement()
    val t = FieldElement()

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

/**
 *  SetBytes sets v to x, where x is a 32-byte little-endian encoding.
 *
 *  Consistent with RFC 7748, the most significant bit (the high bit of the
 *  last byte) is ignored, and non-canonical values (2^255-19 through 2^255-1)
 *  are accepted. Note that this is laxer than specified by RFC 8032, but
 *  consistent with most Ed25519 implementations.
 *
 *  @throws IllegalArgumentException If x is not of the right length
 */
fun FieldElement.setBytes(x: ByteArray) {
    require(x.size == 32) { "invalid field element input size; expected: 32, actual: ${x.size}" }
    // Bits 0:51 (bytes 0:8, bits 0:64, shift 0, mask 51).
    value[0] = (LittleEndian.uLong(x, 0)) and MASK_LOW_51_BITS
    // Bits 51:102 (bytes 6:14, bits 48:112, shift 3, mask 51).
    value[1] = (LittleEndian.uLong(x, 6) shr 3) and MASK_LOW_51_BITS
    // Bits 102:153 (bytes 12:20, bits 96:160, shift 6, mask 51).
    value[2] = (LittleEndian.uLong(x, 12) shr 6) and MASK_LOW_51_BITS
    // Bits 153:204 (bytes 19:27, bits 152:216, shift 1, mask 51).
    value[3] = (LittleEndian.uLong(x, 19) shr 1) and MASK_LOW_51_BITS
    // Bits 204:255 (bytes 24:32, bits 192:256, shift 12, mask 51).
    // Note: not bytes 25:33, shift 4, to avoid overread.
    value[4] = (LittleEndian.uLong(x, 24) shr 12) and MASK_LOW_51_BITS
}

fun FieldElement.toBytes(out: ByteArray = ByteArray(32)): ByteArray {
    val t = FieldElement(value)
    t.reduce()

    val buf = ByteArray(8)
    t.forEachIndexed { i, l ->
        val bitsOffset = i * 51
        val byte = l shl (bitsOffset % 8)
        LittleEndian.putULong(buf, byte)
        buf.forEachIndexed { bi, bb ->
            val off = bitsOffset / 8 + bi
            if (off < out.size) {
                out[off] = (out[off]) or bb
            }
        }
    }

    return out
}

fun FieldElement.square() = FieldElement().also { v ->
    feSquare(v, this)
}

operator fun FieldElement.times(y: FieldElement): FieldElement = FieldElement().also { v ->
    feMul(v, this, y)
}

operator fun FieldElement.times(y: Int) = FieldElement().also { v ->
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
fun FieldElement.reduce() = apply {
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
    this[0] = this[0] and MASK_LOW_51_BITS
    this[2] += this[1] shr 51
    this[1] = this[1] and MASK_LOW_51_BITS
    this[3] += this[2] shr 51
    this[2] = this[2] and MASK_LOW_51_BITS
    this[4] += this[3] shr 51
    this[3] = this[3] and MASK_LOW_51_BITS
    // no additional carry
    this[4] = this[4] and MASK_LOW_51_BITS
}

internal fun mul51(a: ULong, b: Int): UBigInt {
    val (mh, ml) = a.mul64(b.toULong())
    val lo = ml and MASK_LOW_51_BITS
    val hi = (mh shl 13) or (ml shr 51)
    return lo to hi
}