package aoc_2025

import aoc_util.extractInts
import aoc_util.readInput2025
import java.util.*
import kotlin.math.sqrt

fun main() {
    val testLines = readInput2025("Day08_test")
    val testPoints = parseInput(testLines)
    val testResult = connectionTest(testPoints, 10)
    println("Test result: $testResult")
    val testResult2 = connection(testPoints)
    println("Test 2 result: $testResult2")

    val lines = readInput2025("Day08")
    val points = parseInput(lines)
    val result = connectionTest(points, 1000)
    println("Result: $result")
    val result2 = connection(points)
    println("Result 2: $result2")
}

private fun connection(junctions: List<Point3D>): Long {
    val distances: Map<Pair<Point3D, Point3D>, Double> = buildMap {
        for (first in 0..<junctions.size - 1) {
            for (second in first + 1..<junctions.size) {
                put(junctions[first] to junctions[second], junctions[first].distance(junctions[second]))
            }
        }
    }
    val orderedByDistance = PriorityQueue<Map.Entry<Pair<Point3D, Point3D>, Double>>(Comparator.comparing { it.value })
    distances.forEach { orderedByDistance.add(it) }

    val connectionManagement = ConnectionManagement()
    val connectedPoints: MutableSet<Point3D> = HashSet()
    var last: Pair<Point3D, Point3D> = Point3D(0, 0, 0) to Point3D(0, 0, 0)
    while (connectedPoints.size < junctions.size && orderedByDistance.isNotEmpty()) {
        last = orderedByDistance.poll().key
        val (first, second) = last
        connectionManagement.connect(first, second)
        connectedPoints.add(first)
        connectedPoints.add(second)
    }
    return last.first.x.toLong() * last.second.x.toLong()
}


private fun connectionTest(junctions: List<Point3D>, connections: Int): Long {
    val distances: Map<Pair<Point3D, Point3D>, Double> = buildMap {
        for (first in 0..<junctions.size - 1) {
            for (second in first + 1..<junctions.size) {
                put(junctions[first] to junctions[second], junctions[first].distance(junctions[second]))
            }
        }
    }
    val orderedByDistance = PriorityQueue<Map.Entry<Pair<Point3D, Point3D>, Double>>(Comparator.comparing { it.value })
    distances.forEach { orderedByDistance.add(it) }

    val connectionManagement = ConnectionManagement()

    for (i in 1..connections) {
        val (first, second) = orderedByDistance.poll().key
        connectionManagement.connect(first, second)
    }
    val comparator = Comparator.comparing<Set<*>, Int> { it.size }
    val clusterQueue = PriorityQueue(comparator.reversed())
    connectionManagement.getClusterSequence().forEach { clusterQueue.add(it) }
    var result = 1L
    for (i in 1..3) {
        result *= clusterQueue.poll().size
    }
    return result
}

private fun parseInput(lines: List<String>): List<Point3D> {
    return buildList {
        for (line in lines) {
            val ints = line.extractInts()
            add(Point3D(ints[0], ints[1], ints[2]))
        }
    }
}

private class ConnectionManagement() {
    private val clusters: MutableList<MutableSet<Point3D>> = ArrayList()

    fun connect(first: Point3D, second: Point3D) {
        val firstCluster: MutableSet<Point3D>? = clusters.find { first in it }
        val secondCluster: MutableSet<Point3D>? = clusters.find { second in it }
        if (firstCluster != null && secondCluster != null) {
            if (firstCluster == secondCluster) {
                return
            }
            // fusion power
            firstCluster.addAll(secondCluster)
            clusters.remove(secondCluster)
        } else if (firstCluster != null && secondCluster == null) {
            firstCluster.add(second)
        } else if (firstCluster == null && secondCluster != null) {
            // second is not null
            secondCluster.add(first)
        } else { // both are null
            val newCluster = HashSet<Point3D>()
            newCluster.add(first)
            newCluster.add(second)
            clusters.add(newCluster)
        }
    }

    fun getClusterSequence() = clusters.asSequence()
}

private data class Point3D(val x: Int, val y: Int, val z: Int) {
    fun distance(other: Point3D): Double {
        val xx: Double = other.x.toDouble() - this.x
        val yy: Double = other.y.toDouble() - this.y
        val zz: Double = other.z.toDouble() - this.z
        return sqrt(xx * xx + yy * yy + zz * zz)
    }
}