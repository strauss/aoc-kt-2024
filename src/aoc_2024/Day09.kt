package aoc_2024

import aoc_util.readInput2024
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList

fun main() {
    val testList = readInput2024("Day09_test")
    val testInput = parseInput(testList)
    val testResult = solve1(testInput)
    val testResult2 = solve2(testInput)
    println("Test result 1: $testResult")
    println("Test result 2: $testResult2")

    val realList = readInput2024("Day09")
    val realInput = parseInput(realList)
    val realResult = solve1(realInput)
    val realResult2 = 0 // solve2(realInput)
    println("Real result 1: $realResult")
    println("Real result 2: $realResult2")

}

private const val FREE: Int = -1

fun solve1(input: List<Int>): Long {
    val blockList = createBlockList(input)
    defragBlocks(blockList)
    val result = checksum(blockList)
    return result
}

fun solve2(input: List<Int>): Long {
    val blockList = createBlockList(input)
    defragFiles(blockList)
    val result = checksum(blockList)
    return result
}

private fun checksum(input: List<Int>): Long {
    var checksum = 0L
    for (i in 0..input.lastIndex) {
        val element = input[i]
        if (element != FREE) {
            checksum += element * i
        }
    }
    return checksum
}

private fun defragFiles(input: MutableList<Int>) {
    var lastFilled = input.lastIndex
    while (input[lastFilled] == FREE) {
        lastFilled -= 1
    }
    var currentFile = input[lastFilled]
//    val movedFiles: MutableSet<Int> = PrimitiveIntSetB()
//    movedFiles.add(0)
    while (currentFile > 0) {
        val fileSize = getFileSize(input, lastFilled)
        var firstFree = 0
        var fileMoved = false
        inner@ while (firstFree < input.size) {
            while (input[firstFree] != FREE) {
                firstFree += 1
            }
            val freeSize = getFreeSize(input, firstFree)
            if (fileSize <= freeSize) {
//                movedFiles.add(input[lastFilled])
                for (i in 0..<fileSize) {
                    input[firstFree] = input[lastFilled]
                    input[lastFilled] = FREE
                    firstFree += 1
                    lastFilled -= 1
                }
                while (input[firstFree] != FREE) {
                    firstFree += 1
                }
                fileMoved = true
                break@inner
            } else {
                firstFree += 1
                while (firstFree < input.size && input[firstFree] != FREE) {
                    firstFree += 1
                }
            }
        }
        currentFile -= 1
        while (
//            lastFilled >= 0 &&
            input[lastFilled] != currentFile) {// && movedFiles.contains(input[lastFilled])) {
            lastFilled -= 1
        }
    }
}

private fun getFreeSize(input: List<Int>, index: Int): Int {
    var i = 0
    while (index + i < input.size && input[index + i] == FREE) {
        i += 1
    }
    return i
}

private fun getFileSize(input: List<Int>, index: Int): Int {
    var i = 0
    val fileId = input[index]
    while (index - i >= 0 && input[index - i] != FREE && fileId == input[index - i]) {
        i += 1
    }
    return i
}

private fun defragBlocks(input: MutableList<Int>) {
    var lastFilled = input.lastIndex
    while (input[lastFilled] == FREE) {
        lastFilled -= 1
    }
    var i = 0
    while (i < lastFilled) {
        val current = input[i]
        if (current == FREE) {
            input[i] = input[lastFilled]
            input[lastFilled] = FREE
            while (input[lastFilled] == FREE) {
                lastFilled -= 1
            }
        }
        i += 1
    }
}

private fun createBlockList(input: List<Int>): MutableList<Int> {
    val blockList: MutableList<Int> = PrimitiveIntArrayList()
    var id = 0
    var free = false
    input.forEach {
        for (i in 1..it) {
            if (free) {
                blockList.add(FREE)
            } else {
                blockList.add(id)
            }
        }
        if (!free) {
            id += 1
        }
        free = !free
    }
    return blockList
}

private fun parseInput(input: List<String>): List<Int> {
    val outList: MutableList<Int> = PrimitiveIntArrayList()
    input.forEach { line ->
        for (c in line) {
            outList.add(c.toString().toInt())
        }
    }
    return outList
}