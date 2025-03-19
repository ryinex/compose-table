plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint) apply false
}

rootProject.file(".env").readLines().forEach { line ->
    if (line.isNotBlank()) {
        val splits = line.split("=")
        val env = splits[0] to splits.subList(1, splits.size).joinToString("")
        System.setProperty(env.first, env.second)
    }
}

group = "com.ryinex.kotlin"
version = System.getProperty("VERSION_NAME") ?: ""


subprojects {
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
        filter {
            exclude("*.gradle.kts")
            exclude {
                it.file.path.contains("${buildDir}/generated/")
            }
        }
    }
}