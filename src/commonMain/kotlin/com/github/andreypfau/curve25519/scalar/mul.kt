package com.github.andreypfau.curve25519.scalar

import com.github.andreypfau.curve25519.UBigInt
import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.models.ProjectiveNielsPoint
import com.github.andreypfau.curve25519.models.ProjectivePoint

internal inline fun mul(point: EdwardsPoint, scalar: Scalar): EdwardsPoint {
    // Construct a lookup table of [P,2P,3P,4P,5P,6P,7P,8P]
    val lookupTable = ProjectiveNielsPoint.lookupTable(point)
    // Setting s = scalar, compute
    //
    //    s = s_0 + s_1*16^1 + ... + s_63*16^63,
    //
    // with `-8 ≤ s_i < 8` for `0 ≤ i < 63` and `-8 ≤ s_63 ≤ 8`.
    val scalarDigists = scalar.toRadix16()
    // Compute s*P as
    //
    //    s*P = P*(s_0 +   s_1*16^1 +   s_2*16^2 + ... +   s_63*16^63)
    //    s*P =  P*s_0 + P*s_1*16^1 + P*s_2*16^2 + ... + P*s_63*16^63
    //    s*P = P*s_0 + 16*(P*s_1 + 16*(P*s_2 + 16*( ... + P*s_63)...))
    //
    // We sum right-to-left.

    // Unwrap first loop iteration to save computing 16*identity
    var tmp2: ProjectivePoint
    var tmp3 = EdwardsPoint.IDENTITY
    var tmp1 = tmp3 + lookupTable[scalarDigists[63]]
    // Now tmp1 = s_63*P in P1xP1 coords
    for (i in 62 downTo 0) {
        tmp2 = tmp1.toProjective() // tmp2 =    (prev) in P2 coords
        tmp1 = tmp2.double()       // tmp1 =  2*(prev) in P1xP1 coords
        tmp2 = tmp1.toProjective() // tmp2 =  2*(prev) in P2 coords
        tmp1 = tmp2.double()       // tmp1 =  4*(prev) in P1xP1 coords
        tmp2 = tmp1.toProjective() // tmp2 =  4*(prev) in P2 coords
        tmp1 = tmp2.double()       // tmp1 =  8*(prev) in P1xP1 coords
        tmp2 = tmp1.toProjective() // tmp2 =  8*(prev) in P2 coords
        tmp1 = tmp2.double()       // tmp1 = 16*(prev) in P1xP1 coords
        tmp3 = tmp1.toExtended()   // tmp3 = 16*(prev) in P3 coords
        tmp1 = tmp3 + lookupTable[scalarDigists[i]]
        // Now tmp1 = s_i*P + 16*(prev) in P1xP1 coords
    }
    return tmp1.toExtended()
}

internal inline fun m(x: ULong, y: ULong): UBigInt = UBigInt(x) * UBigInt(y)
