enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven {
            url =
                java.net.URI.create("https://maven.pkg.jetbrains.space/stockme/p/main/stock-me-android")
            val properties = java.util.Properties().apply {
                load(java.io.FileInputStream(File("${rootDir.absolutePath}/local.properties")))
            }
            credentials {
                username = (properties["spaceUsername"] ?: "") as String
                password = (properties["spaceToken"] ?: "") as String
            }
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
dependencyResolutionManagement {
    // Do RCA and find alternative and elegant approach
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven {
            url =
                java.net.URI.create("https://maven.pkg.jetbrains.space/stockme/p/main/stock-me-android")
            val properties = java.util.Properties().apply {
                load(java.io.FileInputStream(File("${rootDir.absolutePath}/local.properties")))
            }
            credentials {
                username = (properties["spaceUsername"] ?: "") as String
                password = (properties["spaceToken"] ?: "") as String
            }
        }
        google()
        mavenCentral()
        mavenLocal()
    }
}

include(":runtime")
include(":compiler")
include(":integration-tests")
include(":idea-plugin")
include("compiler:core")
findProject(":compiler:core")?.name = "core"
include("compiler:ksp")
findProject(":compiler:ksp")?.name = "ksp"
include("compiler:kcp")
findProject(":compiler:kcp")?.name = "kcp"
