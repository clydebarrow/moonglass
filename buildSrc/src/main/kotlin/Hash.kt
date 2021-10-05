import java.io.File
import java.security.MessageDigest

object Hash {

    fun File.hash(type: String = "SHA-256"): String {
        val digest = MessageDigest
            .getInstance(type)
        val buffer = ByteArray(65536)
        var count = 0L
        inputStream().use {
            do {
                val len = it.read(buffer)
                if (len > 0) {
                    count += len
                    digest.update(buffer, 0, len)
                }
            } while (len > 0)
        }
        val bytes = digest.digest()
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
