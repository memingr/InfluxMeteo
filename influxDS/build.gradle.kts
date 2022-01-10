plugins {
    kotlin("jvm") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.1.1"
    application
    java
}

tasks {
    compileJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
}

repositories {
    flatDir {
        dirs(
            "c://Jaspersoft/jasperreports-server-cp-8.0.0/apache-tomcat/webapps/jasperserver/WEB-INF/lib"
        )
    }
    mavenCentral()
}

dependencies {
    compileOnly("net.sf.jasperreports:jasperreports:6.18.1")
    // https://mvnrepository.com/artifact/net.sf.jasperreports/jasperreports-annotation-processors
    compileOnly("net.sf.jasperreports:jasperreports-annotation-processors:6.18.1")

    implementation("com.influxdb:influxdb-client-kotlin:3.1.0")

    compileOnly(":jasperserver-api-metadata:8.0.0")
    compileOnly(":jasperserver-api-engine:8.0.0")
    compileOnly(":jasperserver-api-engine-impl:8.0.0")
    compileOnly(":jasperserver-api-common:8.0.0")
    compileOnly(":hibernate-core:5.2.12.Final")
    compileOnly(":hibernate-jpa-2.1-api-1.0.0.Final")
    compileOnly(":spring-context:5.2.8.RELEASE")
    compileOnly(":spring-orm:5.2.8.RELEASE")
    compileOnly(":spring-core:5.2.8.RELEASE")
    compileOnly(":spring-beans:5.2.8.RELEASE")
    compileOnly(":spring-security-acl-4.2.19.RELEASE")
    compileOnly(":spring-tx-5.2.8.RELEASE")
    compileOnly("commons-logging:commons-logging:1.2")
    compileOnly("org.slf4j:slf4j-api:1.7.32")

    //
    compileOnly(":jasperserver-custom-datasources-8.0.0")


}

tasks.test {
    useTestNG()
}


application {
    mainClass.set("online.grimen.influxds.InfluxDataSourceKt")
}