package io.github.andreypfau.curve25519.x25519

import io.github.andreypfau.curve25519.constants.tables.ED25519_BASEPOINT_TABLE
import io.github.andreypfau.curve25519.ed25519.Ed25519PrivateKey
import io.github.andreypfau.curve25519.ed25519.Ed25519PublicKey
import io.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import io.github.andreypfau.curve25519.edwards.EdwardsPoint
import io.github.andreypfau.curve25519.montgomery.MontgomeryPoint
import io.github.andreypfau.curve25519.scalar.Scalar
import io.github.andreypfau.kotlinx.crypto.Sha512
import io.github.andreypfau.kotlinx.crypto.subtle.constantTimeEquals
import kotlin.jvm.JvmStatic

object X25519 {
    const val SCALAR_SIZE_BYTES = 32
    const val POINT_SIZE_BYTES = 32

    private val BASEPOINT = ByteArray(32).also {
        it[0] = 9
    }

    @JvmStatic
    fun x25519(
        scalar: ByteArray,
        point: ByteArray = BASEPOINT,
        output: ByteArray = ByteArray(POINT_SIZE_BYTES),
        offset: Int = 0
    ): ByteArray {
        if (point.contentEquals(BASEPOINT)) {
            scalarBaseMult(scalar, output, offset)
        } else {
            scalarMult(scalar, point, output, offset)
            check(output.constantTimeEquals(ByteArray(POINT_SIZE_BYTES)) != 1) { "bad input point: low order point" }
        }
        return output
    }

    @JvmStatic
    fun toX25519(
        publicKey: Ed25519PublicKey,
        output: ByteArray = ByteArray(POINT_SIZE_BYTES),
        offset: Int = 0
    ): ByteArray {
        val aCompressed = CompressedEdwardsY(publicKey.data)
        val a = EdwardsPoint.from(aCompressed)
        val montA = MontgomeryPoint.from(a)
        montA.data.copyInto(output, offset)
        return output
    }

    @JvmStatic
    fun toX25519(
        privateKey: Ed25519PrivateKey,
        output: ByteArray = ByteArray(SCALAR_SIZE_BYTES),
        offset: Int = 0
    ): ByteArray {
        val hash = Sha512().update(privateKey.data, 0, 32).digest()
        clampScalar(hash)
        hash.copyInto(output, offset, 0, SCALAR_SIZE_BYTES)
        return output
    }

    @JvmStatic
    private fun scalarBaseMult(
        input: ByteArray,
        output: ByteArray = ByteArray(SCALAR_SIZE_BYTES),
        offset: Int = 0
    ): ByteArray {
        val ec = ByteArray(SCALAR_SIZE_BYTES)
        input.copyInto(ec)
        clampScalar(ec)

        val s = Scalar.fromByteArray(ec)
        val edP = EdwardsPoint().mulBasepoint(ED25519_BASEPOINT_TABLE, s)
        val montP = MontgomeryPoint.from(edP)
        montP.data.copyInto(output, offset)
        return output
    }

    @JvmStatic
    private fun scalarMult(
        input: ByteArray,
        base: ByteArray,
        output: ByteArray = ByteArray(SCALAR_SIZE_BYTES),
        offset: Int = 0
    ): ByteArray {
        val ec = ByteArray(SCALAR_SIZE_BYTES)
        input.copyInto(ec)
        clampScalar(ec)

        val s = Scalar.fromByteArray(ec)
        val montP = MontgomeryPoint(base)
        montP.mul(montP, s)

        montP.data.copyInto(output, offset)
        return output
    }

    private fun clampScalar(scalar: ByteArray): ByteArray {
        scalar[0] = (scalar[0].toInt() and 248).toByte()
        scalar[31] = (scalar[31].toInt() and 127).toByte()
        scalar[31] = (scalar[31].toInt() or 64).toByte()
        return scalar
    }
}
