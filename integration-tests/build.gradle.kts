plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.ksp)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(projects.di.runtime)
    implementation(libs.kotlin.inject.runtime)
    ksp(projects.di.compiler)
    ksp(libs.kotlin.inject.compiler)

    testImplementation(libs.kotlin.inject.compiler)
    testImplementation(projects.di.compiler)
    testImplementation(libs.ksp.testing)
    testImplementation(libs.koTest)
}

