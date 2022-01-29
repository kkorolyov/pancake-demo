import org.apache.tools.ant.taskdefs.condition.Os

plugins {
	kotlin("jvm") version "1.+"
	id("org.openjfx.javafxplugin") version "0.+"
	id("org.javamodularity.moduleplugin") version "1.+"
	id("org.beryx.jlink") version "2.+"
	id("org.ajoberstar.reckon") version "0.+"
}

description = "Common demo skeleton"

tasks.wrapper {
	distributionType = Wrapper.DistributionType.ALL
}

reckon {
	scopeFromProp()
	snapshotFromProp()
}
tasks.reckonTagCreate {
	dependsOn(tasks.check)
}

tasks.register("allDeps") {
	group = "help"
	description = "List dependencies of all projects"

	dependsOn(allprojects.map { it.tasks.withType<DependencyReportTask>() })
}
tasks.register("allJpackage") {
	group = "build"
	description = "Packages all subprojects"

	dependsOn(subprojects.map { it.tasks.withType<org.beryx.jlink.JPackageTask>() })
}

dependencies {
	api(libs.bundles.stdlib)
	api(libs.bundles.pancake)
	api(libs.bundles.log)
}

allprojects {
	apply(plugin = "kotlin")
	apply(plugin = "org.openjfx.javafxplugin")
	apply(plugin = "org.javamodularity.moduleplugin")

	repositories {
		mavenCentral()
		maven {
			url = uri("https://maven.pkg.github.com/kkorolyov/flub")
			mavenContent {
				releasesOnly()
			}
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}
		maven {
			url = uri("https://oss.sonatype.org/content/repositories/snapshots")
			mavenContent {
				includeGroup("no.tornado")
			}
		}
		maven {
			url = uri("https://maven.pkg.github.com/kkorolyov/pancake")
			mavenContent {
				releasesOnly()
			}
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}
	}

	dependencyLocking {
		lockAllConfigurations()
	}

	tasks.compileKotlin {
		kotlinOptions {
			jvmTarget = tasks.compileJava.get().targetCompatibility
		}
	}
	javafx {
		version = tasks.compileJava.get().targetCompatibility
		modules("javafx.fxml", "javafx.web", "javafx.swing")
	}
}

subprojects {
	apply(plugin = "org.beryx.jlink")

	jlink {
		forceMerge("jackson", "log4j", "slf4j")

		jpackage {
			appVersion = (findProperty("jpackage.version") ?: version).toString()

			icon = if (Os.isFamily(Os.FAMILY_WINDOWS)) "../pancake.ico" else "../pancake.png"

			val options = mutableListOf("--license-file", "../LICENSE")
			if (Os.isFamily(Os.FAMILY_WINDOWS)) options += "--win-dir-chooser"

			installerOptions = options.toList()
		}
	}
}
