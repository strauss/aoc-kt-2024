package aoc_2024

import aoc_util.CombinatorialIterator
import aoc_util.readInput2024
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import kotlin.math.min

private const val tokenACost = 3
private const val tokenBCost = 1

fun main() {
    val testList = readInput2024("Day13_test")
    val list = readInput2024("Day13")
    val testInput = parseInput(testList)
    val input = parseInput(list)


    println("Test Result        : ${evaluateMachines(testInput)} ")
    println("Test Result smartly: ${evaluateMachinesSmartly(testInput)}")
    println("Result: ${evaluateMachines(input)}")

    println("Test Result 2: ${evaluateMachinesSmartly(testInput, 10000000000000L)}")
//    println("Result 2: $result2")
}

private fun evaluateMachinesSmartly(machines: List<Machine>, correction: Long = 0L): Pair<Int, Long> {
    var minTotalCost = 0L
    var workingMachines = 0
    for (machine in machines) {
        var bPresses: Long = min((machine.pX.toLong() + correction) / machine.bX, (machine.pY.toLong() + correction) / machine.bY)
        var aPresses: Long = 0
        outer@ while (bPresses >= 0 && (getResultX(machine, aPresses, bPresses) < machine.pX + correction ||
                    getResultY(machine, aPresses, bPresses) < machine.pY + correction)
        ) {
            while (getResultX(machine, aPresses, bPresses) <= machine.pX &&
                getResultY(machine, aPresses, bPresses) <= machine.pY
            ) {
                aPresses += 1
                if (getResultX(machine, aPresses, bPresses) == machine.pX + correction &&
                    getResultY(machine, aPresses, bPresses) == machine.pY + correction
                ) {
                    minTotalCost += aPresses * tokenACost + bPresses * tokenBCost
                    workingMachines += 1
                    continue@outer
                }
            }
            while (getResultX(machine, aPresses, bPresses) >= machine.pX ||
                getResultY(machine, aPresses, bPresses) >= machine.pY
            ) {
                bPresses -= 1
                if (getResultX(machine, aPresses, bPresses) == machine.pX + correction &&
                    getResultY(machine, aPresses, bPresses) == machine.pY + correction
                ) {
                    minTotalCost += aPresses * tokenACost + bPresses * tokenBCost
                    workingMachines += 1
                    continue@outer
                }
            }
        }
    }
    return Pair(workingMachines, minTotalCost)
}

private fun getResultX(machine: Machine, aPresses: Long, bPresses: Long) = bPresses * machine.bX + aPresses * machine.aX

private fun getResultY(machine: Machine, aPresses: Long, bPresses: Long) = bPresses * machine.bY + aPresses * machine.aY


private fun evaluateMachines(machines: List<Machine>): Int {
    var minTotalCost = 0
    val buttonPresses = PrimitiveIntArrayList(100)
    for (i in 1..100) {
        buttonPresses.add(i)
    }
    for (machine in machines) {
        var minCostForMachine = Int.MAX_VALUE
        val iterator = CombinatorialIterator(buttonPresses, 2)
        iterator.iterate { buttonCombination ->
            val aPress = buttonCombination[0]
            val bPress = buttonCombination[1]
            val x = machine.aX * aPress + machine.bX * bPress
            val y = machine.aY * aPress + machine.bY * bPress
            if (x == machine.pX && y == machine.pY) {
                val currentCost = tokenACost * aPress + tokenBCost * bPress
                minCostForMachine = min(currentCost, minCostForMachine)
            }
        }
        if (minCostForMachine < Int.MAX_VALUE) {
            minTotalCost += minCostForMachine
        }
    }
    return minTotalCost
}

private val aRegex = "Button A: X\\+(\\d+), Y\\+(\\d+)".toRegex()
private val bRegex = "Button B: X\\+(\\d+), Y\\+(\\d+)".toRegex()
private val pRegex = "Prize: X=(\\d+), Y=(\\d+)".toRegex()

private fun parseInput(input: List<String>): List<Machine> {
    val out = mutableListOf<Machine>()
    var aX = 0
    var aY = 0
    var bX = 0
    var bY = 0
    var pX = 0
    var pY = 0
    for (line in input) {
        if (line.isBlank()) {
            out.add(Machine(aX, aY, bX, bY, pX, pY))
            aX = 0
            aY = 0
            bX = 0
            bY = 0
            pX = 0
            pY = 0
            continue
        }
        val aResult: List<String> = aRegex.matchEntire(line)?.groupValues ?: emptyList()
        val bResult: List<String> = bRegex.matchEntire(line)?.groupValues ?: emptyList()
        val pResult: List<String> = pRegex.matchEntire(line)?.groupValues ?: emptyList()
        if (aResult.isNotEmpty()) {
            aX = aResult[1].toInt()
            aY = aResult[2].toInt()
        } else if (bResult.isNotEmpty()) {
            bX = bResult[1].toInt()
            bY = bResult[2].toInt()
        } else if (pResult.isNotEmpty()) {
            pX = pResult[1].toInt()
            pY = pResult[2].toInt()
        }
    }
    out.add(Machine(aX, aY, bX, bY, pX, pY))
    return out
}

private data class Machine(val aX: Int, val aY: Int, val bX: Int, val bY: Int, val pX: Int, val pY: Int)