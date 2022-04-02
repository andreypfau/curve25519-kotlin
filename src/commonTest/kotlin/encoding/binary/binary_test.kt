package encoding.binary

import kotlin.test.Test
import kotlin.test.assertEquals

class BynaryTest {
    val little = byteArrayOf(
        1,
        3, 2,
        7, 6, 5, 4,
        15, 14, 13, 12, 11, 10, 9, 8,
        16,
        18, 17,
        22, 21, 20, 19,
        30, 29, 28, 27, 26, 25, 24, 23,

        34, 33, 32, 31,
        42, 41, 40, 39, 38, 37, 36, 35,
        46, 45, 44, 43, 50, 49, 48, 47,
        58, 57, 56, 55, 54, 53, 52, 51, 66, 65, 64, 63, 62, 61, 60, 59,

        67, 68, 69, 70,

        1,
        1, 0, 1, 0,
    )

    @Test
    fun testLittleEndian() {
        testOrder(LittleEndian, little)
    }

    fun testOrder(order: ByteOrder, input: ByteArray, output: ByteArray = ByteArray(input.size)) {
        var offset = 0
        repeat(input.size - ULong.SIZE_BYTES) {
            val l1 = order.uLong(input, offset)
            order.putULong(output, l1, offset)
            order.uLong(output, offset)
            val l2 = order.uLong(input, offset)
            assertEquals(l1, l2)
            offset += 1
        }
    }
}