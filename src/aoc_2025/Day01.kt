package aoc_2025

import aoc_util.readInput2025
import kotlin.math.abs

fun main() {
    val testInput = readInput2025("Day01_otest")
    val testValues = parseInput(testInput)
    println(stretchInput(testValues))
    println("Test results")
    val testResult1 = solve1(50, testValues)
    println("Test 1 result $testResult1")
    val testResult2 = solve2ThirdTry(50, testValues)
    println("Test 2 result $testResult2")
    println("Test 2 result ${solve2WithSimulation(50, testValues)}")

    println()

    val input = readInput2025("Day01")
    val values = parseInput(input)
    println("Results")
    val result1 = solve1(50, values)
    println("1st Result $result1")
    val result2 = solve2ThirdTry(50, values)
    println("2nd Result $result2")
    println("2nd Result ${solve2WithSimulation(50, values)}")

}

private fun solve2WithSimulation(start: Int, input: List<Int>): Int {
    val simulation = VaultSimulation(start)
    input.forEach { simulation.move(it) }
    return simulation.reachedZero
}

private class VaultSimulation(startPosition: Int) {
    private var position = startPosition % 100
    var reachedZero = 0
        private set

    fun move(by: Int) {
        if (by > 0) {
            var i = by
            while (i > 0) {
                position += 1
                if (position == 100) {
                    position = 0
                }
                if (position == 0) {
                    reachedZero += 1
                }
                i -= 1
            }
        } else if (by < 0) {
            var i = -by
            while (i > 0) {
                position -= 1
                if (position == -1) {
                    position = 99
                }
                if (position == 0) {
                    reachedZero += 1
                }
                i -= 1
            }
        }
    }
}

private fun stretchInput(input: List<Int>): List<Int> {
    return buildList {
        for (i in input) {
            if (i in -100..100) {
                add(i)
            } else {
                if (i < -100) {
                    var j = i
                    while (j < -100) {
                        add(-100)
                        j += 100
                    }
                    add(j)
                } else {
                    var j = i
                    while (j > 100) {
                        add(100)
                        j -= 100
                    }
                    add(j)
                }
            }
        }
    }
}

private fun solve2ThirdTry(start: Int, input: List<Int>): Int {
    val realInput = stretchInput(input)
    var current = start
    var result = 0
    for (i in realInput) {
        if (abs(i) == 100) {
            result += 1
        } else {
            current += i
            if (i > 0) {
                current %= 100
            }
            if (i < 0) {
                current += 100
            }
            if (current == 0) {
                result += 1
            }
        }
    }
    return result
}

private fun solve2ForReal(start: Int, input: List<Int>): Int {
    var current = start
    var result = 0

    for (i in input) {
        val sign: Int = if (i < 0) -1 else 1
        val abs: Int = abs(i)
        val additionals = abs / 100
        val ii = abs % 100
        if (sign == 1) {
            current += ii
            current %= 100
        } else {
            current -= ii
            if (current < 0) {
                current += 100
            }
        }
        if (current == 0 && abs % 100 != 0) {
            result += 1
        }
        result += additionals
    }
    return result
}

private fun solve2(start: Int, input: List<Int>): Int {
    var current = start
    var result = 0
    for (i in input) {
        current += i
        if (current >= 100) {
            while (current >= 100) {
                current -= 100
                result += 1
            }
        } else if (current < 0) {
            while (current < 0) {
                current += 100
                result += 1
            }
            if (current == 0) {
                result += 1
            }
        } else if (i < 0 && current == 0) {
            // we land exactly at 0
            result += 1
        }
    }
    return result
}

private fun solve1(start: Int, input: List<Int>): Int {
    var current = start
    var result = 0
    for (i in input) {
        val old = current
        current += i
        if (current >= 100) {
            current %= 100
        } else if (current < 0) {
            while (current < 0) {
                current += 100
            }
        }
        if (old != 0 && current == 0) {
            result += 1
        }
        if (old == current) {
            println(old)
        }
//        println(current)
    }
    return result
}

private fun parseInput(input: List<String>): List<Int> {
    return buildList {
        input.forEach {
            val i = it.replace('L', '-')
            val r = i.replace("R", "")
            val ri = r.toInt()
            add(ri)
        }
    }
}