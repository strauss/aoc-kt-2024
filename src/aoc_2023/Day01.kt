package aoc_2023

import aoc_util.readInput2023
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList

fun main() {
    val testList = readInput2023("Day01_test1")
    println("Test Result: ${getCalibration(testList)}")
    val list = readInput2023("Day01")
    println("Result: ${getCalibration(list)}")
    val testList2 = readInput2023("Day01_test2")
    println("Test Result 2: ${getCalibrationWithNumberWords(testList2)}")
    println("Result 2: ${getCalibrationWithNumberWords(list)}")

}

private fun getCalibration(input: List<String>): Int {
    var sum = 0
    input.forEach { line ->
        val firstDigit: Int = line.find { it.isDigit() }.toString().toInt()
        val lastDigit: Int = line.findLast { it.isDigit() }.toString().toInt()
        sum += firstDigit * 10 + lastDigit
    }
    return sum
}

private fun getCalibrationWithNumberWords(input: List<String>): Int {
    var sum = 0
    input.forEach { line ->
        val indexOfFirstDigit = line.indexOfFirst { it.isDigit() }
        val indexOfLastDigit = line.indexOfLast { it.isDigit() }
        val indexOfFirstNumberWord = getIndexOfFirstNumberWord(line)
        val indexOfLastNumberWord = getIndexOfLastNumberWord(line)

        val firstDigit: Int = if (indexOfFirstDigit >= 0 && (indexOfFirstNumberWord == -1 || indexOfFirstDigit < indexOfFirstNumberWord)) {
            line[indexOfFirstDigit].toString().toInt()
        } else {
            getDigitForNumberWordAtIndex(line, indexOfFirstNumberWord)
        }
        val secondDigit: Int = if (indexOfLastDigit > indexOfLastNumberWord) {
            line[indexOfLastDigit].toString().toInt()
        } else {
            getDigitForNumberWordAtIndex(line, indexOfLastNumberWord)
        }

        sum += firstDigit * 10 + secondDigit
    }
    return sum
}

private fun getDigitForNumberWordAtIndex(line: String, index: Int): Int {
    val sub = line.substring(index..<line.length)
    return when {
        sub.startsWith("one") -> 1
        sub.startsWith("two") -> 2
        sub.startsWith("three") -> 3
        sub.startsWith("four") -> 4
        sub.startsWith("five") -> 5
        sub.startsWith("six") -> 6
        sub.startsWith("seven") -> 7
        sub.startsWith("eight") -> 8
        sub.startsWith("nine") -> 9
        else -> -1
    }
}

private fun getIndexOfFirstNumberWord(line: String): Int {
    val list = PrimitiveIntArrayList()
    list.add(line.indexOf("one"))
    list.add(line.indexOf("two"))
    list.add(line.indexOf("three"))
    list.add(line.indexOf("four"))
    list.add(line.indexOf("five"))
    list.add(line.indexOf("six"))
    list.add(line.indexOf("seven"))
    list.add(line.indexOf("eight"))
    list.add(line.indexOf("nine"))
    val all = list.filter { it >= 0 }
    return if (all.isEmpty()) -1 else all.min()
}

private fun getIndexOfLastNumberWord(line: String): Int {
    val list = PrimitiveIntArrayList()
    list.add(line.lastIndexOf("one"))
    list.add(line.lastIndexOf("two"))
    list.add(line.lastIndexOf("three"))
    list.add(line.lastIndexOf("four"))
    list.add(line.lastIndexOf("five"))
    list.add(line.lastIndexOf("six"))
    list.add(line.lastIndexOf("seven"))
    list.add(line.lastIndexOf("eight"))
    list.add(line.lastIndexOf("nine"))
    val all = list.filter { it >= 0 }
    return if (all.isEmpty()) -1 else all.max()
}