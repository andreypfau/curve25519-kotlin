package io.github.andreypfau.curve25519.constants.tables

import io.github.andreypfau.curve25519.models.AffineNielsPoint

internal val AFFINE_ODD_MULTIPLES_OF_BASEPOINT =
    AffineNielsPointNafLookupTable.unpack(PackedAffineOddMultiplesOfBasepoint)

class AffineNielsPointNafLookupTable(
    val table: Array<AffineNielsPoint> = Array(64) { AffineNielsPoint() }
) {
    fun lookup(x: Byte) = table[x / 2]

    companion object {
        fun unpack(packed: Array<ByteArray>): AffineNielsPointNafLookupTable {
            val table = AffineNielsPointNafLookupTable()
            packed.forEachIndexed { index, bytes ->
                table.table[index].setRawData(bytes)
            }
            return table
        }
    }
}
