import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
    id("com.squareup.sqldelight") version "1.5.3"
}

group = "com.xaluoqone"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.1")
    implementation("com.squareup.okio:okio:3.1.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.squareup.sqldelight:sqlite-driver:1.5.3")
}

sqldelight {
    database("Database") {
        packageName = "com.xaluoqone.publisher"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

compose.desktop {
    application {
        mainClass = "com.xaluoqone.publisher.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Publisher"
            packageVersion = "1.0.1"
            windows {
                upgradeUuid = "D4E420AE-72B7-4B67-873A-FE29C423309E"
                menuGroup = "xaluoqone"
                iconFile.set(project.file("src/main/resources/icon.ico"))
            }
        }
    }
}