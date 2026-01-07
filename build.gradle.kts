plugins {
    kotlin("jvm") version "2.1.0"
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.11.1"
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("de.dreamcube:hornet-queen:0.3.0")
    implementation("com.google.ortools:ortools-java:9.10.4067")
    implementation("org.apache.commons:commons-math3:3.6.1")
}
