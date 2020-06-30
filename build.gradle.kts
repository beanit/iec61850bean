import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    `maven-publish`
    signing
    eclipse
    id("biz.aQute.bnd.builder") version "5.1.1"
    id("com.diffplug.gradle.spotless") version "4.4.0"
    id("io.codearte.nexus-staging") version "0.21.2"
    id("net.ltgt.errorprone") version "1.2.1"
}

var cfgJavaVersion = JavaVersion.VERSION_1_8

val sonatypeRepository: String? by project
val sonatypeSnapshotRepository: String? by project
val sonatypeUser: String? by project
val sonatypePass: String? by project
val sonatypeStagingProfileId: String? by project

//----------- project specific configuration start --------------------

val cfgVersion = "1.9.0"
val cfgGroup = "com.beanit"
val cfgCopyToRoot = false
val cfgSignPom = true
val cfgRepository: String? = sonatypeRepository
val cfgSnapshotRepository: String? = sonatypeSnapshotRepository
val cfgRepositoryUser: String? = sonatypeUser
val cfgRepositoryPass: String? = sonatypePass
val cfgStagingProfileId: String? = sonatypeStagingProfileId
val javaProjects: Set<Project> = allprojects
val distributionProjects = javaProjects
val docProjects = javaProjects
val repositoryProjects = javaProjects
val cfgModuleName = "com.beanit.iec61850bean"

tasks.register<Tar>("tar") {
    into(project.name) {
        from("./") {
            include("build.gradle.kts")
            include("configuration.gradle.kts")
            include("settings.gradle.kts")
            include("LICENSE.txt")
            include("doc/**")
            include("bin/**")
            exclude("bin/main/")
            exclude("bin/test/")
            include("gradle/wrapper/**")
            include("gradlew")
            include("gradlew.bat")
            include("build/libs-all/**")
            include("src/**")

            include("asn1/**")
        }
    }

    into(project.name + "/doc/") {
        from("./build/docs/") {
            include("javadoc/**")
        }
    }
}


//-----java root project configurations

dependencies {
    implementation("com.beanit:asn1bean:1.12.0")
    implementation("org.slf4j:slf4j-api:1.7.25")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
}

project.extra["cfgModuleName"] = "com.beanit.iec61850bean"

tasks["jar"].withConvention(aQute.bnd.gradle.BundleTaskConvention::class) {
    bnd("""
        Bundle-Name: IEC61850bean
        Bundle-SymbolicName: ${project.extra["cfgModuleName"]}
        -exportcontents: !*.internal.*,*
        Import-Package: com.beanit.asn1bean.*,javax.net,*;resolution:=optional
    """)
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java", "src/main/java-gen"))
        }
    }
}


publishing {
    publications {
        maybeCreate<MavenPublication>("mavenJava").pom {
            name.set("IEC61850bean")
            description.set("IEC61850bean is a Java library implementing the IEC 61850 MMS communication standard for clients and servers.")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
        }
    }
}


//----------- project specific configuration end ---------------------


configure(allprojects) {
    version = cfgVersion
    group = cfgGroup
}

nexusStaging {
    packageGroup = cfgGroup
    username = cfgRepositoryUser
    password = cfgRepositoryPass
    stagingProfileId = cfgStagingProfileId
}

