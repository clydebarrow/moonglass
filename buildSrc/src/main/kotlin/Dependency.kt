open class Dependency(val prefix: String, val artifact: String, val version: String) : Invokable {
    override operator fun invoke(): String = "$prefix:$artifact:$version"

    val path = "$prefix:$artifact"
    fun variant(art: String, vers: String = version) = Dependency(prefix, art, vers)
}

interface Invokable {
    operator fun invoke(): String
}

open class KotlinDependency(val artifact: String) : Invokable {
    override operator fun invoke(): String = "org.jetbrains.kotlin:$artifact"

    fun variant(art: String) = KotlinDependency(art)
}

