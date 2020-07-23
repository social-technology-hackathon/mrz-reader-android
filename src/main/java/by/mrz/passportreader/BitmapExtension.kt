package by.mrz.passportreader

import android.graphics.*
import by.mrz.passportreader.models.MrzBitmapInformation
import io.reactivex.Single

fun Bitmap.crop(mrz: MrzBitmapInformation): Single<Bitmap> {
    val source = this
    val matrix = Matrix()
    matrix.postRotate(mrz.angle, source.width / 2f, source.height / 2f)

    return Single.fromCallable {
        Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
            .let { result ->
                val rect = matrix.calculateZone(source, result, mrz.corners, mrz.text)
                Bitmap.createBitmap(result, rect.left, rect.top, rect.width(), rect.height())
            }
    }
}


fun Bitmap.rotate(degrees: Float): Bitmap {
    val source = this
    val matrix = Matrix()
    matrix.postRotate(degrees, source.width / 2f, source.height / 2f)

    return Bitmap.createBitmap(
        source,
        0,
        0,
        source.width,
        source.height,
        matrix,
        true
    )
}

fun Bitmap.grayout(
    redVal: Float = 0.299f,
    greenVal: Float = 0.587f,
    blueVal: Float = 0.114f
): Bitmap {
    // create output bitmap
    val bmOut = Bitmap.createBitmap(width, height, config)
    // pixel information
    var A: Int
    var R: Int
    var G: Int
    var B: Int
    var pixel: Int
    // get image size
    val width = width
    val height = height
    // scan through every single pixel
    for (x in 0 until width) {
        for (y in 0 until height) {
            // get one pixel color
            pixel = getPixel(x, y)
            // retrieve color of all channels
            A = Color.alpha(pixel)
            R = Color.red(pixel)
            G = Color.green(pixel)
            B = Color.blue(pixel)
            // take conversion up to one single value
            B = (redVal * R + greenVal * G + blueVal * B).toInt()
            G = B
            R = G
            // set new pixel color to output bitmap
            bmOut.setPixel(x, y, Color.argb(A, R, G, B))
        }
    }
    // return final image
    return bmOut
}