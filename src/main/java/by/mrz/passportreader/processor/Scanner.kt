package by.mrz.passportreader.processor

import android.graphics.Bitmap
import by.mrz.passportreader.models.PassportInformation
import by.mrz.passportreader.scheduler.ApplicationSchedulerProvider
import io.reactivex.Single


interface Scanner {
    fun scan(bitmap: Bitmap): Single<PassportInformation>
    fun findBlock(bitmap: Bitmap): Single<Bitmap>

    companion object {
        fun instance(): Scanner = ScannerImpl(schedulerProvider = ApplicationSchedulerProvider())
    }
}












