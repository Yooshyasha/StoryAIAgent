plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.yooshyasha"
version = "0.0.1-SNAPSHOT"
description = "StoryAIAgent"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

val koogVersion = "0.4.1"

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx" && requested.name.startsWith("kotlinx-coroutines")) {
            useVersion("1.10.2")
        }
        if (requested.group == "org.jetbrains.kotlinx" && requested.name.startsWith("kotlinx-serialization")) {
            useVersion("1.8.1")
        }
        if (requested.group == "io.modelcontextprotocol" && requested.name == "kotlin-sdk") {
            useVersion("0.4.0")
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("ai.koog:koog-agents:$koogVersion")
    implementation("ai.koog:koog-spring-boot-starter:$koogVersion")

    implementation(kotlin("test"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
