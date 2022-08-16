import org.beryx.jlink.JPackageTask
import org.gradle.internal.os.OperatingSystem

plugins {
	kotlin("jvm") version "1.7.10"
	id("com.github.jk1.dependency-license-report") version "2.+"
	id("org.beryx.jlink") version "2.+"
	id("org.ajoberstar.reckon") version "0.+"
}

description = "Common demo skeleton"

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

	dependsOn(subprojects.map { it.tasks.withType<JPackageTask>() })
}

dependencies {
	api(libs.bundles.stdlib)
	api(libs.bundles.pancake)
	api(libs.bundles.log)

	val os = OperatingSystem.current()

	// lwjgl
	val lwjglPlatform = "natives-${if (os.isWindows) "windows" else if (os.isMacOsX) "macos" else "linux"}"
	libs.bundles.lwjgl.get()
		.map { it.module }
		.forEach {
			implementation("${it.group}:${it.name}::$lwjglPlatform")
		}

	// imgui
	implementation(
		if (os.isWindows) libs.imgui.windows
		else if (os.isMacOsX) libs.imgui.macos
		else libs.imgui.linux
	)
}

allprojects {
	apply(plugin = "kotlin")

	repositories {
		mavenCentral()

		listOf("flub", "pancake").forEach {
			maven {
				url = uri("https://maven.pkg.github.com/kkorolyov/$it")
				credentials {
					username = System.getenv("GITHUB_ACTOR")
					password = System.getenv("GITHUB_TOKEN")
				}
			}
		}
	}

	dependencyLocking {
		lockAllConfigurations()

		ignoredDependencies.add("io.github.spair:imgui-java-natives*")
	}

	tasks.compileKotlin {
		kotlinOptions {
			jvmTarget = tasks.compileJava.get().targetCompatibility
		}
		destinationDirectory.set(tasks.compileJava.get().destinationDirectory)
	}
}

subprojects {
	apply(plugin = "com.github.jk1.dependency-license-report")
	apply(plugin = "org.beryx.jlink")

	dependencies {
		implementation(rootProject)
	}

	licenseReport {
		excludeBoms = true
	}

	jlink {
		forceMerge("jackson", "log4j", "slf4j")

		options.addAll("--compress", "2", "--no-header-files", "--no-man-pages", "--strip-debug")

		jpackage {
			appVersion = (findProperty("jpackage.version") ?: version).toString()
			vendor = "kkorolyov"
			icon = "${rootProject.rootDir}/pancake.${if (OperatingSystem.current().isWindows) "ico" else "png"}"

			val options = mutableListOf("--license-file", "../LICENSE", "--icon", icon)
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
