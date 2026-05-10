plugins {
    kotlin("jvm") version "2.0.21" apply false
}

allprojects {
    group = "cc.noraneko"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // Repositories are declared in settings.gradle.kts because project repositories are disabled there.
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
