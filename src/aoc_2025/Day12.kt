package aoc_2025

import aoc_util.extractInts
import aoc_util.readInput2025
import aoc_util.solve

fun main() {
    val testLines = readInput2025("Day12_test")
    val testInput = parseInput(testLines)
    solve("Test plausible", testInput, ::countPlausible)

    val lines = readInput2025("Day12")
    val input = parseInput(lines)
    solve("Plausible", input, ::countPlausible)
}

private fun countPlausible(input: List<Area>): Int {
    return input.count { it.plausible }
}

private enum class ParsingState {
    IDLE, PRESENT, AREA
}

private fun parseInput(lines: List<String>): List<Area> {
    var state = ParsingState.IDLE
    var currentPresentString: String? = null
    val presents = ArrayList<Present>()
    val areas = ArrayList<Area>()
    for (line in lines) {
        if (line.contains(':') && !line.contains('x')) {
            state = ParsingState.PRESENT
            currentPresentString = ""
        } else if (line.isBlank()) {
            if (state == ParsingState.PRESENT) {
                presents.add(Present(currentPresentString!!))
            }
            state = ParsingState.IDLE
            currentPresentString = null
        } else if (state == ParsingState.PRESENT) {
            currentPresentString += line
        } else if (state == ParsingState.AREA || (line.contains(':') && line.contains('x'))) {
            state = ParsingState.AREA
            val ints = line.extractInts()
            val width = ints[0]
            val height = ints[1]
            val requirements = IntArray(ints.size - 2)
            for (i in 2..<ints.size) {
                requirements[i - 2] = ints[i]
            }
            areas.add(Area(width, height, requirements, presents))
        }
    }
    return areas
}

private class Present(val raw: String) {
    val space: Int
        get() = raw.count { it == '#' }
}

private class Area(val width: Int, val height: Int, val requirements: IntArray, val available: List<Present>) {
    val size: Int
        get() = width * height
    val requiredSize: Int
        get() {
            var result: Int = 0
            for (i in requirements.indices) {
                result += requirements[i] * available[i].space
            }
            return result
        }
    val plausible: Boolean = size >= requiredSize

}