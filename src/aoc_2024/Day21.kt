package aoc_2024

import aoc_util.*

fun main() {
    val testInput = readInput2024("Day21_test")
    val testResult = sumOfComplexities(testInput)
    println("Test result part 1: $testResult")
    val input = readInput2024("Day21")
    val result = sumOfComplexities(input)
//    val result = sumOfComplexities(listOf("3A"), 1)
    println("Result part 1: $result")
//     Puzzle result would be 25
    val start = System.currentTimeMillis()
    val robotLayers = 25
    val advancedResult = sumOfComplexities(input, robotLayers)
    val duration = System.currentTimeMillis() - start
    println("Result part 2($robotLayers): $advancedResult")
    println("Duration part 2: ${duration.toDouble() / 1000.0} s")
    // Hopefully the correct test value for robot layers 25 is 154115708116294
    val testResult2 = sumOfComplexities(testInput, robotLayers)
    println("Test result part 2: $testResult2")
}

private fun sumOfComplexities(inputs: List<String>, robotLayers: Int = 2): Pair<Long, Long> {
    var complexities = 0L
    var complexitiesQuickly = 0L
    for (input in inputs) {
        val (correct, quick) = complexityOf(input, robotLayers)
        complexities += correct
        complexitiesQuickly += quick
    }
    return complexities to complexitiesQuickly
}

private fun complexityOf(code: String, robotLayers: Int): Pair<Long, Long> {
    val numericPart = code.split('A')[0].toLong()
    val possibleNumpads: List<List<DirectionButton>> = buttonSequences(code) // these tend to be optimal already

    val minButtonPresses = 0//determineMinButtonPresses(bestNumpad, robotLayers)
    val minButtonPressesQuickly = determineMinButtonPressesQuickly(possibleNumpads, robotLayers)

    return (minButtonPresses * numericPart) to (minButtonPressesQuickly * numericPart)
}

private fun determineMinButtonPressesQuickly(possibleNumpads: List<List<DirectionButton>>, robotLayers: Int): Long {
    val numpadMinChunks = possibleNumpads.asSequence().map { it.countChunks() }.min()
    val bestNumpad = possibleNumpads.filter { it.countChunks() == numpadMinChunks }.minByOrNull { it.size }!!

    val numpadSegmentation = bestNumpad.split(DirectionButton.ENTER_DIRECTION, inclusive = true, keepTrailingEmptyList = false)
    val numpadSegmentCount: MutableMap<List<DirectionButton>, Long> = countSegments(numpadSegmentation)

    var currentSegmentCount = numpadSegmentCount
    for (i in 1..robotLayers) {
        val nextLayerSegmentCount: MutableMap<List<DirectionButton>, Long> = HashMap()
        for ((segment, count) in currentSegmentCount) {
            val directionPadDirectionSequences = directionPadDirectionSequences(segment)
            val movesForSegment: List<DirectionButton> = directionPadDirectionSequences.minByOrNull { it.size }!!
            val nextLayerSegments = movesForSegment.split(DirectionButton.ENTER_DIRECTION, inclusive = true, keepTrailingEmptyList = false)
            for (nextLayerSegment in nextLayerSegments) {
                val currentCount = nextLayerSegmentCount[nextLayerSegment] ?: 0L
                nextLayerSegmentCount[nextLayerSegment] = currentCount + count
            }
        }
        currentSegmentCount = nextLayerSegmentCount
    }

    return currentSegmentCount.entries.asSequence().map { it.key.size.toLong() * it.value }.sum()
}

private fun countSegments(segments: List<List<DirectionButton>>, countBy: Long = 1L): MutableMap<List<DirectionButton>, Long> {
    val out = mutableMapOf<List<DirectionButton>, Long>()
    for (segment in segments) {
        val currentCount = out[segment] ?: 0L
        out[segment] = currentCount + countBy
    }
    return out
}

