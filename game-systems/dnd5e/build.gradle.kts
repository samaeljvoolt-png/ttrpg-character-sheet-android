plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core:domain"))

    testImplementation(libs.junit)
}

kotlin {
    jvmToolchain(17)
}
