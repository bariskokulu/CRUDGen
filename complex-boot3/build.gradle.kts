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

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

dependencies {
    compileOnly(project(":lib"))
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:3.2.8"))
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.boot:spring-boot-starter-data-mongodb")
    compileOnly("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("com.flipkart.zjsonpatch:zjsonpatch:0.4.16")
    compileOnly("org.mapstruct:mapstruct:1.5.5.Final")
    compileOnly("io.swagger.core.v3:swagger-annotations-jakarta:2.2.22")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    annotationProcessor(project(":lib"))
}
