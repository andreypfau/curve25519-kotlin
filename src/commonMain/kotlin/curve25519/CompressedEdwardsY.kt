@file:Suppress("OPT_IN_USAGE")

package curve25519

class CompressedEdwardsY constructor(
    val data: ByteArray = ByteArray(32)
) {

    fun decompress(): EdwardsPoint? {
        val y = FieldElement.fromByteArray(data)
        val z = FieldElement.one()
        val yy = y.square()
        // u = y²-1
        val u = yy - z
        // v = dy²+1
        val v = (yy * EDWARDS_D) + z
        val (x, isValidCoordY) = FieldElement.sqrtRatio(u, v)

        if (!isValidCoordY) return null

        return EdwardsPoint(
            x = x.select(-x, (data[31].toInt() shr 7) != 0),
            y = y,
            z = z,
            t = x * y
        )
    }

    companion object {
        /**
         * Edwards `d` value, equal to `-121665/121666 mod p`.
         */
        val EDWARDS_D = FieldElement(
            ulongArrayOf(
                929955233495203u,
                466365720129213u,
                1662059464998953u,
                2033849074728123u,
                1442794654840575u,
            )
        )

        fun fromBytes(data: ByteArray) = CompressedEdwardsY(data.copyOf())
    }
}