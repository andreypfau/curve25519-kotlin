package com.github.andreypfau.curve25519.subtle

import com.github.andreypfau.curve25519.internal.constantTimeEquals
import com.github.andreypfau.curve25519.internal.constantTimeSelect
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantTimeTest {
    private val constantTimeByteEq = arrayOf(
        byteArrayOf(0, 0, 1),
        byteArrayOf(0, 1, 0),
        byteArrayOf(1, 0, 0),
        byteArrayOf(0xff.toByte(), 0xff.toByte(), 1),
        byteArrayOf(0xff.toByte(), 0xfe.toByte(), 0)
    )
    private val constantTimeIntEquals = arrayOf(
        intArrayOf(0, 0, 1),
        intArrayOf(0, 1, 0),
        intArrayOf(1, 0, 0),
        intArrayOf(Int.MAX_VALUE, Int.MAX_VALUE, 1),
        intArrayOf(Int.MIN_VALUE, Int.MAX_VALUE, 0),
    )

    @Test
    fun testConstantSelect() {
        val x = 10
        val y = 5
        assertEquals(x, 1.constantTimeSelect(x, y))
        assertEquals(y, 0.constantTimeSelect(x, y))
    }

    @Test
    fun testConstantTimeByteEquals() {
        constantTimeByteEq.forEach { test ->
            val (a, b, out) = test
            val r = a.constantTimeEquals(b)
            assertEquals(out.toInt(), r)
        }
    }

    @Test
    fun testConstantTimeIntEquals() {
        constantTimeIntEquals.forEach { test ->
            val (a, b, out) = test
            val r = a.constantTimeEquals(b)
            assertEquals(out, r)
        }
    }
}
