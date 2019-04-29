plugins {
    kotlin("multiplatform") version "1.3.31"
    `maven-publish`
    jacoco
}

group = "com.pajato"
version = "0.0.1"

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
}

kotlin {

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("com.pajato:argus-tmdb-core:0.0.8")
            }
        }
        commonTest {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }

        jvm("jvm").compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.31")
                implementation("com.pajato:argus-tmdb-core-jvm:0.0.7")
            }
        }

        jvm("jvm").compilations["test"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
                implementation("io.javalin:javalin:2.8.0")
                implementation("org.slf4j:slf4j-simple:1.7.26")
            }
        }
    }
}

// for future functionality related to using the available TmdbData ids.
task("generateSecureProperties") {
    doLast {
        File("$projectDir/src/commonMain/resources", "secureProps.txt").apply {
            val apiKey = project.property("argus_tmdb_api_key") ?: "invalid!"
            writeText(apiKey.toString())
        }
    }
}

tasks.register<Copy>("copyTestResources") {
    from(file("$projectDir/src/jvmTest/resources/"))
    into(file("$buildDir/classes/kotlin/jvm/test/"))
}

tasks.get(name = "jvmTest").dependsOn += tasks.get(name = "copyTestResources")

jacoco {
    toolVersion = "0.8.3"
}

tasks {
    val coverage = register<JacocoReport>("jacocoJVMTestReport") {
        group = "Reporting"
        description = "Generate Jacoco coverage report."
        classDirectories.setFrom(fileTree("$buildDir/classes/kotlin/jvm/main"))
        val coverageSourceDirs = listOf("src/commonMain/kotlin", "src/jvmMain/kotlin")
        additionalSourceDirs.setFrom(files(coverageSourceDirs))
        sourceDirectories.setFrom(files(coverageSourceDirs))
        executionData.setFrom(files("$buildDir/jacoco/jvmTest.exec"))
        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }
    named("jvmTest") {
        finalizedBy(coverage)
    }
}