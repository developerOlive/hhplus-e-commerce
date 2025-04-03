plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("jacoco")
}

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allprojects {
    group = "com.hhplusecommerce"
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.cloud.dependencies.get().toString())
    }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
    testImplementation(libs.spring.boot.starter.test)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
}

// about source and compilation
java {
    sourceCompatibility = JavaVersion.VERSION_17
}

with(extensions.getByType(JacocoPluginExtension::class.java)) {
    toolVersion = "0.8.7"
}

// bundling tasks
tasks.getByName("bootJar") {
    enabled = true
}
tasks.getByName("jar") {
    enabled = false
}

// test tasks
tasks.test {
    ignoreFailures = true
    useJUnitPlatform()
}

