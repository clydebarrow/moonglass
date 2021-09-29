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

// Top-level build file for  options common to all sub-projects/modules.


buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        listOf(
            Plugins.kotlin,
            Plugins.googleServices,
            Plugins.androidBuildTools,
            Plugins.dokka,
            Plugins.robovmPlugin
        ).forEach { classpath(it()) }
    }
}

val kotlinVersion: String = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION

println("Kotlin version used is $kotlinVersion")



//--------- Version Increment ----------//

val versionPropsFile = File("version.properties")
val buildNumber: Int by lazy {
    if (versionPropsFile.canRead()) {
        val versionProps = Properties()

        versionProps.load(versionPropsFile.inputStream())

        (versionProps["buildNumber"] as String).toInt()
    } else
        -1
}

val versionString: String by lazy {
    if (versionPropsFile.canRead()) {
        val versionProps = Properties()

        versionProps.load(versionPropsFile.inputStream())

        (versionProps["appVersion"] as String)
    } else
        "0.1"
}

fun incrementBuildNumber() {
    if (versionPropsFile.canRead()) {
        val versionProps = Properties()

        versionProps.load(versionPropsFile.inputStream())

        val oldCode = (versionProps["buildNumber"] as String).toInt()
        val code = oldCode + 1

        versionProps["buildNumber"] = code.toString()
        versionProps.store(versionPropsFile.outputStream(), "version incremented to $code")
        println("version incremented to $code")
    }
}

group = "com.controlj.moonglass"
version = versionString


allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
        //maven("https://dl.bintray.com/korlibs/korlibs")
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }
}

tasks.register("incBuild") {
    doLast {
        incrementBuildNumber()
    }
}

fun adocToHtml(fileBase: String) {
    exec {
        workingDir("${projectDir}/asciidoc")
        executable("asciidoc")
        args("$fileBase.adoc")
    }
}

tasks.register("asciidoc") {
    doLast {
        listOf("about", "licences").forEach {
            adocToHtml(it)
        }
    }
}

