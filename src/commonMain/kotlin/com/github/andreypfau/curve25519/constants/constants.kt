package com.github.andreypfau.curve25519.constants

import com.github.andreypfau.curve25519.field.FieldElement

// Edwards `d` value, equal to `-121665/121666 mod p`.
internal val EDWARDS_D = FieldElement(
    929955233495203u,
    466365720129213u,
    1662059464998953u,
    2033849074728123u,
    1442794654840575u,
)

// Edwards `2*d` value, equal to `2*(-121665/121666) mod p`.
internal val EDWARDS_D2 = FieldElement(
    1859910466990425u,
    932731440258426u,
    1072319116312658u,
    1815898335770999u,
    633789495995903u,
)

// Precomputed value of one of the square roots of -1 (mod p).
internal val SQRT_M1 = FieldElement(
    1718705420411056u,
    234908883556509u,
    2233514472574048u,
    2117202627021982u,
    765476049583133u,
)
