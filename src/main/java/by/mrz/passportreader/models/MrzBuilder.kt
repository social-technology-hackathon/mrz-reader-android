package by.mrz.passportreader.models

class MrzBuilder(text: String) {
    val top: String
    val bottom: String

    init {
        if (text.isNotBlank()) {
            text.replace(" ", "")
                .split("\n")
                .let { lines ->
                    val top = lines[0].let {
                        var result = it
                        while (result.length < 44) {
                            result += "<"
                        }
                        result
                    }
                    val bottom = lines[1]

                    if (top.isEmpty() || bottom.isEmpty()) {
                        throw IllegalArgumentException("invalid scan (top: $top & bottom: $bottom)")
                    }




                    this.top = top
                    this.bottom = bottom
                }
        } else {
            this.top = ""
            this.bottom = ""
        }
    }

    fun getLine(number: Int): String {
        when (number) {
            0 -> return top
            1 -> return bottom
        }

        throw IllegalArgumentException("invalid line: $number")
    }
}