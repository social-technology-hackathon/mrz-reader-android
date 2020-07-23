package by.mrz.passportreader.models

data class PassportInformation(
    val documentNumber: String,
    val documentCode: String,

    val firstName: String,
    val lastName: String,

    val country: String,
    val nationality: String,
    val sex: String,

    val dateOfBirth: String,
    val expirationDate: String
)