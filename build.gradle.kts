import edu.udo.cs.sopra.util.addFileToDistribution
import edu.udo.cs.sopra.util.ignoreClassesInCoverageReport
import edu.udo.cs.sopra.util.sonatypeSnapshots
import edu.udo.cs.sopra.util.sopraPackageRegistry
import org.gradle.kotlin.dsl.application

plugins {
    kotlin("jvm") version "1.9.25"
    application
    id("edu.udo.cs.sopra") version "1.0.3"
}

group = "edu.udo.cs.sopra"
version = "1.0"

/* Change this to the version of the BGW you want to use */
val bgwVersion = "0.10"

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}

repositories {
    mavenCentral()
    sonatypeSnapshots()
    sopraPackageRegistry()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    implementation(group = "tools.aqua", name = "bgw-gui", version = bgwVersion)
    implementation(group = "tools.aqua", name = "bgw-net-common", version = bgwVersion)
    implementation(group = "tools.aqua", name = "bgw-net-client", version = bgwVersion)
    implementation(group = "edu.udo.cs.sopra", name = "ntf", version = "26A.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

/* This is how you can add the HowToPlay.pdf to the distribution zip file */
addFileToDistribution(file("HowToPlay.pdf"))

/* This is how you can ignore additional classes from test coverage */
/* All classes in gui, entity and service.bot package are already excluded. */

/* To ignore a class Foo in the package foo.bar.baz you would use the following line */
// this.ignoreClassesInCoverageReport("foo.bar.baz.Foo")

/* To ignore all classes in the foo.bar.baz package use a wildcard like this */
// this.ignoreClassesInCoverageReport("foo.bar.baz.*")

tasks.distZip {
    archiveFileName.set("distribution.zip")
    into("") {
        from("HowToPlay.pdf")
        include("")
    }
    destinationDirectory.set(layout.projectDirectory.dir("public"))
}

tasks.clean {
    delete.add("public")
}
