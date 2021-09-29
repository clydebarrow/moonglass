/**
 *  The presence of settings.gradle.kts marks this as a stand-alone project.
 *  This *should* be a subproject of the top-level project, but if it is the browserDevelopmentRun task does not
 *  serve up the compiled Javascript. When this issue is solved, it can be brought back into the fold.
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}
rootProject.name = "moonglass-webapp"