configure(javaProjects) {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "eclipse")
    apply(plugin = "biz.aQute.bnd.builder")
    apply(plugin = "com.diffplug.gradle.spotless")
    apply(plugin = "net.ltgt.errorprone")

    tasks.publish {
        enabled = false
    }

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = cfgJavaVersion
        targetCompatibility = cfgJavaVersion
        withSourcesJar()
        withJavadocJar()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
        testImplementation("com.tngtech.archunit:archunit-junit5:0.14.1")
        errorprone("com.google.errorprone:error_prone_core:2.4.0")
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.errorprone.excludedPaths.set(".*java-gen.*")
        if (!JavaVersion.current().isJava9Compatible) {
            options.errorprone.isEnabled.set(false)
        }
    }

    afterEvaluate {
        tasks.jar {
            manifest {
                attributes["Automatic-Module-Name"] = project.extra["cfgModuleName"]
            }
        }
    }

    spotless {
        java {
            googleJavaFormat()
        }
    }

    tasks.register<Copy>("jarAll") {
        from(configurations.runtimeClasspath) // all runtime dependencies
        from(tasks.jar) // the jar file created
        if (cfgCopyToRoot) {
            into(rootDir.path + "/build/libs-all")
        } else {
            into("build/libs-all")
        }
    }

    tasks.build {
        dependsOn("jarAll")
    }

    eclipse.pathVariables(mapOf("GRADLE_USER_HOME" to file(gradle.gradleUserHomeDir)))
    tasks.eclipse { dependsOn(tasks.cleanEclipse) }

    tasks.javadoc {
        exclude("**/internal/**")
        exclude("**/java-gen/**")
        exclude("**/app/**")

        //linking Javadoc in version prior 9 does not work well because Javadoc uses html frames.
        if (cfgJavaVersion.isJava9Compatible) {
            if (cfgJavaVersion.isJava11Compatible) {
                (options as StandardJavadocDocletOptions).links?.add("https://docs.oracle.com/en/java/javase/${cfgJavaVersion.majorVersion}/docs/api/")
            } else {
                (options as StandardJavadocDocletOptions).links?.add("https://docs.oracle.com/javase/${cfgJavaVersion.majorVersion}/docs/api/")
            }
        }
    }
}

configure(repositoryProjects) {
    tasks.publish {
        enabled = true
    }
}

configure(repositoryProjects) {

    publishing {
        publications {
            val mvnPublication: MavenPublication = maybeCreate<MavenPublication>("mavenJava")
            mvnPublication.from(components["java"])
            mvnPublication.versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            mvnPublication.pom {
                url.set("http://www.beanit.com/")

                developers {
                    developer {
                        id.set("beanit")
                        name.set("Beanit GmbH")
                    }
                }
                scm {
                    connection.set("none")
                    url.set("none")
                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = uri(cfgRepository ?: "")
                val snapshotsRepoUrl = uri(cfgSnapshotRepository ?: "")
//                val releasesRepoUrl = uri("$buildDir/repos/releases")
//                val snapshotsRepoUrl = uri("$buildDir/repos/snapshots")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username = cfgRepositoryUser
                    password = cfgRepositoryPass
                }
            }
        }
    }
    if (cfgSignPom) {
        signing {
            sign(publishing.publications["mavenJava"])
        }
    }
}



tasks.register<Javadoc>("javadocAll") {

    source(docProjects.map { project ->
        project.sourceSets["main"].allJava
    })

    exclude("**/internal/**")
    exclude("**/java-gen/**")
    exclude("**/app/**")

    setDestinationDir(File(buildDir, "docs/javadoc-all"))

    classpath = files(distributionProjects.map { project ->
        project.sourceSets["main"].compileClasspath
    })

    classpath += files(distributionProjects.map { project ->
        project.sourceSets["main"].output
    })

    //linking Javadoc in version prior 9 does not work well because Javadoc uses html frames.
    if (cfgJavaVersion.isJava9Compatible) {
        if (cfgJavaVersion.isJava11Compatible) {
            (options as StandardJavadocDocletOptions).links?.add("https://docs.oracle.com/en/java/javase/${cfgJavaVersion.majorVersion}/docs/api/")
        } else {
            (options as StandardJavadocDocletOptions).links?.add("https://docs.oracle.com/javase/${cfgJavaVersion.majorVersion}/docs/api/")
        }
    }

}

tasks.named<Tar>("tar") {
    archiveFileName.set(project.name + "-" + project.version + ".tgz")

    dependsOn(distributionProjects.map { "${it.path}:build" })
    distributionProjects.forEach {
        println("project: "
                + it.path)
    }
    dependsOn(tasks.named("javadocAll"))

    compression = Compression.GZIP

    destinationDirectory.set(File("build/distributions/"))
}