private fun determineMinButtonPresses(possibleNumpads: List<List<DirectionButton>>, robotLayers: Int): Int {
    val numpadMinChunks = possibleNumpads.asSequence().map { it.countChunks() }.min()
    val bestNumpad = possibleNumpads.filter { it.countChunks() == numpadMinChunks }.minByOrNull { it.size }!!
    var currentResult = bestNumpad
    for (i in 1..robotLayers) {
//        println("Computing layer $i...")
        val nextLayer: List<List<DirectionButton>> = directionPadDirectionSequences(currentResult)
        val nextMinChunks: Int = nextLayer.asSequence().map { it.countChunks() }.min()
        currentResult = nextLayer.filter { it.countChunks() == nextMinChunks }.minByOrNull { it.size }!!
//        println("Best solution for layer$i: ${currentResult.size}")
    }
    val minButtonPresses = currentResult.size
    return minButtonPresses
}

private fun combine(buttonSequenceCombinations: List<List<List<DirectionButton>>>) = combineI(buttonSequenceCombinations)

private fun combineR(buttonSequenceCombinations: List<List<List<DirectionButton>>>): List<List<DirectionButton>> {
    if (buttonSequenceCombinations.isEmpty()) {
        return listOf(emptyList())
    }
    val remainderResult: List<List<DirectionButton>> = combineR(buttonSequenceCombinations.subList(1, buttonSequenceCombinations.size))
    val result = mutableListOf<List<DirectionButton>>()
    // focus on optimal solutions
    val relevantRemainder: List<DirectionButton> = extractRelevantTail(remainderResult)

    val combination: List<List<DirectionButton>> = buttonSequenceCombinations[0]
    fillResultList(combination, relevantRemainder, result)

    return result
}

private fun combineI(buttonSequenceCombinations: List<List<List<DirectionButton>>>): List<List<DirectionButton>> {
    if (buttonSequenceCombinations.isEmpty()) {
        return listOf(emptyList())
    }
    var currentTail: List<List<DirectionButton>> = listOf(emptyList())
    for (i in buttonSequenceCombinations.lastIndex downTo 0) {
        val relevantTail = extractRelevantTail(currentTail)
        val currentResult = mutableListOf<List<DirectionButton>>()
        val currentCombination = buttonSequenceCombinations[i]
        fillResultList(currentCombination, relevantTail, currentResult)
        currentTail = currentResult
    }
    return currentTail
}

private fun fillResultList(
    currentCombination: List<List<DirectionButton>>,
    relevantTail: List<DirectionButton>,
    currentResult: MutableList<List<DirectionButton>>
) {
    for (sequence: List<DirectionButton> in currentCombination) {
        val resultEntry = mutableListOf<DirectionButton>()
        resultEntry.addAll(sequence)
        if (sequence.isEmpty() || sequence.last() != DirectionButton.ENTER_DIRECTION) {
            resultEntry.add(DirectionButton.ENTER_DIRECTION)
        }
        resultEntry.addAll(relevantTail)
        currentResult.add(resultEntry)
    }
}

private fun extractRelevantTailOld(currentTail: List<List<DirectionButton>>): List<DirectionButton> {
    val currentTailMinChunks: Int = currentTail.asSequence().map { it.countChunks() }.min()
    val relevantTail = if (currentTail.size == 1) currentTail.first() else
        currentTail.filter { it.countChunks() == currentTailMinChunks }.minByOrNull { it.size }!!
    return relevantTail
}

private fun extractRelevantTail(currentTail: List<List<DirectionButton>>): List<DirectionButton> {
    var currentTailMinChunks = Int.MAX_VALUE
    var bestTail = currentTail.first()
    for (element in currentTail) {
        val elementChunks = element.countChunks()
        if (elementChunks < currentTailMinChunks) {
            currentTailMinChunks = elementChunks
            bestTail = element
        }
    }
    return bestTail
}

private fun buttonSequences(buttons: String): List<List<DirectionButton>> {
    val literalList = buttons.toCharArray().toList()
    return if (NumberButton.LITERAL_TO_NUMBER_BUTTON.keys.containsAll(literalList)) {
        val numberButtons: MutableList<NumberButton> = mutableListOf()
        literalList.forEach { numberButtons.add(NumberButton.LITERAL_TO_NUMBER_BUTTON[it]!!) }
        numpadDirectionSequences(numberButtons)
    } else {
        val directionalButton: List<DirectionButton> = literalList.asSequence()
            .map { DirectionButton.LITERAL_TO_DIRECTION_BUTTON[it]!! }
            .toList()
        directionPadDirectionSequences(directionalButton)
    }
}

