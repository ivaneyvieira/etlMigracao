
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.0"

    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    // Use JCenter for resolving dependencies.
    jcenter()
    mavenCentral()
}

tasks {
    named<ShadowJar>("shadowJar") {
        manifest {
            attributes(mapOf("Main-Class" to "etlMigracao.AppKt"))
        }
    }
}


dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:29.0-jre")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")


    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // https://mvnrepository.com/artifact/com.oracle.database.jdbc/ojdbc8
    implementation("com.oracle.database.jdbc:ojdbc8:21.1.0.0")
    // https://mvnrepository.com/artifact/mysql/mysql-connector-java
    implementation("mysql:mysql-connector-java:5.1.49")
    // https://mvnrepository.com/artifact/org.sql2o/sql2o
    implementation("org.sql2o:sql2o:1.6.0")
}

