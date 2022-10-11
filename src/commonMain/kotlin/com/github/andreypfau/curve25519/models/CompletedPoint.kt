package com.github.andreypfau.curve25519.models

import com.github.andreypfau.curve25519.edwards.EdwardsPoint
import com.github.andreypfau.curve25519.field.FieldElement

data class CompletedPoint(
    val x: FieldElement,
    val y: FieldElement,
    val z: FieldElement,
    val t: FieldElement,
) {
    constructor() : this(FieldElement(), FieldElement(), FieldElement(), FieldElement())

    fun double(pp: ProjectivePoint): CompletedPoint = apply {
        val xx = FieldElement()
        val yy = FieldElement()
        val zz2 = FieldElement()
        val xPlusYsq = FieldElement()
        xx.square(pp.x)
        yy.square(pp.y)
        zz2.square2(pp.z)
        xPlusYsq.add(pp.x, pp.y) // X+Y
        xPlusYsq.square(xPlusYsq) // (X+Y)^2

        y.add(yy, xx)
        x.sub(xPlusYsq, y)
        z.sub(yy, xx)
        t.sub(zz2, z)
    }

    fun add(a: EdwardsPoint, b: ProjectiveNielsPoint): CompletedPoint = apply {
        val pp = FieldElement()
        val mm = FieldElement()
        val tt2d = FieldElement()
        val zz = FieldElement()
        val zz2 = FieldElement()
        pp.add(a.y, a.x) // a.Y + a.X
        pp.mul(pp, b.yPlusX) // (a.Y + a.X) * b.Y_plus_X
        mm.sub(a.y, a.x) // a.Y - a.X
        mm.mul(mm, b.yMinusX) // (a.Y - a.X) * b.Y_minus_X
        tt2d.mul(a.t, b.t2d)
        zz.mul(a.z, b.z)
        zz2.add(zz, zz)
        x.sub(pp, mm)
        y.add(pp, mm)
        z.add(zz2, tt2d)
        t.sub(zz2, tt2d)
    }

    fun sub(a: EdwardsPoint, b: ProjectiveNielsPoint): CompletedPoint = apply {
        val pm = FieldElement()
        val mp = FieldElement()
        val tt2d = FieldElement()
        val zz = FieldElement()
        val zz2 = FieldElement()
        pm.add(a.y, a.x)
        pm.mul(pm, b.yMinusX)
        mp.sub(a.y, a.x)
        mp.mul(mp, b.yPlusX)
        tt2d.mul(a.t, b.t2d)
        zz.mul(a.z, b.z)
        zz2.add(zz, zz)

        x.sub(pm, mp)
        y.add(pm, mp)
        z.sub(zz2, tt2d)
        t.add(zz2, tt2d)
    }

    fun add(a: EdwardsPoint, b: AffineNielsPoint): CompletedPoint = apply {
        val pp = FieldElement()
        val mm = FieldElement()
        val txy2d = FieldElement()
        val z2 = FieldElement()
        pp.add(a.y, a.x)
        pp.mul(pp, b.yPlusX)
        mm.sub(a.y, a.x)
        mm.mul(mm, b.yMinusX)
        txy2d.mul(a.t, b.xy2d)
        z2.add(a.z, a.z)

        x.sub(pp, mm)
        y.add(pp, mm)
        z.add(z2, txy2d)
        t.sub(z2, txy2d)
    }

    fun add(a: CompletedPoint, b: AffineNielsPoint): CompletedPoint = apply {
        val aTmp = EdwardsPoint()
        add(aTmp.set(a), b)
    }

    fun sub(a: EdwardsPoint, b: AffineNielsPoint): CompletedPoint = apply {
        val yPlusX = FieldElement()
        val yMinusX = FieldElement()
        val pm = FieldElement()
        val mp = FieldElement()
        val txy2d = FieldElement()
        val z2 = FieldElement()
        yPlusX.add(a.y, a.x)
        yMinusX.sub(a.y, a.x)
        pm.mul(yPlusX, b.yMinusX)
        mp.mul(yMinusX, b.yPlusX)
        txy2d.mul(a.t, b.xy2d)
        z2.add(a.z, a.z)

        x.sub(pm, mp)
        y.add(pm, mp)
        z.sub(z2, txy2d)
        t.add(z2, txy2d)
    }

    fun sub(a: CompletedPoint, b: AffineNielsPoint): CompletedPoint = apply {
        val aTmp = EdwardsPoint()
        sub(aTmp.set(a), b)
    }
}
