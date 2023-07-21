buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    java
    checkstyle
}

project.extra["GithubUrl"] = "https://github.com/0Hutch/PiggyPlugins"

apply<BootstrapPlugin>()
apply<VersionPlugin>()

allprojects {
    group = "com.plugins"
    apply<MavenPublishPlugin>()
    version = ProjectVersions.runeliteVersion
}

tasks.named("bootstrapPlugins") {
    finalizedBy("copyBootstrap")
}

tasks.register<Copy>("copyBootstrap") {
    println("Copying bootstrap to main dir.")
    from("./build/bootstrap/")
    into(System.getProperty("user.home") + "/Documents/RuneLitePlugins/Jars")
    eachFile {
        if (this.relativePath.getFile(destinationDir).exists() && this.sourceName != "plugins.json") {
            this.exclude()
            println("Excluding " + this.sourceName + " as its the same version.")
        }
    }
}

subprojects {
    group = "com.plugins"

    project.extra["PluginProvider"] = "Piggy Plugins"
    project.extra["ProjectSupportUrl"] = "https://discord.gg/whz59WErKg"
    project.extra["PluginLicense"] = "2-Clause BSD License"

    repositories {
        maven {
            url = uri("https://repo.runelite.net")
        }
        mavenLocal()
        mavenCentral()

        jcenter {
            content {
                excludeGroupByRegex("com\\.openosrs.*")
                excludeGroupByRegex("com\\.runelite.*")
            }
        }

        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://repo.runelite.net")
                }
            }
            filter {
                includeModule("net.runelite", "discord")
            }
        }
    }

    apply<JavaPlugin>()

    val version = "latest.release"

    dependencies {
        annotationProcessor(Libraries.lombok)
        annotationProcessor(Libraries.pf4j)

        compileOnly(group = "net.runelite", name = "client", version = version)
        compileOnly(group = "net.runelite", name = "runelite-api", version = version)
        compileOnly(group = "net.runelite", name = "jshell", version = version)

        compileOnly(Libraries.findbugs)
        compileOnly(Libraries.apacheCommonsText)
        compileOnly(Libraries.gson)
        compileOnly(Libraries.guice)
        compileOnly(Libraries.lombok)
        compileOnly(Libraries.okhttp3)
        compileOnly(Libraries.pf4j)
        compileOnly(Libraries.rxjava)

        testImplementation("junit:junit:4.12")
        testImplementation(group = "net.runelite", name = "client", version = version)
        testImplementation(group = "net.runelite", name = "jshell", version = version)

    }

    configure<PublishingExtension> {
        repositories {
            maven {
                url = uri("$buildDir/repo")
            }
        }
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
            }
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        withType<Jar> {
            doLast {
                copy {
                    from("./build/libs/")
                    into("../release/")
                }
            }
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }

        register<Copy>("copyDeps") {
            into("./build/deps/")
            from(configurations["runtimeClasspath"])
        }
    }
}