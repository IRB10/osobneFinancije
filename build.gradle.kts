import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.1.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.7.RELEASE"
	kotlin("jvm") version "1.2.71"
	kotlin("plugin.spring") version "1.2.71"
}

group = "com.diplomski"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-mustache")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-config")
	implementation("org.springframework.security:spring-security-web")
	implementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.1.7.RELEASE")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.71")
	implementation("org.jetbrains.kotlin:kotlin-noarg:1.2.71")
	implementation("com.amazonaws:aws-java-sdk-lambda:1.11.546")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.passay:passay:1.3.1")
	implementation("com.google.guava:guava:28.0-jre")
	implementation("org.json:json:20180813")
	implementation("org.apache.poi:poi:3.17")
	implementation("org.apache.poi:poi-ooxml:3.15")
	implementation("com.lowagie:itext:4.2.2")
	implementation("io.springfox:springfox-swagger2:2.9.2")
	implementation("io.springfox:springfox-swagger-ui:2.9.2")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.github.davidmoten:rxjava2-jdbc:0.2.5")
	implementation("org.junit.jupiter:junit-jupiter-engine:5.1.0")
	runtimeOnly("mysql:mysql-connector-java")
	runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.6")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test"){
		exclude(module = "junit")
	}
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
