tasks.register("compileAllExamples") {
    group = "verification"
    description = "Compiles all four sample modules (simple/complex × Boot 3/4)."
    dependsOn(
        ":samples:simple-boot3:compileJava",
        ":samples:simple-boot4:compileJava",
        ":samples:complex-boot3:compileJava",
        ":samples:complex-boot4:compileJava"
    )
}

tasks.register("testAllExamples") {
    group = "verification"
    description = "Runs Spring Boot integration tests for all four sample modules."
    dependsOn(
        ":samples:simple-boot3:test",
        ":samples:simple-boot4:test",
        ":samples:complex-boot3:test",
        ":samples:complex-boot4:test"
    )
}

tasks.register("verifyAllExamples") {
    group = "verification"
    description = "compileAllExamples + testAllExamples (processor + runtime smoke)."
    dependsOn("compileAllExamples", "testAllExamples")
}
