plugins {
    java
}

group = "com.builder"
version = "1.1.0-SNAPSHOT"


java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType(Javadoc::class.java) {
    enabled = false
}


tasks.named<JavaCompile>("compileJava") {
    options.isIncremental = true
    options.compilerArgs.addAll(listOf(
        "--add-modules", "jdk.compiler",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
    ))
    options.isFork = true
    options.forkOptions.jvmArgs?.addAll(listOf(
        "--add-modules=jdk.compiler",
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
    ))
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    val seleniumVersion = "4.22.0"
    val jupiterApiVersion = "5.10.0"

    implementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = jupiterApiVersion)
    implementation(group= "org.seleniumhq.selenium", name= "selenium-java", version= seleniumVersion){
        exclude(group="org.projectlombok", module = "lombok")
    }

    annotationProcessor(group= "com.google.auto.service", name= "auto-service", version= "1.0")
    implementation(group= "com.google.auto.service", name= "auto-service", version= "1.0")
}