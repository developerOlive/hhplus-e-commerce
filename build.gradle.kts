plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("jacoco")
}

val querydslDir = "build/generated/querydsl"

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

    // Docs
    implementation(libs.springdoc.openapi)

    // Test
    testImplementation(libs.spring.boot.starter.test) {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation(libs.rest.assured)
    testImplementation(libs.rest.assured.json.path)
    testImplementation(libs.rest.assured.schema)
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:mysql:1.19.3")
    testImplementation("org.instancio:instancio-junit:2.15.0")
    testImplementation("org.instancio:instancio-core:2.15.0")
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    // DB
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    runtimeOnly("com.mysql:mysql-connector-j")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

with(extensions.getByType(JacocoPluginExtension::class.java)) {
    toolVersion = "0.8.7"
}

sourceSets["main"].java.srcDirs(querydslDir)

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.annotationProcessorGeneratedSourcesDirectory = file(querydslDir)
}

tasks.test {
    ignoreFailures = true
    useJUnitPlatform()
    testLogging {
        events("failed", "skipped", "passed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

configurations.all {
    exclude("org.apache.logging.log4j", "log4j-slf4j2-impl")
    exclude("org.apache.logging.log4j", "log4j-to-slf4j")
}
