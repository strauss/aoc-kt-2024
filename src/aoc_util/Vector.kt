package aoc_util

import java.math.BigInteger

data class Vector(val x: Double, val y: Double, val z: Double) {

    companion object {
        val ZERO = Vector(0.0, 0.0, 0.0)
    }

    operator fun plus(v: Vector): Vector = Vector(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vector): Vector = Vector(x - v.x, y - v.y, z - v.z)
    operator fun unaryMinus(): Vector = Vector(-x, -y, -z)
    operator fun times(v: Vector): Double = x * v.x + y * v.y + z * v.z
    operator fun times(s: Double): Vector = Vector(x * s, y * s, z * s)
    fun cross(w: Vector): Vector {
        val v = this
        return Vector(
            v.y * w.z - v.z * w.y,
            v.z * w.x - v.x * w.z,
            v.x * w.y - v.y * w.x
        )
    }

}

data class DiscreteVector(val x: BigInteger, val y: BigInteger, val z: BigInteger) {
    operator fun plus(v: DiscreteVector): DiscreteVector = DiscreteVector(x + v.x, y + v.y, z + v.y)
    operator fun minus(v: DiscreteVector): DiscreteVector = DiscreteVector(x - v.x, y - v.y, z - v.z)
    operator fun unaryMinus(): DiscreteVector = DiscreteVector(-x, -y, -z)
    operator fun times(v: DiscreteVector): BigInteger = x * v.x + y * v.y + z * v.z
    operator fun times(s: BigInteger): DiscreteVector = DiscreteVector(x * s, y * s, z * s)
    operator fun times(s: Double): Vector = Vector(x.toDouble() * s, y.toDouble() * s, z.toDouble() * s)
    fun cross(w: DiscreteVector): DiscreteVector {
        val v = this
        return DiscreteVector(
            v.y * w.z - v.z * w.y,
            v.z * w.x - v.x * w.z,
            v.x * w.y - v.y * w.x
        )
    }
}
