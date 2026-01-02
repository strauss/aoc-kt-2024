package aoc_util

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

operator fun Int.times(v: Vector): Vector = Vector(v.x * this, v.y * this, v.z * this)