private fun numpadDirectionSequences(buttons: List<NumberButton>): List<List<DirectionButton>> {
    if (buttons.isEmpty()) {
        return listOf(listOf(DirectionButton.ENTER_DIRECTION))
    }
    val actualButtons = mutableListOf(NumberButton.ENTER_NUMBER)
    actualButtons.addAll(buttons)
    val result = mutableListOf<List<List<DirectionButton>>>()
    for (i in 1..<actualButtons.size) {
        result.add(numberButtonMovements(actualButtons[i - 1], actualButtons[i]))
    }
    return combine(result)
}

private fun directionPadDirectionSequences(buttons: List<DirectionButton>, startAtEnter: Boolean = true): List<List<DirectionButton>> {
    if (buttons.isEmpty()) {
        return listOf(listOf(DirectionButton.ENTER_DIRECTION))
    }
    val actualButtons = mutableListOf<DirectionButton>()
    if (startAtEnter) {
        actualButtons.add(DirectionButton.ENTER_DIRECTION)
    }
    actualButtons.addAll(buttons)
    val result = mutableListOf<List<List<DirectionButton>>>()
    for (i in 1..<actualButtons.size) {
        result.add(directionButtonMovements(actualButtons[i - 1], actualButtons[i]))
    }
    return combine(result)
}

private fun directionButtonMovementsNew(from: DirectionButton, to: DirectionButton): List<List<DirectionButton>> {
    return listOf(DirectionButton.OPTIMAL_MOVE_MAP[from to to] ?: emptyList())
}

private fun directionButtonMovements(from: DirectionButton, to: DirectionButton): List<List<DirectionButton>> {
    val optimal: Int = from.coordinate.manhattanDistance(to.coordinate)
    val output = mutableListOf<List<DirectionButton>>()
    val iterator = CombinatorialIterator(DirectionButton.allWithoutEnter(), optimal)
    iterator.iterate { combination: List<DirectionButton> ->
        val result = combination.reversed()
        var currentCoordinate = from.coordinate
        for (movement in result) {
            currentCoordinate += movement.delta
            if (currentCoordinate !in DirectionButton.COORDINATE_TO_DIRECTION_BUTTON.keys) {
                return@iterate // continue
            }
        }
        if (DirectionButton.COORDINATE_TO_DIRECTION_BUTTON[currentCoordinate] == to) {
            output.add(result)
        }
    }
    return output
}

private fun numberButtonMovements(from: NumberButton, to: NumberButton): List<List<DirectionButton>> {
    val optimal: Int = from.coordinate.manhattanDistance(to.coordinate)
    val output = mutableListOf<List<DirectionButton>>()
    val iterator = CombinatorialIterator(DirectionButton.allWithoutEnter(), optimal)
    iterator.iterate { combination: List<DirectionButton> ->
        val result = combination.reversed()
        var currentCoordinate = from.coordinate
        for (movement in result) {
            currentCoordinate += movement.delta
            if (currentCoordinate !in NumberButton.COORDINATE_TO_NUMBER_BUTTON.keys) {
                return@iterate // continue
            }
        }
        if (NumberButton.COORDINATE_TO_NUMBER_BUTTON[currentCoordinate] == to) {
            output.add(result)
        }
    }
    return output
}

enum class NumberButton(val char: Char, val coordinate: Coordinate) {
    SEVEN('7', Coordinate(0, 0)),
    EIGHT('8', Coordinate(0, 1)),
    NINE('9', Coordinate(0, 2)),
    FOUR('4', Coordinate(1, 0)),
    FIVE('5', Coordinate(1, 1)),
    SIX('6', Coordinate(1, 2)),
    ONE('1', Coordinate(2, 0)),
    TWO('2', Coordinate(2, 1)),
    THREE('3', Coordinate(2, 2)),
    ZERO('0', Coordinate(3, 1)),
    ENTER_NUMBER('A', Coordinate(3, 2));


