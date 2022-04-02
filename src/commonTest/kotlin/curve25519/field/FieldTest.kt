@file:Suppress("OPT_IN_USAGE")

package curve25519.field

import cruve25519.field.*
import math.bits.addMul64
import math.bits.hi
import math.bits.length
import math.bits.lo
import kotlin.experimental.and
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FieldTest {
    @Test
    fun testMultiplyDistributesOverAdd() {
        repeat(1024) {
            multiplyDistributesOverAdd(
                x = FieldElement(Random.nextBytes(32)),
                y = FieldElement(Random.nextBytes(32)),
                z = FieldElement(Random.nextBytes(32)),
            )
        }
    }

    @Test
    fun testMul64to128() {
        var a = 5uL
        var b = 5uL
        var r = mul64(a, b)
        assertEquals(0x19u, r.lo)
        assertEquals(0u, r.hi, "lo-range wide mult failed, got ${r.lo} + ${r.hi}*(2^64)")

        a = 18014398509481983uL // 2^54 - 1
        b = 18014398509481983uL // 2^54 - 1
        r = mul64(a, b)
        assertEquals(0xff80000000000001uL, r.lo)
        assertEquals(0xfffffffffffuL, r.hi, "lo-range wide mult failed, got ${r.lo} + ${r.hi}*(2^64)")

        a = 1125899906842661u
        b = 2097155u
        r = mul64(a, b)
        r = addMul64(r, a, b)
        r = addMul64(r, a, b)
        r = addMul64(r, a, b)
        r = addMul64(r, a, b)
        assertEquals(16888498990613035u, r.lo, "wrong answer: ${r.lo} + ${r.hi}*(2^64)")
    }

    private fun multiplyDistributesOverAdd(x: FieldElement, y: FieldElement, z: FieldElement) {
        // Compute t1 = (x+y)*z
        var t1 = x + y
        t1 *= z

        // Compute t2 = x*z + y*z
        var t2 = x * z
        val t3 = y * z
        t2 += t3

        assertContentEquals(t1.toBytes(), t2.toBytes())
        assertTrue(t1.isInBounds())
        assertTrue(t2.isInBounds())
    }

    @Test
    fun TestSetBytesRoundTrip() {
        repeat(10) {
            val bytes = Random.nextBytes(32)
            val fieldElement = FieldElement()
            f1(bytes, fieldElement)
        }
    }

    private fun f1(input: ByteArray, fieldElement: FieldElement) {
        fieldElement.setBytes(input)

        // Mask the most significant bit as it's ignored by setBytes. (Now
        // instead of earlier so we check the masking in [setBytes] is working.)
        input[input.lastIndex] = input[input.lastIndex] and ((1 shl 7) - 1).toByte()
        val fieldBytes = fieldElement.toBytes()
        assertContentEquals(input, fieldBytes)
        assertTrue(fieldElement.isInBounds())
    }

    private fun f2(fieldElement: FieldElement, r: FieldElement) {
        r.setBytes(fieldElement.toBytes())

        // Intentionally not using Equal not to go through Bytes again.
        // Calling reduce because both Generate and SetBytes can produce
        // non-canonical representations.
        fieldElement.reduce()
        r.reduce()

        assertContentEquals(fieldElement, r)
    }

    private fun FieldElement.isInBounds() =
        this[0].length <= 52 &&
                this[1].length <= 52 &&
                this[2].length <= 52 &&
                this[3].length <= 52 &&
                this[4].length <= 52
}