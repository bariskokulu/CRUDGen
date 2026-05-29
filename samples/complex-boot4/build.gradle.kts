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

tasks.test {
    useJUnitPlatform()
}

dependencies {
    compileOnly(project(":lib"))
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:4.0.5"))
    compileOnly("org.springframework.boot:spring-boot-starter-webmvc")
    compileOnly("org.springframework.boot:spring-boot-starter-json")
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

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.5"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("io.github.vishwakarma:zjsonpatch:0.6.2")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    testImplementation("org.mapstruct:mapstruct:1.6.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
