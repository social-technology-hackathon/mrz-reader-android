package by.mrz.passportreader.models

import androidx.annotation.VisibleForTesting
import by.mrz.passportreader.*
import by.mrz.passportreader.models.MrzField.*
import java.util.*

/**
 * http://www.highprogrammer.com/cgi-bin/uniqueid/mrzpr
 *
 * @see https://www.icao.int/publications/Documents/9303_p3_cons_en.pdf
 * @see https://pypi.org/project/mrz/
 * @see https://en.wikipedia.org/wiki/Machine-readable_passport
 */
class MrzParser private constructor(text: String) {
    private val charsetRegex = Regex("^[A-Z0-9<]{30,44}$")

    private val builder: MrzBuilder = MrzBuilder(text)

    val passportInformation: PassportInformation

    init {
        val documentCode = parseField(builder, DOC).sanitize()
        val documentNumber = parseField(builder, DOCUMENT_NUMBER)

        val nameField = parseField(builder, SURNAME_GIVEN_NAME)
        val firstName = parseFirstName(nameField)
        val lastName = parseLastName(nameField)

        val country = parseField(builder, COUNTRY)
        val nationality = parseField(builder, NATIONALITY)
        val sex = parseField(builder, SEX)

        val dateOfBirth = parseField(builder, BIRTH_DATE)

        val expirationDate = parseField(builder, EXPIRY_DATE)

        passportInformation = PassportInformation(
            documentNumber = documentNumber,
            documentCode = documentCode,
            firstName = firstName,
            lastName = lastName,
            country = country,
            nationality = nationality,
            sex = sex,
            dateOfBirth = dateOfBirth,
            expirationDate = expirationDate
        )
    }

    /**
     * Parses the given name(s). It's not known what names are to be considered 'surname' simply based on delimiting
     * on spaces. Hence this method gives ALL the given names.
     *
     * FAMILIYNAME<<NAME<FOO<BAR<BAZ
     */
    private fun parseFirstName(raw: String): String {
        if (raw.isBlank()) {
            return ""
        }

        var givenNames = raw.split("<<")[1]
        givenNames = givenNames.replace('<', ' ')

        return givenNames.toLowerCase(Locale.ENGLISH).capitalizeWords()
    }

    /**
     * Parses the family name.
     */
    private fun parseLastName(raw: String): String {
        if (raw.isBlank()) {
            return ""
        }

        val rawLastName = raw.split("<<")[0].toLowerCase(Locale.ENGLISH)
        return rawLastName.replace('<', ' ').capitalize()
    }

    /**
     * Validate the checksum digit on the [documentNumber], returning <code>true</code> when valid, false otherwise.
     */
    @VisibleForTesting
    fun hasValidChecksumDocumentNumber(): Boolean {
        return hasValidChecksum(passportInformation.documentNumber, HASH1)
    }

    /**
     * Validate the checksum digit on the [dateOfBirth], returning <code>true</code> when valid, false otherwise.
     */
    @VisibleForTesting
    fun hasValidChecksumBirthday(): Boolean {
        return hasValidChecksum(passportInformation.dateOfBirth, HASH2)
    }

    /**
     * Validate the checksum digit on the [expirationDate], returning <code>true</code> when valid, false otherwise.
     */
    @VisibleForTesting
    fun hasValidChecksumExpirationDate(): Boolean {
        return hasValidChecksum(passportInformation.expirationDate, HASH3)
    }

    /**
     * Validate the checksum digit on the optional data, returning <code>true</code> when valid, false otherwise.
     * Only valid when the [type] is of [MrzType.TYPE_TD3] because it is the only type with a checksum for optional
     * data.
     *
     * Note: that all other types will return <code>false</code> because they do not have the [MrzField.OPTIONAL_DATA]
     * field.
     */
    @VisibleForTesting
    fun hasValidChecksumOptionalData(): Boolean {
        val data = parseField(builder, OPTIONAL_DATA)

        val valid = hasValidChecksum(data, HASH4)
        if (valid) {
            return true
        }

        return false
    }

