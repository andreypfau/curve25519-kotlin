package com.github.andreypfau.curve25519.x25519

import com.github.andreypfau.curve25519.constants.tables.ED25519_BASEPOINT_TABLE
import com.github.andreypfau.curve25519.ed25519.Ed25519PrivateKey
import com.github.andreypfau.curve25519.ed25519.Ed25519PublicKey
import com.github.andreypfau.curve25519.edwards.CompressedEdwardsY
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.internal.sha512
import com.github.andreypfau.curve25519.montgomery.MontgomeryPoint
import com.github.andreypfau.curve25519.scalar.Scalar
import kotlin.experimental.and
import kotlin.experimental.or

object X25519 {
    const val SCALAR_SIZE_BYTES = 32
    const val POINT_SIZE_BYTES = 32

    private val BASEPOINT = ByteArray(32).also {
        it[0] = 9
    }

//    fun x25519(
//        scalar: ByteArray,
//        point: ByteArray,
//        output: ByteArray = ByteArray(POINT_SIZE_BYTES),
//        offset: Int = 0
//    ): ByteArray {
//        if (point[0] == BASEPOINT[0]) {
//
//        }
//    }

    fun toX22519(
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

    fun toX25519(
        privateKey: Ed25519PrivateKey,
        output: ByteArray = ByteArray(SCALAR_SIZE_BYTES),
        offset: Int = 0
    ): ByteArray {
        val digest = sha512(privateKey.data)
        clampScalar(digest)
        digest.copyInto(output, offset)
        return output
    }

    private fun scalarBaseMult(
        scalar: ByteArray,
        output: ByteArray = ByteArray(SCALAR_SIZE_BYTES),
        offset: Int = 0
    ): ByteArray {
        val s = Scalar.fromByteArray(scalar)
        val edP = EdwardsPoint()
        edP.mulBasepoint(ED25519_BASEPOINT_TABLE, s)
        val montP = MontgomeryPoint.from(edP)
        montP.data.copyInto(output, offset)
        return output
    }

    private fun scalarMult(input: ByteArray, base: ByteArray, output: ByteArray, offset: Int = 0) {
        val s = Scalar.fromByteArray(input)
        val edP = EdwardsPoint.from(CompressedEdwardsY(base))
        val montP = MontgomeryPoint.from(edP)
        val montQ = MontgomeryPoint()
        montQ.mul(montP, s)
        montQ.data.copyInto(output, offset)
    }

    private fun clampScalar(scalar: ByteArray): ByteArray {
        scalar[0] = scalar[0] and 248.toByte()
        scalar[31] = scalar[31] and 127.toByte()
        scalar[31] = scalar[31] or 64.toByte()
        return scalar
    }
}
