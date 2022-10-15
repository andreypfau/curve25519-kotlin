@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.curve25519.montgomery

import com.github.andreypfau.curve25519.field.FieldElement
import kotlin.jvm.JvmStatic

data class MontgomeryProjectivePoint(
    val u: FieldElement,
    val w: FieldElement
) {
    constructor() : this(FieldElement(), FieldElement())

    fun identity() = identity(this)

    fun conditionalSwap(other: MontgomeryProjectivePoint, choise: Int) {
        u.conditionalSwap(other.u, choise)
        w.conditionalSwap(other.w, choise)
    }

    companion object {
        @JvmStatic
        fun identity(
            output: MontgomeryProjectivePoint = MontgomeryProjectivePoint()
        ): MontgomeryProjectivePoint {
            output.u.one()
            output.w.zero()
            return output
        }
    }
}
