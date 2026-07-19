allprojects {
    group = "org.lazberry"
    version = "1.0.0-BETA"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    // 💡 안전하게 문자열로 자바 플러그인을 주입해 꼬임을 방지합니다.
    apply(plugin = "java")

    dependencies {
        val lombokVersion = "1.18.46"
        "compileOnly"("org.projectlombok:lombok:$lombokVersion")
        "annotationProcessor"("org.projectlombok:lombok:$lombokVersion")
    }

    // 💡 java { ... } 블록 대신 기존 프로젝트처럼 컴파일 타겟을 직접 명시하는 안전한 기법!
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        targetCompatibility = "21"
        sourceCompatibility = "21"
    }
}