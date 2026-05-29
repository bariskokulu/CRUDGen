plugins {
    `java-library`
    signing
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.bariskokulu"
version = "1.1.0"
val artifactId = "crudgen"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("com.squareup:javapoet:1.13.0")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

mavenPublishing {
    coordinates(group.toString(), artifactId, version.toString())
    publishToMavenCentral()
    signAllPublications()
    pom {
        name.set("CRUDGen")
        description.set("Annotation processor that generates Controllers for use cases and CRUD layers for models.")
        inceptionYear.set("2025")
        url.set("https://github.com/bariskokulu/CRUDGen")
        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("bariskokulu")
                name.set("Baris Kokulu")
                email.set("bariskokulu@gmail.com")
            }
        }
        scm {
            url.set("https://github.com/bariskokulu/CRUDGen")
            connection.set("scm:git:git://github.com/bariskokulu/CRUDGen.git")
            developerConnection.set("scm:git:ssh://git@github.com/bariskokulu/CRUDGen.git")
        }
    }
}