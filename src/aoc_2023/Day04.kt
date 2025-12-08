package aoc_2023

import aoc_util.extractIntsWithLocation
import aoc_util.readInput2023
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder

fun main() {
    val testInput = readInput2023("Day04_test")
    val parsedTestInput = parseInput(testInput)
    val testResult = countPoints(parsedTestInput)
    println("Test result: $testResult")
    val testGameResult = actuallyPlayTheGame(parsedTestInput)
    println("Test game result: $testGameResult")

    val input = readInput2023("Day04")
    val parsedInput = parseInput(input)
    val result = countPoints(parsedInput)
    println("Result: $result")
    val gameResult = actuallyPlayTheGame(parsedInput)
    println("Game result: $gameResult")

}

private fun actuallyPlayTheGame(cardPile: List<Card>): Int {
    val cardCount: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    for (card in cardPile) {
        cardCount[card.id] = (cardCount[card.id] ?: 0) + 1
    }
    for ((id, winningNumbers, drawnNumbers) in cardPile) {
        val copies: Int = cardCount[id] ?: 0
        var hits = 0
        for (number in drawnNumbers) {
            if (number in winningNumbers) {
                hits += 1
            }
        }
        for (deltaId in 1..hits) {
            val copyId = id + deltaId
            if (cardCount.containsKey(copyId)) {
                cardCount[copyId] = (cardCount[copyId] ?: 0) + copies
            }
        }
    }
    return cardCount.values.sum()
}

private fun countPoints(cardPile: List<Card>): Int {
    var points = 0
    for (card in cardPile) {
        val (_, winningNumbers, drawnNumbers) = card
        var hits = 0
        for (number in drawnNumbers) {
            if (number in winningNumbers) {
                hits += 1
            }
        }
        if (hits != 0) {
            points += 1 shl (hits - 1)
        }
    }
    return points
}

private fun parseInput(input: List<String>): List<Card> {
    val out = mutableListOf<Card>()
    for (line in input) {
        val splitLine = line.split(':')
        val cardNumber = splitLine[0].extractIntsWithLocation().map { it.number }[0]
        val actualLine: String = splitLine[1]
        val actualSplitLine: List<String> = actualLine.split('|')
        val winningNumbers: Set<Int> = actualSplitLine[0].extractIntsWithLocation().map { it.number }.toSet()
        val drawnNumbers: List<Int> = actualSplitLine[1].extractIntsWithLocation().map { it.number }
        out.add(Card(cardNumber, winningNumbers, drawnNumbers))
    }
    return out
}

private data class Card(val id: Int, val winningNumbers: Set<Int>, val drawnNumbers: List<Int>)