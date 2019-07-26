// SPDX-License-Identifier: LGPL-3.0-or-later

import groovy.util.Node

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    jacoco
    id("maven-publish")
    signing
}

kotlin {

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("com.pajato.argus:argus-tmdb-core:${Versions.ARGUS_CORE}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.SERIALIZATION}")
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
                implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.KOTLIN}")
                api("com.pajato.argus:argus-tmdb-core-jvm:${Versions.ARGUS_CORE}")
            }
        }

        jvm("jvm").compilations["test"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
                implementation("io.javalin:javalin:${Versions.JAVALIN}")
                implementation("org.slf4j:slf4j-simple:1.7.26")
            }
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

// Publishing code (see argus-tmdb-core for attributions)

group = Publish.GROUP
version = Versions.ARGUS_CLIENT

val releaseRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
val snapshotRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
val javadocJar by tasks.creating(Jar::class) { archiveClassifier.value("javadoc") }
val sourcesJar by tasks.creating(Jar::class) { archiveClassifier.value("sources") }

publishing {
    publications.withType<MavenPublication>().all {
        fun customizeForMavenCentral(pom: org.gradle.api.publish.maven.MavenPom) = pom.withXml {
            fun Node.add(key: String, value: String) { appendNode(key).setValue(value) }
            fun Node.node(key: String, content: Node.() -> Unit) { appendNode(key).also(content) }
            fun addToNode(node: Node) {
                node.add("description", Publish.POM_DESCRIPTION)
                node.add("name", Publish.POM_NAME)
                node.add("url", Publish.POM_URL)
            }
            fun addOrganizationSubNode(node: Node) {
                node.node("organization") {
                    add("name", Publish.POM_ORGANIZATION_NAME)
                    add("url", Publish.POM_ORGANIZATION_URL)
                }
            }
            fun addIssuesSubNode(node: Node) {
                node.node("issueManagement") {
                    add("system", "github")
                    add("url", "https://github.com/h0tk3y/k-new-mpp-samples/issues")
                }
            }
            fun addLicensesSubNode(node: Node) {
                node.node("licenses") {
                    node("license") {
                        add("name", Publish.POM_LICENSE_NAME)
                        add("url", Publish.POM_LICENSE_URL)
                        add("distribution", Publish.POM_LICENSE_DIST)
                    }
                }
            }
            fun addSCMSubNode(node: Node) {
                node.node("scm") {
                    add("url", Publish.POM_SCM_URL)
                    add("connection", Publish.POM_SCM_CONNECTION)
                    add("developerConnection", Publish.POM_SCM_DEV_CONNECTION)
                }
            }
            fun addDevelopersSubNode(node: Node) {
                node.node("developers") {
                    node("developer") {
                        add("name", Publish.POM_DEVELOPER_NAME)
                        add("id", Publish.POM_DEVELOPER_ID)
                    }
                }
            }

            asNode().run {
                addToNode(this)
                addOrganizationSubNode(this)
                addIssuesSubNode(this)
                addLicensesSubNode(this)
                addSCMSubNode(this)
                addDevelopersSubNode(this)
            }
        }
        fun isReleaseBuild(): Boolean = !Versions.ARGUS_CLIENT.endsWith("-SNAPSHOT")
        fun getRepositoryUrl(): String = if (isReleaseBuild()) releaseRepositoryUrl else snapshotRepositoryUrl
        fun rename(id: String): String = id.replace(project.name, rootProject.name, false)

        artifactId = artifactId.replace(project.name, rootProject.name)
        artifact(javadocJar)
        customizeForMavenCentral(pom)
        @Suppress("UnstableApiUsage")
        if (isReleaseBuild()) signing.sign(this@all)

        repositories {
            maven {
                url = uri(getRepositoryUrl())
                credentials {
                    username = "${project.property("SONATYPE_NEXUS_USERNAME")}"
                    password = "${project.property("SONATYPE_NEXUS_PASSWORD")}"
                }
            }
        }
    }
}
