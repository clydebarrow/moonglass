/*
 * Copyright (c) 2021. Clyde Stubbs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Properties

plugins {
    kotlin("js") version "1.5.30"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.30"
    idea
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
    maven { url = uri("https://dl.bintray.com/korlibs/korlibs") }
}

/** TODO
 * Move this into buildSrc
 */
open class PropertiesFile(private val file: File) {
    val props = Properties().apply {
        file.inputStream().use { load(it) }
    }

    operator fun get(name: String): String? = (props[name] as? String)
}

val localProperties get() = PropertiesFile(File("local.properties"))

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
                devServer?.apply {
                    open = localProperties["openBrowser"] != "false"
                    localProperties["nvrHost"]?.also {
                        proxy = mutableMapOf(
                            "/api" to mapOf(
                                "target" to it,
                                "ws" to true
                            )
                        )
                    }
                }
            }
        }
    }
}

val ktorVersion = "1.6.3"

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:1.2.2")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.236-kotlin-1.5.30")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.236-kotlin-1.5.30")
    implementation("io.ktor:ktor-client-js:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.0-pre.236-kotlin-1.5.30")
    implementation("com.soywiz.korlibs.krypto:krypto-js:2.4.0")

    implementation(npm("react", "17.0.2"))
    implementation(npm("react-dom", "17.0.2"))
    implementation(npm("react-calendar", "3.4.0"))
    implementation(npm("styled-components", "~5.2.3"))

}

// set up deployment if configured.

localProperties["deployTarget"]?.also { deployTarget ->
    tasks.register<Exec>("deploy") {
        dependsOn("browserProductionWebpack")
        workingDir(File(projectDir, "build/distributions"))
        commandLine("sh", "-c", "scp -r * '$deployTarget'")
    }
}

kotlin {
    sourceSets.all {
        languageSettings.apply {
            useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        }
    }
}
