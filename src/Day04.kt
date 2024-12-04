fun main() {
    val testList = readInput("Day04_test")
    val inputList = readInput("Day04")

    val (exInput, exSize) = readInput(testList)
    val exResult = countXmas(exInput, exSize)
    println("Example result: $exResult")
    val exResult2 = countMasX(exInput, exSize)
    println("Example result2: $exResult2")

    val (input, inputSize) = readInput(inputList)
    val result = countXmas(input, inputSize)
    println("Result: $result")
    val result2 = countMasX(input, inputSize)
    println("Result2: $result2")
    println()

}

fun countMasX(array: MultiDimArray<Char>, size: Int): Int {
    var sum = 0
    for (j in 0 until size) {
        for (i in 0 until size) {
            val currentChar = array[j, i]
            if (currentChar == 'A') {
                sum += checkMasX(array, size, j, i)
            }
        }
    }
    return sum
}

fun countXmas(array: MultiDimArray<Char>, size: Int): Int {
    var sum = 0
    for (j in 0 until size) {
        for (i in 0 until size) {
            val currentChar = array[j, i]
            if (currentChar == 'X') {
                sum += checkXmas(array, size, j, i)
            }
        }
    }
    return sum
}

fun checkMasX(array: MultiDimArray<Char>, size: Int, j: Int, i: Int): Int {
    val checkWord = "MAS"
    val revCheckWord = "SAM"
    val value = array[j, i]
    if (value != 'A') {
        return 0
    }
    val first: String =
        (if (j - 1 < 0 || i - 1 < 0) "" else array[j - 1, i - 1].toString()) +
                value.toString() +
                (if (j + 1 >= size || i + 1 >= size) "" else array[j + 1, i + 1].toString())
    val second: String = (if (j - 1 < 0 || i + 1 >= size) "" else array[j - 1, i + 1].toString()) +
            value.toString() +
            (if (j + 1 >= size || i - 1 < 0) "" else array[j + 1, i - 1].toString())
    if ((first == checkWord || first == revCheckWord) && (second == checkWord || second == revCheckWord)) {
        return 1
    }
    return 0
}

fun checkXmas(array: MultiDimArray<Char>, size: Int, j: Int, i: Int): Int {
    val checkWord = "XMAS"
    var sum = 0
    val value = array[j, i]
    if (value != 'X') {
        return 0
    }

    // read right
    var word = ""
    for (x in i..i + 3) {
        if (x >= size) {
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
        if (y >= size) {
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
        if (x >= size || y >= size) {
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
        if (x < 0 || y >= size) {
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
        if (x >= size || y < 0) {
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

fun readInput(input: List<String>): Pair<MultiDimArray<Char>, Int> {
    var out: MultiDimArray<Char>? = null
    var dim = 0
    var j = 0
    input.forEach { line ->
        if (out == null) {
            dim = line.length
            out = MultiDimArray(dim, dim) { size -> Array(size) { ' ' } }
        }
        for (i in line.indices) {
            out!![j, i] = line[i]
        }
        j += 1
    }
    return Pair(out!!, dim)
}