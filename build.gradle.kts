plugins {
    kotlin("jvm") version "2.3.20"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "org.westongorczyca"
version = "1.0.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}