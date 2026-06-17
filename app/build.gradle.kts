plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)
    implementation(libs.jackson.core)
    implementation(libs.matheclipse.core)
    implementation(libs.fastutil)
    implementation("io.vavr:vavr:1.0.0")
    implementation("org.slf4j:slf4j-nop:2.0.13")
    implementation("org.ejml:ejml-all:0.44.0")
    // implementation("org.ujmp:ujmp-core:0.3.0")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "me.LogosAcUmbra.SimpleMatrixCalculator.App"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
