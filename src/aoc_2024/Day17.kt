package aoc_2024

import aoc_util.*

fun main() {
    println("Computer?")
    val testInput = readInput2024("Day17_test")
    val testComputer = parseInput(testInput)
    val testOutput = testComputer.runProgram()
    println("Test output: ${renderOutput(testOutput)}")

    val testQuineInput = readInput2024("Day17_test_quine")
    val testQuineComputer = parseInput(testQuineInput)
    val testQuineResult = checkQuine(testQuineComputer)
    println("Test quine: $testQuineResult")

    println()

    val input = readInput2024("Day17")
    val computer = parseInput(input)
    val output = computer.runProgram()
    println("Output: ${renderOutput(output)}")

    println()
    println("Detecting secret")
    val octalSecret = detectSecret(computer)
    println("Secret: $octalSecret (${octalSecret.asOctalLong()})")
}

private fun detectSecret(computer: MiniComputer): String {
    val program = computer.program
    val programString = renderOutput(program)
    val limit = program.size
    val b = computer.registerB
    val c = computer.registerC
    println("Limit: $limit")
    val sb = StringBuilder()
    for (run in 1..limit) {
        val currentNumberString = sb.toString()
        for (i in 0L..<64L) {
            val workingNumber = currentNumberString + (i.asOctalString())
            computer.loadNewProgram(program, workingNumber.asOctalLong(), b, c)
            val currentResult = computer.runProgram()
            val renderOutput = renderOutput(currentResult)
            if (currentResult.size == workingNumber.length && programString.endsWith(renderOutput)) {
                println("Output: $renderOutput")
                sb.append(i.asOctalString())
                break
            }
        }
    }
    return sb.toString()
}

private fun checkQuine(computer: MiniComputer): Long {
    val program = computer.program
    val limit = program.size
    var currentA = 1L
    val b = computer.registerB
    val c = computer.registerC
    while (currentA > 0) {
        computer.loadNewProgram(program, currentA, b, c)
        val currentResult = computer.runProgram(limit)
        if (currentResult == program) {
            break
        }
        currentA += 1
    }
    return currentA
}

private fun parseInput(input: List<String>): MiniComputer {
    var registerA = 0L
    var registerB = 0L
    var registerC = 0L
    var program = emptyList<Int>()
    for (line in input) {
        when {
            line.contains("A") -> registerA = line.extractLongs().first()
            line.contains("B") -> registerB = line.extractLongs().first()
            line.contains("C") -> registerC = line.extractLongs().first()
            line.startsWith("Program") -> program = line.extractIntsWithLocation().map { it.number }
        }
    }
    return MiniComputer(registerA, registerB, registerC, program)
}

private fun renderOutput(output: List<Int>) = output.joinToString(",") { it.toString() }

private data class MiniComputer(var registerA: Long, var registerB: Long, var registerC: Long, var program: List<Int>) {
    private var programCounter = 0
    private val output: MutableList<Int> = mutableListOf()

    fun runProgram(outputLimit: Int = Int.MAX_VALUE): List<Int> {
        while (programCounter < program.size && output.size <= outputLimit) {
            val opCode = program[programCounter]
            val literalOperand: Long = program[programCounter + 1].toLong()
            when (opCode) {
                0 -> adv(comboOperand(literalOperand))
                1 -> bxl(literalOperand)
                2 -> bst(comboOperand(literalOperand))
                3 -> jnz(literalOperand)
                4 -> bxc()
                5 -> out(comboOperand(literalOperand))
                6 -> bdv(comboOperand(literalOperand))
                7 -> cdv(comboOperand(literalOperand))
            }
        }
        return output
    }

    fun loadNewProgram(newProgram: List<Int>, a: Long = 0, b: Long = 0, c: Long = 0) {
        registerA = a
        registerB = b
        registerC = c
        programCounter = 0
        output.clear()
        this.program = newProgram
    }

    private fun comboOperand(literalOperand: Long): Long {
        return when (literalOperand) {
            0L, 1L, 2L, 3L -> literalOperand
            4L -> registerA
            5L -> registerB
            6L -> registerC
            else -> error("Combo operand type $literalOperand not supported")
        }
    }

    // op 0
    private fun adv(comboOperand: Long) {
        val numerator = registerA
        val denominator = 1L shl comboOperand.toInt()
        registerA = numerator / denominator
        programCounter += 2
    }

    // op 1
    private fun bxl(literalOperand: Long) {
        registerB = registerB xor literalOperand
        programCounter += 2
    }

    // op 2
    private fun bst(comboOperand: Long) {
        registerB = comboOperand % 8
        programCounter += 2
    }

    // op 3
    private fun jnz(literalOperand: Long) {
        if (registerA == 0L) {
            programCounter += 2
            return
        }
        programCounter = literalOperand.toInt()
    }

    // op 4
    private fun bxc() {
        registerB = registerB xor registerC
        programCounter += 2
    }

    // op 5
    private fun out(comboOperand: Long) {
        output.add((comboOperand % 8L).toInt())
        programCounter += 2
    }

    // op 6
    private fun bdv(comboOperand: Long) {
        val numerator = registerA
        val denominator = 1 shl comboOperand.toInt()
        registerB = numerator / denominator
        programCounter += 2
    }

    // op 7
    private fun cdv(comboOperand: Long) {
        val numerator = registerA
        val denominator = 1 shl comboOperand.toInt()
        registerC = numerator / denominator
        programCounter += 2
    }
}