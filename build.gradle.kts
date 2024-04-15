plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("java")
}

group = "com.ccatchings"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.hibernate.orm:hibernate-core:6.4.2.Final")
    implementation("org.slf4j:slf4j-simple:1.7.5")
    implementation("org.postgresql:postgresql:42.5.1")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.security:spring-security-web")
    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.keycloak:keycloak-common:23.0.6")
    implementation("org.keycloak:keycloak-core:23.0.6")
    implementation("org.keycloak:keycloak-policy-enforcer:23.0.6")
    implementation(platform("org.springframework.data:spring-data-bom:2023.1.2"))
    implementation("org.springframework.data:spring-data-jpa")
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-suite-api:1.10.2")
    testImplementation("com.c4-soft.springaddons:spring-addons-webmvc-test:6.2.3")
    testImplementation("org.keycloak:keycloak-test-helper:23.0.6")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}




