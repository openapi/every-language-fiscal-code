import java.util.Locale

/**
 * Italian Fiscal Code (codice fiscale) — Kotlin implementation.
 *
 * Reads one JSON object from stdin and writes the result to stdout,
 * following SPECIFICATION.md. Includes a minimal self-contained JSON parser.
 */
class InputError(message: String) : Exception(message)

private val REQUIRED_FIELDS = listOf("surname", "name", "birth_date", "sex", "birthplace_code")
private val NAME_REGEX = Regex("^[A-Za-z '\\-]*$")
private val DATE_REGEX = Regex("^([0-9]{4})-([0-9]{2})-([0-9]{2})$")
private val BIRTHPLACE_REGEX = Regex("^[A-Z][0-9]{3}$")
private const val VOWELS = "AEIOU"
private const val MONTH_CODES = "ABCDEHLMPRST"

/** Values of characters in odd positions, indexed by 0-9 then A-Z. */
private val ODD_VALUES = intArrayOf(
    1, 0, 5, 7, 9, 13, 15, 17, 19, 21,
    1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3,
    6, 8, 12, 14, 16, 10, 22, 25, 24, 23
)

private object NotAString

/** Minimal JSON parser: parses top-level JSON objects and extracts fields. */
class JsonParser(private val text: String) {
    private var pos = 0

    private fun invalid(): InputError = InputError("invalid_json")

    private fun peek(): Char = if (pos < text.length) text[pos] else '\u0000'

    private fun next(): Char {
        if (pos >= text.length) throw invalid()
        return text[pos++]
    }

    private fun expect(c: Char) {
        if (next() != c) throw invalid()
    }

    private fun skipWhitespace() {
        while (pos < text.length && text[pos] in " \t\n\r") {
            pos++
        }
    }

    fun parseDocument(): Map<String, Any?> {
        skipWhitespace()
        if (peek() != '{') throw invalid()
        val members = mutableMapOf<String, Any?>()
        parseObject(members)
        skipWhitespace()
        if (pos != text.length) throw invalid()
        return members
    }

    private fun parseObject(members: MutableMap<String, Any?>?) {
        expect('{')
        skipWhitespace()
        if (peek() == '}') {
            pos++
            return
        }
        while (true) {
            skipWhitespace()
            if (peek() != '"') throw invalid()
            val key = parseString()
            skipWhitespace()
            expect(':')
            val value = parseValue()
            members?.put(key, value)
            skipWhitespace()
            val c = next()
            if (c == '}') return
            if (c != ',') throw invalid()
        }
    }

    private fun parseArray() {
        expect('[')
        skipWhitespace()
        if (peek() == ']') {
            pos++
            return
        }
        while (true) {
            parseValue()
            skipWhitespace()
            val c = next()
            if (c == ']') return
            if (c != ',') throw invalid()
        }
    }

    private fun parseLiteral(word: String) {
        if (!text.startsWith(word, pos)) throw invalid()
        pos += word.length
    }

    private fun digits() {
        if (peek() !in '0'..'9') throw invalid()
        while (peek() in '0'..'9') pos++
    }

    private fun parseNumber() {
        if (peek() == '-') pos++
        if (peek() == '0') {
            pos++
        } else {
            digits()
        }
        if (peek() == '.') {
            pos++
            digits()
        }
        if (peek() == 'e' || peek() == 'E') {
            pos++
            if (peek() == '+' || peek() == '-') pos++
            digits()
        }
    }

    private fun parseString(): String {
        expect('"')
        val out = StringBuilder()
        while (true) {
            val c = next()
            if (c == '"') return out.toString()
            if (c.code < 0x20) throw invalid()
            if (c != '\\') {
                out.append(c)
                continue
            }
            when (val e = next()) {
                '"', '\\', '/' -> out.append(e)
                'b' -> out.append('\b')
                'f' -> out.append('\u000C')
                'n' -> out.append('\n')
                'r' -> out.append('\r')
                't' -> out.append('\t')
                'u' -> {
                    if (pos + 4 > text.length) throw invalid()
                    val hex = text.substring(pos, pos + 4)
                    try {
                        out.append(hex.toInt(16).toChar())
                    } catch (e: NumberFormatException) {
                        throw invalid()
                    }
                    pos += 4
                }
                else -> throw invalid()
            }
        }
    }

