plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

springBoot {
    mainClass = "com.insider.WebUiTestApplication"
}

val springBootVersion = "3.2.0"
val seleniumVersion = "4.15.0"
val webDriverManagerVersion = "5.6.2"
val commonsLang3Version = "3.13.0"
val jacksonVersion = "2.16.0"
val junitVersion = "5.10.0"
val mockitoVersion = "5.7.0"
val lombokVersion = "1.18.30"
val googleGenAIVersion = "1.3.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}


dependencies {
    annotationProcessor(project(":processor"))
    implementation(project(":processor")) {
        exclude(group = "org.seleniumhq.selenium", module = "selenium-java")
    }

    implementation(
        group = "org.springframework.boot",
        name = "spring-boot-starter",
        version = springBootVersion
    )
    implementation(
        group = "org.springframework.boot",
        name = "spring-boot-starter-test",
        version = springBootVersion
    )
    implementation(
        group = "org.springframework",
        name = "spring-context"
    )
    implementation(
        group = "org.springframework",
        name = "spring-test"
    )
    
    // Lombok
    compileOnly(
        group = "org.projectlombok",
        name = "lombok",
        version = lombokVersion
    )
    annotationProcessor(
        group = "org.projectlombok",
        name = "lombok",
        version = lombokVersion
    )
    testCompileOnly(
        group = "org.projectlombok",
        name = "lombok",
        version = lombokVersion
    )
    testAnnotationProcessor(
        group = "org.projectlombok",
        name = "lombok",
        version = lombokVersion
    )
    
    implementation(
        group = "org.seleniumhq.selenium",
        name = "selenium-java",
        version = seleniumVersion
    )
    implementation(
        group = "io.github.bonigarcia",
        name = "webdrivermanager",
        version = webDriverManagerVersion
    )
    implementation(
        group = "org.apache.commons",
        name = "commons-lang3",
        version = commonsLang3Version
    )
    implementation(
        group = "com.fasterxml.jackson.core",
        name = "jackson-databind",
        version = jacksonVersion
    )
    
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter",
        version = junitVersion
    )
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = junitVersion
    )
    testImplementation(
        group = "org.springframework.boot",
        name = "spring-boot-starter-test",
        version = springBootVersion
    )
    testImplementation(
        group = "org.mockito",
        name = "mockito-core",
        version = mockitoVersion
    )
    testImplementation(
        group = "org.mockito",
        name = "mockito-junit-jupiter",
        version = mockitoVersion
    )
    implementation(
        group = "com.google.genai",
        name = "google-genai",
        version = googleGenAIVersion
    )
}

val generatedSourcesDir = "$layout.buildDirectory/generated"
tasks.named<JavaCompile>("compileJava") {
    options.isFork = true
    options.compilerArgs.addAll(listOf("-Xlint:-unchecked", "-nowarn"))
    options.sourcepath = project.files("src/main/java")

    val workspaceProperty = System.getProperty("WORKSPACE", "LOCAL")
    val isJenkins = "WORKSPACE" in workspaceProperty

    if (isJenkins) {
        options.generatedSourceOutputDirectory.set(File(generatedSourcesDir))
        outputs.dir(generatedSourcesDir)
    }
}