    /**
     * Validate the checksum digit on all data fields that have a checksum, returning <code>true</code> when valid,
     * <code>false</code> otherwise.
     */
    @VisibleForTesting
    fun hasValidChecksumOverall(): Boolean {
        val builder = StringBuilder()
        for (field in TYPE3_CHECKSUM_FIELDS) {
            builder.append(parseField(this.builder, field))
        }

        val total = builder.toString()
        return hasValidChecksum(total, FINAL_HASH)
    }

    /**
     * Check is the checksum in [data] matches the data that comes from [field]. Returns <code>true</code> when the
     * checksum is valid, <code>false</code> otherwise.
     */
    private fun hasValidChecksum(
        data: String,
        field: MrzField
    ): Boolean {
        val sum = data.toCharArray().weightSum()

        val hash = parseField(builder, field)
        if (hash.isBlank()) {
            return false
        }
        if (validSum(sum, hash.toInt())) {
            return true
        }

        return false
    }

    /**
     * Checks if the [sum] matches [hash] after sum mod(10)
     */
    private fun validSum(sum: Int, hash: Int): Boolean {
        val rem = sum.rem(10)
        if (rem == hash) {
            return true
        }
        return false
    }

    /**
     * Checks the validity of the data whether the correct charset is used and if each checksum is valid.
     */
    fun isValid(): Boolean {
        val regexBirthDay = Regex("^[0-9]{6}$")
        if (!passportInformation.dateOfBirth.matches(regexBirthDay)) {
            return false
        }

        if (!hasValidChecksumBirthday()) {
            return false
        }

        if (!hasValidChecksumDocumentNumber()) {
            return false
        }

        if (!hasValidChecksumExpirationDate()) {
            return false
        }

        // charset check
        if (!charsetRegex.matches(builder.top)) {
            return false
        }

        if (!charsetRegex.matches(builder.bottom)) {
            return false
        }

        if (!hasValidChecksumOptionalData()) {
            return false
        }


        if (!hasValidChecksumOverall()) {
            return false
        }

        return true
    }


    companion object {

        fun build(text: String): MrzParser = MrzParser(text)

        fun parseField(builder: MrzBuilder, field: MrzField): String {
            if (TYPE3_PARSE_MAP.containsKey(field)) {
                val range = TYPE3_PARSE_MAP.getValue(field)
                val row = builder.getLine(range.line)
                return row.substring(range.begin, range.end)
            }

            return ""
        }


        val TYPE3_CHECKSUM_FIELDS = listOf(
            DOCUMENT_NUMBER,
            HASH1,
            BIRTH_DATE,
            HASH2,
            EXPIRY_DATE,
            HASH3,
            OPTIONAL_DATA,
            HASH4
        )

        private val TYPE3_PARSE_MAP = hashMapOf(
            Pair(DOC, Range(0, 0, 2)),
            Pair(COUNTRY, Range(0, 2, 5)),
            Pair(SURNAME_GIVEN_NAME, Range(0, 5, 44)),
            Pair(DOCUMENT_NUMBER, Range(1, 0, 9)),
            Pair(HASH1, Range(1, 9, 10)),
            Pair(NATIONALITY, Range(1, 10, 13)),
            Pair(BIRTH_DATE, Range(1, 13, 19)),
            Pair(HASH2, Range(1, 19, 20)),
            Pair(SEX, Range(1, 20, 21)),
            Pair(EXPIRY_DATE, Range(1, 21, 27)),
            Pair(HASH3, Range(1, 27, 28)),
            Pair(OPTIONAL_DATA, Range(1, 28, 42)),
            Pair(HASH4, Range(1, 42, 43)),
            Pair(FINAL_HASH, Range(1, 43, 44))
        )
    }
}