    private fun parseValue(): Any? {
        skipWhitespace()
        return when (peek()) {
            '"' -> parseString()
            '{' -> {
                parseObject(null)
                NotAString
            }
            '[' -> {
                parseArray()
                NotAString
            }
            't' -> {
                parseLiteral("true")
                NotAString
            }
            'f' -> {
                parseLiteral("false")
                NotAString
            }
            'n' -> {
                parseLiteral("null")
                null
            }
            else -> {
                parseNumber()
                NotAString
            }
        }
    }
}

private fun normalizeName(value: Any?, code: String): String {
    if (value !is String || !NAME_REGEX.matches(value)) throw InputError(code)
    val letters = value.uppercase(Locale.ROOT).filter { it in 'A'..'Z' }
    if (letters.isEmpty()) throw InputError(code)
    return letters
}

private fun encodeSurname(text: String): String {
    val consonants = text.filter { it !in VOWELS }
    val vowels = text.filter { it in VOWELS }
    return (consonants + vowels + "XXX").take(3)
}

private fun encodeName(text: String): String {
    val consonants = text.filter { it !in VOWELS }
    val vowels = text.filter { it in VOWELS }
    if (consonants.length >= 4) {
        return "${consonants[0]}${consonants[2]}${consonants[3]}"
    }
    return (consonants + vowels + "XXX").take(3)
}

private data class DateComponents(val year: Int, val month: Int, val day: Int)

private fun parseDate(value: Any?): DateComponents {
    val error = InputError("invalid_birth_date")
    if (value !is String) throw error
    val match = DATE_REGEX.matchEntire(value) ?: throw error
    val year = match.groupValues[1].toInt()
    val month = match.groupValues[2].toInt()
    val day = match.groupValues[3].toInt()
    val leap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    val daysInMonth = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    if (year < 1 || month !in 1..12 || day !in 1..daysInMonth[month - 1]) {
        throw error
    }
    return DateComponents(year, month, day)
}

private fun checkCharacter(partial: String): Char {
    var sum = 0
    for (i in partial.indices) {
        val c = partial[i]
        val isDigit = c in '0'..'9'
        if (i % 2 == 0) {
            val idx = if (isDigit) c - '0' else c - 'A' + 10
            sum += ODD_VALUES[idx]
        } else {
            sum += if (isDigit) c - '0' else c - 'A'
        }
    }
    return 'A' + (sum % 26)
}

fun compute(input: Map<String, Any?>): String {
    for (field in REQUIRED_FIELDS) {
        if (!input.containsKey(field)) throw InputError("missing_field")
    }
    val surname = normalizeName(input["surname"], "invalid_surname")
    val name = normalizeName(input["name"], "invalid_name")
    val date = parseDate(input["birth_date"])
    val sex = input["sex"]
    if (sex != "M" && sex != "F") throw InputError("invalid_sex")
    val place = input["birthplace_code"]
    if (place !is String || !BIRTHPLACE_REGEX.matches(place)) {
        throw InputError("invalid_birthplace_code")
    }
    val yearCode = "%02d".format(Locale.ROOT, date.year % 100)
    val monthCode = MONTH_CODES[date.month - 1]
    val daySexCode = "%02d".format(Locale.ROOT, if (sex == "F") date.day + 40 else date.day)
    val partial = encodeSurname(surname) + encodeName(name) + yearCode + monthCode + daySexCode + place
    return partial + checkCharacter(partial)
}

fun main() {
    val text = System.`in`.readAllBytes().toString(Charsets.UTF_8)
    try {
        val input = JsonParser(text).parseDocument()
        val code = compute(input)
        println("{\"fiscal_code\":\"$code\"}")
    } catch (e: InputError) {
        println("{\"error\":\"${e.message}\"}")
        kotlin.system.exitProcess(1)
    }
}
