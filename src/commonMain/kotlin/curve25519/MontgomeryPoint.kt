package curve25519

import kotlin.experimental.xor

/**
 * Scalar multiplication on the Montgomery form of Curve25519.
 *
 * To avoid notational confusion with the Edwards code, we use variables `( u, v )` for the Montgomery curve,
 * so that “Montgomery `(u)`” here corresponds to “Montgomery `(x)`” elsewhere.
 *
 * Montgomery arithmetic works not on the curve itself, but on the `(u)`-line,
 * which discards sign information and unifies the curve and its quadratic twist.
 * See [_Montgomery curves and their arithmetic_](https://eprint.iacr.org/2017/212.pdf) by Costello and Smith for more details.
 */
class MontgomeryPoint(
    val value: ByteArray = ByteArray(32)
) {
    /**
     * Convert this [MontgomeryPoint] to an array of bytes.
     */
    fun toBytes(bytes: ByteArray = ByteArray(32)) = bytes.apply {
        value.copyInto(bytes)
    }

    /**
     * Attempt to convert to an [EdwardsPoint], using the supplied choice of sign for the [EdwardsPoint].
     *
     * @param sign donating the desired sign of the resulting [EdwardsPoint]. 0 denotes positive and 1 negative.
     *
     * @return
     * - [EdwardsPoint] if `this` is the `(u)`-coordinate of a point on (the Montgomery form of) Curve25519;
     * - `null` if `this` is the `(u)`-coordinate of a point on the twist of (the Montgomery form of) Curve25519;
     */
    fun toEdwards(sign: Int): EdwardsPoint? {
        // To decompress the Montgomery u coordinate to an
        // `EdwardsPoint`, we apply the birational map to obtain the
        // Edwards y coordinate, then do Edwards decompression.
        //
        // The birational map is y = (u-1)/(u+1).
        //
        // The exceptional points are the zeros of the denominator,
        // i.e., u = -1.
        //
        // But when u = -1, v^2 = u*(u^2+486662*u+1) = 486660.
        //
        // Since this is nonsquare mod p, u = -1 corresponds to a point
        // on the twist, not the curve, so we can reject it early.
        val u = FieldElement.fromBytes(value)
        if (u == FieldElement.minusOne()) {
            return null
        }
        val one = FieldElement.one()
        val y = (u - one) * (u + one).invert()
        val yBytes = y.toBytes()
        yBytes[31] = yBytes[31] xor (sign shl 7).toByte()

        return CompressedEdwardsY(yBytes).decompress()
    }
}