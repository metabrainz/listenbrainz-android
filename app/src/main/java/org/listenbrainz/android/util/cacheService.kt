import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
                    println(json)
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
            val data = json.split("},")
                .filter { it.isNotEmpty() }
                .map { "$it}" }
                .map {
                    val fixedJson = it.replace("[","").replace("]","")
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
            val fileOutputStream = FileOutputStream(file)
            value.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
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
