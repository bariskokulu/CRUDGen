plugins {
    java
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets {
    main {
        java.srcDir("../complex-boot3/src/main/java")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

dependencies {
    compileOnly(project(":lib"))
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:4.0.5"))
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.boot:spring-boot-starter-data-mongodb")
    compileOnly("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("io.github.vishwakarma:zjsonpatch:0.6.2")
    compileOnly("org.mapstruct:mapstruct:1.6.3")
    compileOnly("io.swagger.core.v3:swagger-annotations-jakarta:2.2.22")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    annotationProcessor(project(":lib"))
}
