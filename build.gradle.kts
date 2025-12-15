plugins {
    id("java")
    id("application")
}

group = "me.namila.project.text_render"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    mainClass.set("me.namila.project.text_render.BulkTextRendererApp")
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Context (DI only)
    implementation("org.springframework:spring-context:6.1.14")
    
    // Picocli CLI
    implementation("info.picocli:picocli:4.7.6")
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")
    
    // OpenPDF for PDF manipulation
    implementation("com.github.librepdf:openpdf:2.0.3")
    
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Fat JAR configuration
tasks.jar {
    manifest {
        attributes["Main-Class"] = "me.namila.project.text_render.BulkTextRendererApp"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}