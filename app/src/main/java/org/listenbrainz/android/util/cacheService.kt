import android.content.Context
import com.google.gson.Gson
import java.io.*

class CacheService<T>(private val context: Context, private val key: String) {
    fun saveData(value: T) {
        val cacheDir: File = context.cacheDir
        val gson = Gson()
        val json = gson.toJson(value)
        val file=File(cacheDir,key)
        try {
            val fileWriter= FileWriter(file,true)
            fileWriter.write("$json,")
            fileWriter.close()
        }catch (e:IOException)
        {
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
            val data = json.split("},")
                .filter { it.isNotEmpty() }
                .map { "$it}" }
                .map { Gson().fromJson(it, dataType) }
                .toSet()
                .toList()
            return data
        } catch (e: IOException) {
            println(e)
            return emptyList()
        }
    }

    fun deleteData() {
        val file = File(context.cacheDir, key)
        file.delete()
    }
}
