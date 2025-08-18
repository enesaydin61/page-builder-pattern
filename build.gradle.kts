plugins {
    java
    id("java-library")
}

allprojects {
    group = "com.annotation.test"
    version = "1.0.0"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    }
    
    tasks.test {
        useJUnitPlatform()
    }
} 