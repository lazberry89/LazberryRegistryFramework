plugins {
    id("java-library")
    id("io.github.goooler.shadow") version "8.1.8"
}

dependencies {
    implementation(project(":lrf-common"))
    implementation("net.bytebuddy:byte-buddy:1.18.11")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

tasks {
    test {
        useJUnitPlatform()
    }
    shadowJar {
        archiveFileName.set("LazberryRegistryFramework.jar")
    }
    build {
        dependsOn(shadowJar)
    }
}