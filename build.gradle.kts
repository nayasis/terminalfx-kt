import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`java`
	`maven-publish`
	kotlin("jvm") version "1.9.20"
	id("org.openjfx.javafxplugin") version "0.0.14"
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
	withJavadocJar()
	withSourcesJar()
}

javafx {
	version = "19.0.2.1"
	modules = listOf("javafx.controls","javafx.web","javafx.swing")
}

group = "com.github.nayasis"
version = "0.0.1-SNAPSHOT"
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
	implementation("com.github.nayasis:basica-kt:0.3.1")
	implementation("com.github.nayasis:basicafx-kt:0.2.1")
	implementation("no.tornado:tornadofx:1.7.20")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
	implementation("org.jetbrains.pty4j:pty4j:0.12.10")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:2.0.10")
	implementation("au.com.console:kassava:2.1.0")
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