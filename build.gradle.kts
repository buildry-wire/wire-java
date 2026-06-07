import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    // Handles Maven Central upload via the Sonatype Central Portal + GPG signing.
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "mn.wire"
version = "1.0.0"
description = "Official Java SDK for the Wire payment API"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.code.gson:gson:2.11.0")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.Javadoc(), sourcesJar = true))

    // Publishes to Maven Central through the Sonatype Central Portal.
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    // GPG signing. The plugin reads signingInMemoryKey / signingInMemoryKeyPassword from
    // Gradle properties or the ORG_GRADLE_PROJECT_* env vars set in CI; only signs when present.
    if (project.findProperty("signingInMemoryKey") != null) {
        signAllPublications()
    }

    coordinates("mn.wire", "wire-java", version.toString())

    pom {
        name.set("wire-java")
        description.set("Official Java SDK for the Wire payment API")
        url.set("https://github.com/buildry-wire/wire-java")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("buildry-wire")
                name.set("Wire")
                url.set("https://wire.mn")
            }
        }
        scm {
            connection.set("scm:git:https://github.com/buildry-wire/wire-java.git")
            developerConnection.set("scm:git:ssh://git@github.com/buildry-wire/wire-java.git")
            url.set("https://github.com/buildry-wire/wire-java")
        }
    }
}
