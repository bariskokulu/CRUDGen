tasks.register("compileAllExamples") {
    group = "verification"
    description = "Compiles all four example modules (simple/complex × Boot 3/4)."
    dependsOn(
        ":simple-boot3:compileJava",
        ":simple-boot4:compileJava",
        ":complex-boot3:compileJava",
        ":complex-boot4:compileJava"
    )
}
