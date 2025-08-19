plugins {
	java
	id("org.jetbrains.intellij") version "1.17.4"
}

intellij {
		version.set("2024.1")
		type.set("IC")
		plugins.set(listOf("java"))
	}

group = "com.builder.tools"
version = "0.1.0"

repositories {
	mavenCentral()
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

dependencies {
	// none
}

// Ensure Java 17 target to match IntelliJ Platform 2024.1 requirements
 tasks.withType<JavaCompile>().configureEach {
	options.release.set(17)
}

// Set explicit since/until build to match IU-241
tasks.patchPluginXml {
	sinceBuild.set("241")
	untilBuild.set("241.*")
} 