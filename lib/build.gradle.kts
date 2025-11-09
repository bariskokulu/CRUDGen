plugins {
    `java-library`
	signing
 //   id("com.gradleup.shadow") version "9.2.2"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.bariskokulu"
version = "1.0.2"
val artifactId = "crudgen"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("com.squareup:javapoet:1.13.0")
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
}

java {
    withSourcesJar()
   //   withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

mavenPublishing {
    coordinates(group.toString(), artifactId, version.toString())
    publishToMavenCentral()
   
  //  publishToMavenCentral(
   //     com.vanniktech.maven.publish.central.CentralPortal(
   //         username = providers.gradleProperty("mavenCentralUsername").orElse(""),
   //         password = providers.gradleProperty("mavenCentralPassword").orElse("")
   //     )
   // )
	
	  
  //  publishToMavenCentral(SonatypeHost.DEFAULT)
   
    signAllPublications()
    pom {
        name.set("CRUDGen")
        description.set("Annotation processor that generates Controllers for use cases and CRUD layers for models.")
        inceptionYear.set("2025")
        url.set("https://github.com/bariskokulu/CRUDGen")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
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