@file:Suppress("OPT_IN_USAGE")

package curve25519

internal val LOW_51_BIT_MASK = (1uL shl 51) - 1u

/**
 * Edwards `d` value, equal to `-121665/121666 mod p`.
 */
internal val EDWARDS_D = FieldElement(
    ulongArrayOf(
        929955233495203u,
        466365720129213u,
        1662059464998953u,
        2033849074728123u,
        1442794654840575u,
    )
)

/**
 * 2^((p-1)/4), which squared is equal to -1 by Euler's Criterion.
 */
internal val SQRT_M1 = FieldElement(
    ulongArrayOf(
        1718705420411056u, 234908883556509u, 2233514472574048u, 2117202627021982u, 765476049583133u
    )
)

/**
 * APLUS2_OVER_FOUR is (A+2)/4. (This is used internally within the Montgomery ladder.)
 */
internal val APLUS2_OVER_FOUR = FieldElement(
    ulongArrayOf(
        121666u, 0u, 0u, 0u, 0u
    )
)