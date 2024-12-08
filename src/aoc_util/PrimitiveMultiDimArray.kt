package aoc_util

import de.dreamcube.hornet_queen.array.PrimitiveArray

class PrimitiveMultiDimArray<T>(private vararg val dimension: Int, oneDimConstructor: (Int) -> PrimitiveArray<T>) {

    private val internalArray: PrimitiveArray<T>

    init {
        // check validity
        val valid = dimension.all { it > 0 }
        if (!valid) {
            throw IllegalArgumentException("Dimensions have to be greater than 0")
        }
        val oneDimLength: Int = dimension.product()
        internalArray = oneDimConstructor.invoke(oneDimLength)
    }

    operator fun get(vararg index: Int) = internalArray[getIndex(*index)]

    operator fun set(vararg index: Int, value: T) {
        internalArray[getIndex(*index)] = value
    }

    private fun getIndex(vararg index: Int): Int {
        if (index.size != dimension.size) {
            throw IllegalArgumentException("Dimension mismatch. Expected size ${dimension.size} and found ${index.size}.")
        }
        var oneDimIndex = 0
        for (currentDimPosition in index.indices) {
            val currentIndex = index[currentDimPosition]
            val currentDimPositionLength = dimension[currentDimPosition]
            if (currentIndex >= currentDimPositionLength) {
                throw ArrayIndexOutOfBoundsException("Index at current dimension position $currentDimPosition may not exceed ${currentDimPositionLength - 1}")
            }
            val product = dimension.product(currentDimPosition + 1)
            oneDimIndex += currentIndex * product
        }
        return oneDimIndex
    }

    /**
     * Retrieve the size of the specified dimension. Counting starts at 0
     */
    fun getDimensionSize(dimensionIndex: Int): Int {
        if (dimensionIndex > dimension.size) {
            throw ArrayIndexOutOfBoundsException("$dimensionIndex exceeds max dimension index of this array: ${dimension.size - 1}")
        }
        return dimension[dimensionIndex]
    }

    override fun toString(): String {
        return internalArray.toString()
    }

    operator fun iterator() = internalArray.iterator()
}

private fun IntArray.product(fromIndex: Int = 0, toIndex: Int = this.size - 1): Int {
    var product = 1
    for (i in fromIndex..toIndex) {
        product *= this[i]
    }
    return product
}

