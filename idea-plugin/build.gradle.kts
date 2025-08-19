plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.builder"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

intellij {
    version.set("2024.1")
    type.set("IC") // IntelliJ IDEA Community Edition
    
    plugins.set(listOf(
        "java"
    ))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
} 