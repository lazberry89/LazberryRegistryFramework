plugins {
    id("java-library")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.8" apply false
}

allprojects {
    group = "org.lazberry"
    version = "v1.0.1"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }
    dependencies {
        val lombokVersion = "1.18.46"
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")
        compileOnly("com.google.auto.service:auto-service:1.1.1")
        annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        targetCompatibility = "21"
        sourceCompatibility = "21"
    }
}