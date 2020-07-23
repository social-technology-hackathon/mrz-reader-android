package by.mrz.passportreader

import android.graphics.*
import by.mrz.passportreader.models.Constants.MRZ_BOX_DENSITY
import by.mrz.passportreader.models.Constants.MRZ_CHARS_IN_LINE
import kotlin.math.max
import kotlin.math.min


fun Matrix.calculateZone(
    source: Bitmap, result: Bitmap, points: Array<Point>,
    text: String
): Rect {
    val dx = (result.width - source.width) / 2
    val dy = (result.height - source.height) / 2

    val convert: (point: Point) -> Point = { point ->
        val array = floatArrayOf(point.x.toFloat(), point.y.toFloat())
        this.mapPoints(array)
        Point(
            array[0].toInt() + dx,
            array[1].toInt() + dy
        )
    }
    val topLeft = convert(points[0])
    val bottomRight = convert(points[2])
    val mrzCharsInBlock = text.length
    val lineHeight = (bottomRight.y - topLeft.y)
    val widthRate = min(1f, MRZ_CHARS_IN_LINE / mrzCharsInBlock.toFloat())
    val width =
        ((bottomRight.x - topLeft.x).toFloat() * widthRate + 2f * lineHeight).toInt()

    val x = max(0, topLeft.x - lineHeight)
        .let { x ->
            when {
                text.startsWith("P<") -> {
                    x
                }
                widthRate >= 1f -> {
                    x
                }
                else -> {
                    (x - (lineHeight * mrzCharsInBlock) / 2f).toInt()
                }
            }
        }
        .let { x ->
            if (x + width >= result.width) {
                result.width - width
            } else {
                x
            }
        }

    val y = max(0, topLeft.y - lineHeight)

    val height = (lineHeight + width / MRZ_BOX_DENSITY).toInt()

    return Rect(x, y, x + width, y + height)
}