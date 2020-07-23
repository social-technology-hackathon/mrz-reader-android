package by.mrz.passportreader

/**
 * Gets the character value (based on ICOA # 4.9) of a given char.
 *
 * @see https://www.icao.int/publications/Documents/9303_p3_cons_en.pdf
 */
fun Char.charValue(): Int {
    if (this == '<') {
        return 0
    }

    if (this in '0'..'9') {
        return this - '0'
    }
    if (this in 'A'..'Z') {
        return this - 'A' + 10
    }

    throw IllegalArgumentException("char [$this] does not have a weigh factor")
}

/**
 * Get the weighted sum of a character array (based on ICOA # 4.9).
 *
 * @see https://www.icao.int/publications/Documents/9303_p3_cons_en.pdf
 */
fun CharArray.weightSum(): Int {
    val weights = listOf(7, 3, 1)

    var sum = 0
    for ((index, character) in this.withIndex()) {
        val value = character.charValue()
        val weigh = weights[index.rem(weights.size)]

        sum += value.times(weigh)
    }

    return sum
}

/**
 * Capitalizes each word in a string (rather then just the first character in a string)
 */
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.capitalize() }
}

/**
 * Cleans up the spacing character '<' and removes any leading/trailing spaces.
 */
fun String.sanitize(): String {
    return this.replace("<", "").trim()
}

