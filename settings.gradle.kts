plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "CrudGen"
include("lib")
include("samples:simple-boot3")
include("samples:simple-boot4")
include("samples:complex-boot3")
include("samples:complex-boot4")
