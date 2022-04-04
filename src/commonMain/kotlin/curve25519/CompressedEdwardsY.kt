@file:Suppress("OPT_IN_USAGE")

package curve25519

class CompressedEdwardsY constructor(
    val data: ByteArray = ByteArray(32),
) {
    fun decompress(): EdwardsPoint? {
        val y = FieldElement(data)
        val z = FieldElement.one()
        val yy = y.square()
        // u = y²-1
        val u = yy - z
        // v = dy²+1
        val v = (yy * EDWARDS_D) + z
        val (xx, isValidCoordY) = FieldElement.sqrtRatio(u, v)

        if (!isValidCoordY) return null
        val x = xx.select(-xx, (data[31].toInt() shr 7) != 0)
        val t = x * y
        return EdwardsPoint(x, y, z, t)
    }
}