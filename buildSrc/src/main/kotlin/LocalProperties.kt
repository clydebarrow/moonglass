import java.io.File
import java.util.Properties

open class PropertiesFile(private val file: File) {
    val props = Properties().apply {
        file.inputStream().use { load(it) }
    }

    operator fun get(name: String): String? = (props[name] as? String)
}

val localProperties get() = PropertiesFile(File("local.properties"))

