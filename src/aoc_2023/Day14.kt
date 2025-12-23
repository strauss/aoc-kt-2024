package aoc_2023

import aoc_util.Primitive2DCharArray
import aoc_util.graph.BitSetAdjacencyBasedGraph
import aoc_util.readInput2023
import aoc_util.solve
import java.util.*

fun main() {
    val testLines = readInput2023("Day14_test")
    val testInput = Primitive2DCharArray.parseFromLines(testLines)
    solve("Test result", testInput, ::tiltAndEvaluate)
    solve("Test 2 result", testInput) { cycleAndEvaluate(it, 1_000_000_000) }

    val lines = readInput2023("Day14")
    val input = Primitive2DCharArray.parseFromLines(lines)
    solve("Result", input, ::tiltAndEvaluate)
    solve("Result 2", input) { cycleAndEvaluate(it, 1_000_000_000) }

}

private fun cycleAndEvaluate(array: Primitive2DCharArray, cycles: Int): Long {
    val cArray = array.copy()
    val startState = cArray.extractBitSet()
    val vertexIds = BitSet()
    val graph = BitSetAdjacencyBasedGraph<BitSet>(directed = true)
    val startId: Int = graph.introduceVertex(startState)
    vertexIds.set(startId)
    var currentState = startState
    var steps = cycles

    // run until done or a cycle is detected
    for (i in 1..cycles) {

        // we perform our cycle
        cArray.cycle()
        // we create a vertex in our graph and connect it with the previous one
        val nextState = cArray.extractBitSet()
        val nextId = graph.introduceVertex(nextState)
        graph.run {
            currentState.connect(nextState)
        }
        currentState = nextState // if a cycle is detected, this is our start point
        if (vertexIds[nextId]) {
            // cycle detected
            steps = i
            break
        }
        vertexIds[nextId] = true
    }

    val remaining = cycles - steps
    if (remaining == 0) {
        return cArray.evaluate()
    }

    // now we calculate the cycle length
    var cycleLength = 0
    graph.run {
        val startVertex = currentState
        var currentVertex = startVertex
        do {
            currentVertex = currentVertex.adjacencies().next() // we assume there is only one
            cycleLength += 1
        } while (currentVertex != startVertex)
    }

    println("Cycle length = $cycleLength")
    val actualRemaining = remaining % cycleLength
    println("Reducing steps from $remaining to $actualRemaining")

    for (i in 1..actualRemaining) {
        // now we perform the remaining cycles
        cArray.cycle()
    }

    return cArray.evaluate()
}

private fun tiltAndEvaluate(array: Primitive2DCharArray): Long {
    val cArray = array.copy()
    cArray.tiltUp()
    return cArray.evaluate()
}

private const val ROCK = 'O'
private const val SPACE = '.'

private fun Primitive2DCharArray.extractBitSet(): BitSet {
    val out = BitSet(this.width * this.height)
    val iterator = iterator()
    var idx = 0
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (next == ROCK) {
            out.set(idx)
        }
        idx += 1
    }
    return out
}

private fun Primitive2DCharArray.cycle() {
    tiltUp()
    tiltLeft()
    tiltDown()
    tiltRight()
}

private fun Primitive2DCharArray.tiltUp() {
    for (row in 1..<this.height) {
        nextCol@ for (col in 0..<this.width) {
            val current = this[row, col]
            if (current != ROCK) {
                continue@nextCol
            }
            var rUp = row - 1
            moveUp@ while (rUp >= 0) {
                val above = this[rUp, col]
                if (above != SPACE) {
                    break@moveUp
                }
                this[rUp, col] = ROCK
                this[rUp + 1, col] = SPACE
                rUp -= 1
            }
        }
    }
}

private fun Primitive2DCharArray.tiltLeft() {
    for (col in 1..<this.width) {
        nextRow@ for (row in 0..<this.height) {
            val current = this[row, col]
            if (current != ROCK) {
                continue@nextRow
            }
            var cLeft = col - 1
            moveLeft@ while (cLeft >= 0) {
                val left = this[row, cLeft]
                if (left != SPACE) {
                    break@moveLeft
                }
                this[row, cLeft] = ROCK
                this[row, cLeft + 1] = SPACE
                cLeft -= 1
            }
        }
    }
}

private fun Primitive2DCharArray.tiltRight() {
    for (col in this.width - 2 downTo 0) {
        nextRow@ for (row in 0..<this.height) {
            val current = this[row, col]
            if (current != ROCK) {
                continue@nextRow
            }
            var cRight = col + 1
            moveRight@ while (cRight < this.width) {
                val right = this[row, cRight]
                if (right != SPACE) {
                    break@moveRight
                }
                this[row, cRight] = ROCK
                this[row, cRight - 1] = SPACE
                cRight += 1
            }
        }
    }
}

private fun Primitive2DCharArray.tiltDown() {
    for (row in this.height - 2 downTo 0) {
        nextCol@ for (col in 0..<this.width) {
            val current = this[row, col]
            if (current != ROCK) {
                continue@nextCol
            }
            var rDown = row + 1
            moveDown@ while (rDown < this.height) {
                val below = this[rDown, col]
                if (below != SPACE) {
                    break@moveDown
                }
                this[rDown, col] = ROCK
                this[rDown - 1, col] = SPACE
                rDown += 1
            }
        }
    }
}

private fun Primitive2DCharArray.evaluate(): Long {
    var result = 0L
    for (row in 0..<this.height) {
        val multiplyBy = height - row
        val rowContent = getRow(row)
        result += multiplyBy * rowContent.count { it == ROCK }
    }
    return result
}