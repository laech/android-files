package l.files.features.objects

import android.app.Instrumentation
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

import android.os.Environment.getExternalStorageDirectory
import android.os.Looper.getMainLooper
import android.os.Looper.myLooper
import android.os.SystemClock.sleep
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit.SECONDS
import junit.framework.Assert.assertTrue

private class InstrumentCallable<T>(
        private val instrumentation: Instrumentation,
        private val delegate: () -> T) : () -> T {

    override fun invoke(): T {
        var result: T = null
        var error: Throwable? = null
        val code: () -> Unit = {
            try {
                result = delegate()
            } catch (e: Exception) {
                error = e
            } catch (e: AssertionError) {
                error = e
            }
        }
        if (getMainLooper() == myLooper()) {
            code()
        } else {
            instrumentation.runOnMainSync(code)
        }
        if (error is AssertionError) {
            throw error as AssertionError
        }
        if (error != null) {
            throw AssertionError(error)
        }
        return result
    }

}

fun <T> Instrumentation.awaitOnMainThread(fn: () -> T) =
        await(5, SECONDS, InstrumentCallable(this, fn))

fun <T> Instrumentation.await(time: Long, unit: TimeUnit, fn: () -> T): T {
    val start = currentTimeMillis()
    val duration = unit.toMillis(time)
    var error: AssertionError? = null
    while ((start + duration) > currentTimeMillis()) {
        try {
            return fn()
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw AssertionError(e)
        } catch (e: AssertionError) {
            error = e
        }

        sleep(5)
    }
    if (error == null) {
        error = AssertionError("Timed out.")
    }
    throw takeScreenshotAndThrow(error!!)
}

private fun Instrumentation.takeScreenshotAndThrow(e: AssertionError): AssertionError {
    val file = File(getExternalStorageDirectory(), "test/failed-" +
            System.currentTimeMillis() + ".jpg")
    val parent = file.getParentFile()
    assertTrue(parent.isDirectory() || parent.mkdir())
    val screenshot = getUiAutomation().takeScreenshot()
    try {

        FileOutputStream(file).use {
            out -> screenshot.compress(JPEG, 90, out)
        }

    } catch (io: IOException) {
        val error = AssertionError("Failed to take screenshot on assertion failure. "
                + "Original assertion error is included below.", e)
        javaClass<Throwable>().getMethod("addSupressed", javaClass<Throwable>())
                .invoke(error, io)
        throw error

    } finally {
        screenshot.recycle()
    }

    throw AssertionError(e.getMessage() + "\nAssertion failed, screenshot saved " + file, e)
}
