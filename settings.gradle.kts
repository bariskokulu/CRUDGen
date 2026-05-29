plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "CrudGen"
include("lib")
include("simple-boot3")
include("simple-boot4")
include("complex-boot3")
include("complex-boot4")
