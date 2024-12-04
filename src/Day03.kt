private val part1Regex = "mul\\((\\d+),(\\d+)\\)".toRegex()
private val part2Regex = "(do\\(\\))|(don't\\(\\))|(mul\\((\\d+),(\\d+)\\))".toRegex()

fun main() {
    val testResultPart1 = part1(readInput("Day03_test_01"))
    println("Test result part 1: $testResultPart1")
    val testResultPart2 = part2(readInput("Day03_test_02"))
    println("Test result part 2: $testResultPart2")

    println()

    val input = readInput("Day03")
    val resultPart1 = part1(input)
    println("Result part 1: $resultPart1")
    val resultPart2 = part2(input)
    println("Result part 2: $resultPart2")
}

private fun part1(input: List<String>): Int {
    var total = 0
    input.forEach { line ->
        var sum = 0
        part1Regex.findAll(line).forEach { match ->
            val product: Int = match.groupValues[1].toInt() * match.groupValues[2].toInt()
            sum += product
        }
        total += sum
    }
    return total
}

private fun part2(input: List<String>): Int {
    var enabled = true
    var total = 0
    input.forEach { line ->
        var sum = 0
        part2Regex.findAll(line).forEach { match ->
            if (match.groupValues[1].isNotBlank()) {
                // do
                enabled = true
            } else if (match.groupValues[2].isNotBlank()) {
                // don't
                enabled = false
            } else if (match.groupValues[3].isNotBlank() && enabled) {
                // mul if enabled
                val product = match.groupValues[4].toInt() * match.groupValues[5].toInt()
                sum += product
            }
        }
        total += sum
    }
    return total
}
