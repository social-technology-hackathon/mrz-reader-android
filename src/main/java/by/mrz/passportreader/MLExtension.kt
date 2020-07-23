package by.mrz.passportreader

import android.graphics.Point
import by.mrz.passportreader.models.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import io.reactivex.Single
import kotlin.math.atan2

fun TextRecognizer.decode(image: InputImage): Single<Text> {
    return Single.create { emitter ->
        process(image)
            .addOnSuccessListener { text ->
                emitter.onSuccess(text)
            }
            .addOnFailureListener { exception ->
                emitter.onError(exception)
            }
    }
}

fun Text.parse(): Single<PassportInformation> {
    return Single.fromCallable {
        val mrz = MrzParser.build(text)
        mrz.passportInformation
    }
}

fun Text.findBlocks(): Single<MrzBitmapInformation> {
    return Single.fromCallable {
        val blocks = textBlocks.filterNotNull()
        val mrzBlock = blocks.firstOrNull { it.text.contains("<<<<<<") }
            ?: throw (Throwable("No MRZ blocks"))
        val cornerPoints = mrzBlock.cornerPoints
            ?: throw Throwable("No MRZ blocks")
        val angle = cornerPoints.angleToNormalizeImage()

        MrzBitmapInformation(
            cornerPoints,
            angle,
            mrzBlock.text
        )
    }
}

private fun Array<Point>.angleToNormalizeImage(): Float {
    val dx = (this[1].x - this[0].x).toFloat()
    val dy = (this[0].y - this[1].y).toFloat()

    return (atan2(dy, dx) * 180 / Math.PI)
        .let { degrees ->
            if (degrees > 90) {
                450 - degrees;
            } else {
                90 - degrees;
            }
        }
        .let { degrees -> 90 - degrees }
        .toFloat()
}