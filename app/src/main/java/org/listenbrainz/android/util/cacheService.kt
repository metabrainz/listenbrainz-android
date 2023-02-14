import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.*

class CacheService<T>(private val context: Context, private val key: String,private val maxSize:Int=10000) {
    fun saveData(value: T, dataType: Class<T>,append:Boolean) {
        val cacheDir: File = context.cacheDir
        val gson = Gson()
        val json = gson.toJson(value)
        val file = File(cacheDir, key)
        if (!file.exists()) {
            file.createNewFile()
        }
        try {
            val data = getData(dataType)
            if (!data.contains(value)) {
                if (file.length() >= maxSize) {
                    val newData = data.toMutableList()
                    newData.removeAt(0)
                    newData.add(value)
                    var tostore=gson.toJson(newData)
                    val fileWriter = FileWriter(file, false)
                    fileWriter.write(tostore.substring(1,tostore.length-2))
                    fileWriter.close()
                } else {
                    val fileWriter = FileWriter(file, append)
                    fileWriter.write("$json,")
                    fileWriter.close()
                }
            }
        } catch (e: IOException) {
            println(e)
        }
    }

    fun getData(dataType: Class<T>): List<T> {
        try {
            val cacheDir: File = context.cacheDir
            val file = File(cacheDir, key)
            if (!file.exists()) {
                file.createNewFile()
            }
            val json = file.readText()
            if (json.isEmpty()) {
                return emptyList()
            }
            val data = json.split("},{")
                .filter { it.isNotEmpty() }
                .mapIndexed { index, it ->
                    if (index == 0) {
                        "$it}"
                    } else {
                        "{$it}"
                    }
                }
                .map {
                    val fixedJson = it.replace(",}","")
                    Gson().fromJson(fixedJson, dataType)
                }
                .toSet()
                .toList()
            return data
        } catch (e: IOException) {
            println(e)
            return emptyList()
        } catch (e: JsonSyntaxException) {
            println(e)
            return emptyList()
        }
    }

    fun deleteData() {
        val file = File(context.cacheDir, key)
        file.delete()
    }

    fun saveBitmap(value: Bitmap) {
        val cacheDir: File = context.cacheDir
        val file = File(cacheDir, key)
        if (!file.exists()) {
            file.createNewFile()
        }
        try {
            val matrix = Matrix()
            val exif = ExifInterface(file.path)

            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
            }
            val aspectRatio = value.height.toFloat() / value.width.toFloat()
            val targetHeight = value.height / 4
            val targetWidth = (targetHeight / aspectRatio).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(value, targetWidth, targetHeight, false)
            val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, targetWidth, targetHeight, matrix, true)
            val fileOutputStream = FileOutputStream(file)
            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()
        } catch (e: IOException) {
            println(e)
        }
    }


    fun getBitmap(): Bitmap? {
        val cacheDir: File = context.cacheDir
        val file = File(cacheDir, key)
        if (!file.exists()) {
            file.createNewFile()
        }
        file.inputStream().use {
            return BitmapFactory.decodeStream(it)
        }
    }

}
