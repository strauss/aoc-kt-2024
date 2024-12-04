import de.dreamcube.hornet_queen.array.PrimitiveCharArray

fun main() {
    val testList = readInput("Day04_test")
    val inputList = readInput("Day04")

    val exInput = readInput(testList)
    val exResult = countXmas(exInput)
    println("Example result: $exResult")
    val exResult2 = countMasX(exInput)
    println("Example result2: $exResult2")

    val input = readInput(inputList)
    val result = countXmas(input)
    println("Result: $result")
    val result2 = countMasX(input)
    println("Result2: $result2")
    println()

}

fun countXmas(array: PrimitiveMultiDimArray<Char>): Int {
    var sum = 0
    for (j in 0 until array.getDimensionSize(0)) {
        for (i in 0 until array.getDimensionSize(1)) {
            val currentChar = array[j, i]
            if (currentChar == 'X') {
                sum += checkXmas(array, j, i)
            }
        }
    }
    return sum
}

fun countMasX(array: PrimitiveMultiDimArray<Char>): Int {
    var sum = 0
    for (j in 0 until array.getDimensionSize(0)) {
        for (i in 0 until array.getDimensionSize(1)) {
            val currentChar = array[j, i]
            if (currentChar == 'A') {
                sum += checkMasX(array, j, i)
            }
        }
    }
    return sum
}

fun checkMasX(array: PrimitiveMultiDimArray<Char>, j: Int, i: Int): Int {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    val checkWord = "MAS"
    val revCheckWord = "SAM"
    val value = array[j, i]
    if (value != 'A') {
        return 0
    }
    val first: String =
        (if (j - 1 < 0 || i - 1 < 0) "" else array[j - 1, i - 1].toString()) +
                value.toString() +
                (if (j + 1 >= height || i + 1 >= width) "" else array[j + 1, i + 1].toString())
    val second: String = (if (j - 1 < 0 || i + 1 >= width) "" else array[j - 1, i + 1].toString()) +
            value.toString() +
            (if (j + 1 >= height || i - 1 < 0) "" else array[j + 1, i - 1].toString())
    if ((first == checkWord || first == revCheckWord) && (second == checkWord || second == revCheckWord)) {
        return 1
    }
    return 0
}

fun checkXmas(array: PrimitiveMultiDimArray<Char>, j: Int, i: Int): Int {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    val checkWord = "XMAS"
    var sum = 0
    val value = array[j, i]
    if (value != 'X') {
        return 0
    }

    // read right
    var word = ""
    for (x in i..i + 3) {
        if (x >= width) {
            break
        }
        word += array[j, x]
    }
    if (word == checkWord) {
        sum += 1
    }

    word = ""

    // read left
    for (x in i downTo i - 3) {
        if (x < 0) {
            break
        }
        word += array[j, x]
    }
    if (word == checkWord) {
        sum += 1
    }

    word = ""

    // read down
    for (y in j..j + 3) {
        if (y >= height) {
            break
        }
        word += array[y, i]
    }
    if (word == checkWord) {
        sum += 1
    }

    word = ""

    // read up
    for (y in j downTo j - 3) {
        if (y < 0) {
            break
        }
        word += array[y, i]
    }
    if (word == checkWord) {
        sum += 1
    }

    word = ""

    // diagonals
    var y = j
    for (x in i..i + 3) {
        if (x >= width || y >= height) {
            break
        }
        word += array[y, x]
        y += 1
    }
    if (word == checkWord) {
        sum += 1
    }

    word = ""
    y = j
    for (x in i downTo i - 3) {
        if (x < 0 || y >= height) {
            break
        }
        word += array[y, x]
        y += 1
    }
    if (word == checkWord) {
        sum += 1
    }

    word = ""
    y = j

    for (x in i downTo i - 3) {
        if (x < 0 || y < 0) {
            break
        }
        word += array[y, x]
        y -= 1
    }
    if (word == checkWord) {
        sum += 1
    }

    word = ""
    y = j

    for (x in i..i + 3) {
        if (x >= width || y < 0) {
            break
        }
        word += array[y, x]
        y -= 1
    }
    if (word == checkWord) {
        sum += 1
    }

    return sum
}

fun readInput(input: List<String>): PrimitiveMultiDimArray<Char> {
    val dim = input[0].length // I don't care about any exception here ... if empty -> bad luck
    val out: PrimitiveMultiDimArray<Char> = PrimitiveMultiDimArray(dim, dim) { size -> PrimitiveCharArray(size) }
    var j = 0
    input.forEach { line ->
        for (i in line.indices) {
            out[j, i] = line[i]
        }
        j += 1
    }
    return out
}