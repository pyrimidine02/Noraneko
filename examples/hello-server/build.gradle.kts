plugins {
    application
}

dependencies {
    implementation(project(":noraneko-core"))
    implementation(project(":noraneko-netty"))
}

application {
    mainClass.set("MainKt")
}
