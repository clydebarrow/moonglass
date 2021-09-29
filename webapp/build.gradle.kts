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

/**
 *  The presence of settings.gradle.kts marks this as a stand-alone project.
 *  This *should* be a subproject of the top-level project, but if it is the browserDevelopmentRun task does not
 *  serve up the compiled Javascript. When this issue is solved, it can be brought back into the fold.
 */
buildscript {
    dependencies {
        listOf(
            Plugins.kotlin,
        ).forEach { classpath(it()) }
    }
}

val kotlinVersion: String = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
println("Kotlin version used is $kotlinVersion")

plugins {
    kotlin("js") version Plugins.kotlin.version
    kotlin("plugin.serialization") version Plugins.kotlin.version
}

val localProperties get() = PropertiesFile(File("local.properties"))

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

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

val reactVersion = "17.0.2-pre.250-kotlin-1.5.31"
val styledVersion = "5.3.1-pre.250-kotlin-1.5.31"

val ktorVersion = "1.6.3"

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:1.2.2")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$reactVersion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$reactVersion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:$styledVersion")

    implementation("io.ktor:ktor-client-js:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.5.2")

    implementation("com.soywiz.korlibs.krypto:krypto-js:2.4.1")

    implementation(npm("react", "17.0.2"))
    implementation(npm("react-dom", "17.0.2"))
    implementation(npm("react-calendar", "3.4.0"))
    implementation(npm("styled-components", "5.3.1"))

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
