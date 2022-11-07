import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    application
    id("org.graalvm.buildtools.native") version "0.9.17"
}

graalvmNative {
    toolchainDetection.set(false)
}

group = "com.trillica"
version = "1.0-SNAPSHOT"



repositories {
    mavenCentral()
    gradlePluginPortal()
}



//graalvmNative {
//    binaries {
//        //main {
//        javaLauncher = javaToolchains.launcherFor {
//            languageVersion = JavaLanguageVersion.of(17)
////                vendor = JvmVendorSpec.matching("GraalVM Community")
//        }
//        //}
//    }
//}

dependencies {
    testImplementation(kotlin("test"))

    implementation("com.sksamuel.hoplite:hoplite-core:2.6.5")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.6.5")

    implementation("com.github.ajalt.clikt:clikt:3.5.0")

    implementation(group="org.slf4j", name="slf4j-api", version="1.7.+")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.0.2")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")

    applicationDefaultJvmArgs = if (System.getProperty("native.image.agent") != null) {
        listOf("-agentlib:native-image-agent=config-output-dir=META-INF/native-image/generated")
    } else emptyList()
}

tasks.getByName("run") {
    outputs.upToDateWhen { false }
}



nativeBuild {
    imageName.set("ksnap")
    mainClass.set("MainKt")

    buildArgs.addAll(
        "--no-fallback",
        "--enable-all-security-services",
        "--report-unsupported-elements-at-runtime",
        "--install-exit-handlers",
        """--initialize-at-build-time=
        |kotlin""".trimMargin().replace(System.lineSeparator(), ""),
        "-J--add-exports=java.management/sun.management=ALL-UNNAMED",
        "-H:+ReportUnsupportedElementsAtRuntime",
        "-H:+ReportExceptionStackTraces",
        "-H:ReflectionConfigurationFiles=${projectDir}/META-INF/native-image/generated/reflect-config.json",
        """-H:ResourceConfigurationFiles=
        |${projectDir}/META-INF/native-image/kotlin-resource.json,
        |${projectDir}/META-INF/native-image/generated/resource-config.json""".trimMargin().replace(System.lineSeparator(), ""),
    )
}