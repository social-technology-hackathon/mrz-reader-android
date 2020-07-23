package by.mrz.passportreader.models

import android.graphics.Point


data class MrzBitmapInformation(
    val corners: Array<Point>,
    val angle: Float,
    val text: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MrzBitmapInformation

        if (!corners.contentEquals(other.corners)) return false
        if (angle != other.angle) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = corners.contentHashCode()
        result = 31 * result + angle.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }

}