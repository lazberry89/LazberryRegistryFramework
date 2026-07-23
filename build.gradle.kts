plugins {
    id("java-library")
    id("io.github.goooler.shadow") version "8.1.8" apply false
}

allprojects {
    group = "org.lazberry"
    version = "1.0.0-BETA"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        val lombokVersion = "1.18.46"
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        targetCompatibility = "21"
        sourceCompatibility = "21"
    }
}