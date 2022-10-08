package com.github.andreypfau.curve25519.edwards

import com.github.andreypfau.curve25519.constants.EDWARD_BASEPOINT_TABLE
import com.github.andreypfau.curve25519.scalar.Scalar

object EdwardsBasepointTable {
    operator fun times(scalar: Scalar): EdwardsPoint {
        val a = scalar.toRadix16()
        val tables = EDWARD_BASEPOINT_TABLE
        var point = EdwardsPoint.IDENTITY
        for (i in 0..63) {
            if (i % 2 == 1) {
                point = (point + tables[i / 2][a[i]]).toExtended()
            }
        }
        point = point.mulByPow2(4)

        for (i in 0..63) {
            if (i % 2 == 0) {
                point = (point + tables[i / 2][a[i]]).toExtended()
            }
        }

        return point
    }
}
