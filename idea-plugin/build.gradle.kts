plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  id("org.jetbrains.intellij") version "1.17.0"
}

group = "in.stock.core.di.idea.plugin"
version = "unspecified"

repositories {
  mavenCentral()
  mavenLocal()
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(projects.kotlinDiCompiler)
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version = "2023.2.5"
  plugins = listOf("Kotlin")
  updateSinceUntilBuild = false
}

tasks {

  patchPluginXml {
    sinceBuild.set("223")
//    untilBuild.set("242.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}
