package aoc_2023

import aoc_util.readInput2023

fun main() {
    val testLines = readInput2023("Day07_test")
    val testInput = parseLines(testLines)
    val testResult = solve(testInput, Hand.handComp)
    println("Test result: $testResult")
    val test2Result = solve(testInput, Hand.jHandComp)
    println("Test 2 result: $test2Result")

    val lines = readInput2023("Day07")
    val input = parseLines(lines)
    val result = solve(input, Hand.handComp)
    println("Result: $result")
    val result2 = solve(input, Hand.jHandComp)
    println("Result 2: $result2")
}

private fun solve(hands: List<Hand>, comp: Comparator<Hand>): Long {
    val sortedHands = hands.sortedWith(comp)
    var result = 0L
    var currentRank = 1
    for (hand in sortedHands) {
        result += hand.bid * currentRank
        currentRank += 1
    }
    return result
}

private fun parseLines(lines: List<String>): List<Hand> {
    val splitEx = " ".toRegex()
    return buildList {
        for (line in lines) {
            val split = splitEx.split(line)
            add(Hand(split[0], split[1].toInt()))
        }
    }
}

private enum class HandTypes {
    HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, FULL_HOUSE, FOUR_OF_A_KIND, FIVE_OF_A_KIND
}

private class Hand(val rep: String, val bid: Int) {
    companion object {
        val order: String = "23456789TJQKA"
        val jOrder: String = "J23456789TQKA"
        val legitHand = "[2-9TJQKA]{5}".toRegex()

        val orderComp: Comparator<Char> = Comparator<Char> { o1: Char, o2: Char ->
            order.indexOf(o1).compareTo(order.indexOf(o2))
        }

        val jOrderComp: Comparator<Char> = Comparator<Char> { o1: Char, o2: Char ->
            jOrder.indexOf(o1).compareTo(jOrder.indexOf(o2))
        }

        val handComp: Comparator<Hand> = Comparator<Hand> { h1: Hand, h2: Hand ->
            val typeComp = h1.handType.compareTo(h2.handType)
            if (typeComp != 0) {
                return@Comparator typeComp
            }
            return@Comparator breakTie(h1, h2, orderComp)
        }

        val jHandComp: Comparator<Hand> = Comparator<Hand> { h1: Hand, h2: Hand ->
            // compare joker hands
            val typeComp = h1.jokerHand.handType.compareTo(h2.jokerHand.handType)
            if (typeComp != 0) {
                return@Comparator typeComp
            }
            return@Comparator breakTie(h1, h2, jOrderComp)
        }

        private fun breakTie(h1: Hand, h2: Hand, orderComp: Comparator<Char>): Int {
            val rep1 = h1.rep
            val rep2 = h2.rep
            for (idx in 0..<5) {
                val pComp = orderComp.compare(rep1[idx], rep2[idx])
                if (pComp != 0) {
                    return pComp
                }
            }
            return 0
        }
    }

    val handType: HandTypes by lazy(LazyThreadSafetyMode.NONE) {
        val chars = rep.toCharArray().toSet()
        fun maxChars(): Int {
            val max = chars.asSequence()
                .map { ch ->
                    rep.count {
                        it == ch
                    }
                }.max()
            return max
        }

        val maxChars = maxChars()
        when (chars.size) {
            1 -> HandTypes.FIVE_OF_A_KIND
            2 -> when (maxChars) {
                4 -> HandTypes.FOUR_OF_A_KIND
                3 -> HandTypes.FULL_HOUSE
                else -> throw IllegalArgumentException("Illegal")
            }

            3 -> when (maxChars) {
                3 -> HandTypes.THREE_OF_A_KIND
                2 -> HandTypes.TWO_PAIR
                else -> throw IllegalArgumentException("Illegal")
            }

            4 -> HandTypes.PAIR
            5 -> HandTypes.HIGH_CARD
            else -> throw IllegalArgumentException("Illegal")
        }
    }

    init {
        if (!legitHand.matches(rep)) {
            throw IllegalArgumentException("Invalid hand: '$rep'")
        }
    }

    val jokerHand: Hand
        get() {
            if (!rep.contains('J')) {
                return this
            }
            // all chars without the jokers
            val chars = rep.toCharArray().toSet().minus('J')
            if (chars.isEmpty()) {
                // All jokers, so we just put in all aces
                return Hand(rep.replace('J', 'A'), this.bid)
            }
            var result: Hand? = null
            for (ch in chars) {
                val candidate = Hand(rep.replace('J', ch), this.bid)
                if (result == null) {
                    result = candidate
                    continue
                }
                val comp = handComp.compare(candidate, result)
                if (comp > 0) {
                    // candidate > result
                    result = candidate
                }
            }
            return result!!
        }
}