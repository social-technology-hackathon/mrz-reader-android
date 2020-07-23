package by.mrz.passportreader.processor

import android.graphics.Bitmap
import by.mrz.passportreader.*
import by.mrz.passportreader.models.PassportInformation
import by.mrz.passportreader.scheduler.SchedulerProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import io.reactivex.Single


internal class ScannerImpl(private val schedulerProvider: SchedulerProvider) : Scanner {
    private val recognizer: TextRecognizer = TextRecognition.getClient()

    override fun scan(bitmap: Bitmap): Single<PassportInformation> {
        return findBlock(bitmap)
            .flatMap { mrzBlock -> recognizer.decode(InputImage.fromBitmap(mrzBlock, 0)) }
            .flatMap { text ->
                text.parse()
            }
            .observeOn(schedulerProvider.ui())
    }

    private fun findBlocks(bitmap: Bitmap): Single<Bitmap> {
        return recognizer.decode(InputImage.fromBitmap(bitmap, 0))
            .flatMap { text ->
                text.findBlocks()
            }
            .flatMap {
                bitmap.crop(it)
            }
    }

    override fun findBlock(bitmap: Bitmap): Single<Bitmap> {
        return findBlocks(bitmap)
            .onErrorResumeNext(findBlocks(bitmap.rotate(90f)))
            .onErrorResumeNext(findBlocks(bitmap.rotate(180f)))
            .onErrorResumeNext(findBlocks(bitmap.rotate(270f)))
            .subscribeOn(schedulerProvider.computation())
    }


}












