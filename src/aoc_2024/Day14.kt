package aoc_2024

import aoc_util.readInput2024
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val testList = readInput2024("Day14_test")
    val list = readInput2024("Day14")
    val testInput = parseInput(testList)
    val input = parseInput(list)
    val testResut = determineSafetyFactor(100, testInput, 7, 11)
    val result = determineSafetyFactor(100, input, 103, 101)
    println("Test result: $testResut")
    println("Result: $result")
//    val interactiveResult = interactiveSimulation(input, 103, 101)
//    println("Result2: $interactiveResult")
    paintSimulation(input, 103, 101, 10000)
}

private fun paintSimulation(robots: List<Robot>, height: Int, width: Int, steps: Int) {
    var currentRobots = robots
    for (i in 0..steps) {
        val coordinateSet = HashSet<Pair<Int, Int>>()
        for (robot in currentRobots) {
            coordinateSet.add(Pair(robot.row, robot.col))
        }
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (row in 0..<height) {
            for (col in 0..<width) {
                if (coordinateSet.contains(Pair(row, col))) {
                    image.setRGB(col, row, 0xff5500)
                } else {
                    image.setRGB(col, row, 0x000000)
                }
            }
        }
        ImageIO.write(image, "PNG", File("out/2024/Day14/$i.png"))
        currentRobots = simulateMovement(currentRobots, 0, 1, height, width)
    }
}

private fun determineSafetyFactor(afterSeconds: Int, robots: List<Robot>, height: Int, width: Int): Int {
    val robotsAfterSimulation = simulateMovement(robots, 0, afterSeconds, height, width)
    val vMedian = height / 2
    val hMedian = width / 2
    var q1 = 0
    var q2 = 0
    var q3 = 0
    var q4 = 0
    for (robot in robotsAfterSimulation) {
        when {
            robot.row < vMedian && robot.col < hMedian -> q1 += 1
            robot.row < vMedian && robot.col > hMedian -> q2 += 1
            robot.row > vMedian && robot.col < hMedian -> q3 += 1
            robot.row > vMedian && robot.col > hMedian -> q4 += 1
//            else -> println("Ignored $robot")
        }
    }
    return q1 * q2 * q3 * q4
}

private fun interactiveSimulation(robots: List<Robot>, height: Int, width: Int): Int {
    println("Hit Enter for each step...")
    var line: String? = readlnOrNull()
    var currentRobots = robots
    var second = 0
    while (line != "q") {
        val coordinateSet = HashSet<Pair<Int, Int>>()
        for (robot in currentRobots) {
            coordinateSet.add(Pair(robot.row, robot.col))
        }
        println("------------------")
        println("Second $second")
        for (row in 0..<height) {
            for (col in 0..<width) {
                if (coordinateSet.contains(Pair(row, col))) {
                    print("#")
                } else {
                    print(".")
                }
            }
            println()
        }
        println()
        currentRobots = simulateMovement(currentRobots, 0, 1, height, width)
        second += 1
        println("If this is an easter egg, type 'q' and hit Enter ... otherwise just hit Enter.")
        line = readlnOrNull()
        println()
    }
    return second
}

private tailrec fun simulateMovement(robots: List<Robot>, second: Int, maxSeconds: Int, height: Int, width: Int): List<Robot> {
    if (second == maxSeconds) {
        return robots
    }
//    println(robots[10])
    val out = mutableListOf<Robot>()
    for (robot in robots) {
        var row = robot.row + robot.vVertical
        row = if (row >= height) row % height else if (row < 0) row + height else row
        var col = robot.col + robot.vHorizontal
        col = if (col >= width) col % width else if (col < 0) col + width else col
        out.add(Robot(row, col, robot.vVertical, robot.vHorizontal))
    }
    return simulateMovement(out, second + 1, maxSeconds, height, width)
}

private val parsingRegex = "p=(\\d+),(\\d+) v=(-?\\d+),(-?\\d+)".toRegex()

private fun parseInput(input: List<String>): List<Robot> {
    val out = mutableListOf<Robot>()
    for (line in input) {
        val result: MatchGroupCollection = parsingRegex.matchEntire(line)?.groups ?: continue
        val row = result[2]?.value?.toInt() ?: 0
        val col = result[1]?.value?.toInt() ?: 0
        val vVertical = result[4]?.value?.toInt() ?: 0
        val vHorizontal = result[3]?.value?.toInt() ?: 0
        val currentRobot = Robot(row, col, vVertical, vHorizontal)
        out.add(currentRobot)
    }
    return out
}

private data class Robot(val row: Int, val col: Int, val vVertical: Int, val vHorizontal: Int)