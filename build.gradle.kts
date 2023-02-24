import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.joor.Reflect.wrapper

plugins {
	`java`
	`maven-publish`
	kotlin("jvm") version "1.8.10"
	kotlin("plugin.jpa") version "1.8.10"
	kotlin("plugin.noarg") version "1.8.10"
	kotlin("plugin.allopen") version "1.8.10"

	// javafx
	application
	id("org.openjfx.javafxplugin") version "0.0.10"
}

allOpen {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}
noArg {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
	annotation("com.github.nayasis.kotlin.spring.kotlin.annotation.NoArg")
	invokeInitializers = true
}

javafx {
	version = "19.0.2.1"
	modules = listOf("javafx.controls","javafx.web","javafx.swing")
}

group = "com.github.nayasis"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

java {
	withJavadocJar()
	withSourcesJar()
}

configurations.all {
	resolutionStrategy.cacheChangingModulesFor(0, "seconds")
	resolutionStrategy.cacheDynamicVersionsFor(5, "minutes")
}

repositories {
	mavenLocal()
	mavenCentral()
	jcenter()
	maven { url = uri("https://jitpack.io") }
}

dependencies {

	// common
	implementation("com.github.nayasis:basica-kt:0.2.16")
	implementation("com.github.nayasis:basicafx-kt:0.1.18")
	implementation("no.tornado:tornadofx:1.7.20")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
	implementation("org.jetbrains.pty4j:pty4j:0.12.10")

	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:2.0.10")
	implementation("au.com.console:kassava:2.1.0")

	// test
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.1")
	testImplementation("ch.qos.logback:logback-classic:1.2.9")

}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf(
			"-Xjsr305=strict"
		)
		jvmTarget = "11"
	}
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
		}
	}
}