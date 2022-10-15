package com.github.andreypfau.curve25519.constants

import com.github.andreypfau.curve25519.field.FieldElement
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantsTest {
    @Test
    fun testConstantsDVsRatio() {
        val a = FieldElement(121665u, 0u, 0u, 0u, 0u)
        a.negate(a)

        val bInv = FieldElement(121666u, 0u, 0u, 0u, 0u)
        bInv.invert(bInv)

        val d = FieldElement()
        val d2 = FieldElement()
        d.mul(a, bInv)
        d2.add(d, d)

        assertEquals(EDWARDS_D, d)
        assertEquals(EDWARDS_D2, d2)
    }
}
