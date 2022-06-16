import org.gradle.internal.os.OperatingSystem

plugins {
	kotlin("jvm") version "1.6.+"
	id("org.openjfx.javafxplugin") version "0.+"
	id("org.javamodularity.moduleplugin") version "1.+"
	id("com.github.jk1.dependency-license-report") version "2.+"
	id("org.beryx.jlink") version "2.+"
	id("org.ajoberstar.reckon") version "0.+"
}

description = "Common demo skeleton"

val osName = if (OperatingSystem.current().isWindows) "windows" else if (OperatingSystem.current().isMacOsX) "macos" else "linux"

tasks.wrapper {
	distributionType = Wrapper.DistributionType.ALL
}

reckon {
	stages("rc", "final")
	setScopeCalc(calcScopeFromProp())
	setStageCalc(calcStageFromProp())
}
tasks.reckonTagCreate {
	dependsOn(tasks.check)
}

tasks.startScripts {
	enabled = false
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

	val os = OperatingSystem.current()
	if (os.isWindows) api(libs.bundles.pancake.natives.windows)
	else if (os.isMacOsX) api(libs.bundles.pancake.natives.macos)
	else api(libs.bundles.pancake.natives.linux)
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

		ignoredDependencies.addAll("dev.kkorolyov.pancake:audio-al-*", "dev.kkorolyov.pancake:graphics-gl-*", "dev.kkorolyov.pancake:input-glfw-*")
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
	apply(plugin = "com.github.jk1.dependency-license-report")
	apply(plugin = "org.beryx.jlink")

	licenseReport {
		excludeBoms = true
	}

	jlink {
		forceMerge("jackson", "log4j", "slf4j")

		options.addAll("--compress", "2", "--no-header-files", "--no-man-pages", "--strip-debug")

		jpackage {
			appVersion = (findProperty("jpackage.version") ?: version).toString()

			icon = "../pancake.${if (OperatingSystem.current().isWindows) "ico" else "png"}"

			val options = mutableListOf("--license-file", "../LICENSE")
			if (OperatingSystem.current().isWindows) options += "--win-dir-chooser"

			installerOptions.addAll(options.toList())

			jvmArgs.addAll(listOf("-splash:\$APPDIR/splash.png"))
		}
	}

	tasks.jpackageImage {
		dependsOn(tasks.generateLicenseReport)

		doLast {
			copy {
				from(rootProject.rootDir)
				include("pancake.png")
				rename("pancake", "splash")
				into("$buildDir/jpackage/${project.name}/app")
			}
			copy {
				from("$buildDir/reports")
				into("$buildDir/jpackage/${project.name}/runtime/legal")
			}
		}
	}
}
