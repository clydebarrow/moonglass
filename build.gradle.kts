import java.io.File
import java.util.Properties

plugins {
    kotlin("js") version "1.5.30"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.30"
}


open class PropertiesFile(private val file: File) {
    val props = Properties().apply {
        file.inputStream().use { load(it) }
    }

    inline operator fun <reified T : Any> get(name: String): T {
        return (props[name] as? T) ?: error("Property $name not found")
    }

    private fun save(comment: String) {
        file.outputStream().bufferedWriter().use {
            props.store(it, comment)
        }
    }

    operator fun set(name: String, value: Any) {
        props[name] = value.toString()
        save("Updated name")
    }

    fun set(values: Map<String, Any>) {
        values.forEach { (name, value) -> props[name] = value.toString() }
        save("Updated ${values.toList().joinToString { "${it.first}=${it.second}" }}")
    }
}

val localProperties get() = PropertiesFile(File("local.properties"))

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
    //maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
    //maven("https://dl.bintray.com/kotlin/kotlin-eap")
    jcenter()
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
                devServer?.apply {
                    val nvrHost:String = localProperties["nvrHost"]
                    if(nvrHost != null) {
                        proxy = mutableMapOf(
                            "/api" to mapOf(
                                "target" to nvrHost,
                                "ws" to true
                            )
                        )
                    }
                }
            }
        }
        binaries.executable()
    }
}

val ktor_version = "1.6.3"
dependencies {

    //React, React DOM + Wrappers (chapter 3)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:1.2.2")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.236-kotlin-1.5.30")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.236-kotlin-1.5.30")
    implementation("io.ktor:ktor-client-js:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation(npm("react", "17.0.2"))
    implementation(npm("react-dom", "17.0.2"))
    implementation(npm("react-time-picker", "4.4.1"))

    //Kotlin Styled (chapter 3)
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.0-pre.236-kotlin-1.5.30")
    implementation(npm("styled-components", "~5.2.3"))

    //Video Player (chapter 7)
    implementation(npm("react-youtube-lite", "1.0.1"))

    //Share Buttons (chapter 7)
    implementation(npm("react-share", "~4.2.1"))
    implementation(npm("react-calendar", "3.4.0"))

    //Coroutines (chapter 8)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
}

// Heroku Deployment (chapter 9)
tasks.register("stage") {
    dependsOn("build")
}
kotlin {
    sourceSets.all {
        languageSettings.apply {
            useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        }
    }
}