    companion object {
        val COORDINATE_TO_NUMBER_BUTTON: Map<Coordinate, NumberButton> = mapOf(
            SEVEN.coordinate to SEVEN,
            EIGHT.coordinate to EIGHT,
            NINE.coordinate to NINE,
            FOUR.coordinate to FOUR,
            FIVE.coordinate to FIVE,
            SIX.coordinate to SIX,
            ONE.coordinate to ONE,
            TWO.coordinate to TWO,
            THREE.coordinate to THREE,
            ZERO.coordinate to ZERO,
            ENTER_NUMBER.coordinate to ENTER_NUMBER
        )
        val LITERAL_TO_NUMBER_BUTTON: Map<Char, NumberButton> = mapOf(
            ENTER_NUMBER.char to ENTER_NUMBER,
            ZERO.char to ZERO,
            ONE.char to ONE,
            TWO.char to TWO,
            THREE.char to THREE,
            FOUR.char to FOUR,
            FIVE.char to FIVE,
            SIX.char to SIX,
            SEVEN.char to SEVEN,
            EIGHT.char to EIGHT,
            NINE.char to NINE,
        )
    }
}

enum class DirectionButton(val char: Char, val coordinate: Coordinate, val delta: Movement) {
    UP('^', Coordinate(0, 1), Movement.NORTH),
    ENTER_DIRECTION('A', Coordinate(0, 2), Movement.STAY),
    LEFT('<', Coordinate(1, 0), Movement.WEST),
    DOWN('v', Coordinate(1, 1), Movement.SOUTH),
    RIGHT('>', Coordinate(1, 2), Movement.EAST), ;

    companion object {
        val COORDINATE_TO_DIRECTION_BUTTON: Map<Coordinate, DirectionButton> = mapOf(
            UP.coordinate to UP,
            ENTER_DIRECTION.coordinate to ENTER_DIRECTION,
            LEFT.coordinate to LEFT,
            DOWN.coordinate to DOWN,
            RIGHT.coordinate to RIGHT,
        )

        val LITERAL_TO_DIRECTION_BUTTON: Map<Char, DirectionButton> = mapOf(
            UP.char to UP,
            ENTER_DIRECTION.char to ENTER_DIRECTION,
            LEFT.char to LEFT,
            DOWN.char to DOWN,
            RIGHT.char to RIGHT
        )

        val OPTIMAL_MOVE_MAP: Map<Pair<DirectionButton, DirectionButton>, List<DirectionButton>> = mapOf(
            ENTER_DIRECTION to UP to listOf(LEFT),
            ENTER_DIRECTION to RIGHT to listOf(DOWN),
            ENTER_DIRECTION to DOWN to listOf(DOWN, LEFT),
            ENTER_DIRECTION to LEFT to listOf(DOWN, LEFT, LEFT),
            UP to ENTER_DIRECTION to listOf(RIGHT),
            UP to RIGHT to listOf(DOWN, RIGHT),
            UP to DOWN to listOf(DOWN),
            UP to LEFT to listOf(DOWN, LEFT),
            RIGHT to ENTER_DIRECTION to listOf(UP),
            RIGHT to UP to listOf(UP, LEFT),
            RIGHT to DOWN to listOf(LEFT),
            RIGHT to LEFT to listOf(LEFT, LEFT),
            DOWN to ENTER_DIRECTION to listOf(UP, RIGHT),
            DOWN to UP to listOf(UP),
            DOWN to RIGHT to listOf(RIGHT),
            DOWN to LEFT to listOf(LEFT),
            LEFT to ENTER_DIRECTION to listOf(RIGHT, RIGHT, UP),
            LEFT to UP to listOf(RIGHT, UP),
            LEFT to RIGHT to listOf(RIGHT, RIGHT),
            LEFT to DOWN to listOf(RIGHT)
        )

        fun allWithoutEnter() = listOf(UP, RIGHT, LEFT, DOWN)
    }
}