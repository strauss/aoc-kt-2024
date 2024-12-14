package aoc_2023

import aoc_util.readInput2023
import kotlin.math.max

private val gameRegex = "Game (\\d+)".toRegex()
private val redRegex = "(\\d+) red".toRegex()
private val greenRegex = "(\\d+) green".toRegex()
private val blueRegex = "(\\d+) blue".toRegex()


fun main() {
    val testInput = readInput2023("Day02_test")
    val parsedTestInput = parseInput(testInput)
    val testResult = analyzeGames(parsedTestInput, 12, 13, 14)
    println("Test result: $testResult")
    val input = readInput2023("Day02")
    val parsedInput = parseInput(input)
    val result = analyzeGames(parsedInput, 12, 13, 14)
    println("Result: $result")
}

private fun analyzeGames(games: List<Game>, maxRed: Int, maxGreen: Int, maxBlue: Int): Pair<Int, Int> {
    var sum = 0
    var totalPower = 0
    for (game in games) {
        var possible = true
        var minRed = 0
        var minGreen = 0
        var minBlue = 0
        for ((red, green, blue) in game.records) {
            if (red > maxRed || green > maxGreen || blue > maxBlue) {
                possible = false
            }
            minRed = max(minRed, red)
            minGreen = max(minGreen, green)
            minBlue = max(minBlue, blue)
        }
        if (possible) {
            sum += game.id
        }
        val currentPower = minRed * minBlue * minGreen
//        println("Power(${game.id}) = $currentPower")
        totalPower += currentPower
    }
    return Pair(sum, totalPower)
}

private fun parseInput(input: List<String>): List<Game> {
    val out: MutableList<Game> = mutableListOf()
    for (line in input) {
        val firstSplit = line.split(':')

        val gameString = firstSplit[0].trim()
        val id: Int = gameRegex.matchEntire(gameString)?.groups?.get(1)?.value?.toInt() ?: -1

        val recordString = firstSplit[1].trim()
        val recordStringList = recordString.split(';')
        val parsedRecords = ArrayList<Record>()
        for (record in recordStringList) {
            val trimmedRecord = record.trim()
            val recordComponentList = trimmedRecord.split(',')
            var red = 0
            var green = 0
            var blue = 0
            for (component in recordComponentList) {
                val trimmedComponent = component.trim()
                red += redRegex.matchEntire(trimmedComponent)?.groups?.get(1)?.value?.toInt() ?: 0
                green += greenRegex.matchEntire(trimmedComponent)?.groups?.get(1)?.value?.toInt() ?: 0
                blue += blueRegex.matchEntire(trimmedComponent)?.groups?.get(1)?.value?.toInt() ?: 0
            }
            parsedRecords.add(Record(red, green, blue))
        }
        out.add(Game(id, parsedRecords))
    }
    return out
}

private data class Game(val id: Int, val records: List<Record>)
private data class Record(val red: Int, val green: Int, val blue: Int)