// build.gradle.kts
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import java.io.File
import java.io.IOException

plugins {
	`java-library`
}

repositories {
	mavenLocal() // TODO: use lib/ instead?
	maven {
		url = uri("https://mvn.freenetproject.org")
	}
	mavenCentral() // jcenter() removed → use Maven Central or another repo
}

dependencies {
	api(project(":cryptad"))
}

val cryptadDir = layout.projectDirectory.dir("cryptad")
val versionIss = layout.projectDirectory.file("cryptad_version.iss")
val depsIss = layout.projectDirectory.file("cryptad_deps.iss")
val runtimeClasspath = configurations.getByName("runtimeClasspath")
val runtimeArtifacts = runtimeClasspath.incoming.artifacts.resolvedArtifacts

// Shared property to pass the computed version between tasks
val cryptadVersion = objects.property<String>()

tasks.register("buildInfo") {
	// declare inputs/outputs if you later want config-cache friendliness
	doLast {
		val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
		val gitcmd = if (isWindows) """C:\Program Files\Git\bin\git.exe""" else "git"
		val cmd = listOf(gitcmd, "describe", "--always", "--abbrev=4", "--dirty")
		try {
			val proc = ProcessBuilder(cmd)
				.directory(cryptadDir.asFile)   // <- no Task.project access
				.redirectErrorStream(true)
				.start()
			val output = proc.inputStream.bufferedReader().readText().trim()
			proc.waitFor()
			cryptadVersion.set(output.ifBlank { "@unknown@" })
			println("Cryptad version: ${cryptadVersion.get()}")
		} catch (_: IOException) {
			cryptadVersion.set("@unknown@")
		}
	}
}

tasks.register("unzip")

/**
 * Removes version and qualifier from a jar filename.
 * Examples:
 *   'jna-platform-5.17.0.jar'           → 'jna-platform.jar'
 *   'unbescape-1.1.6.RELEASE.jar'       → 'unbescape.jar'
 *   'foo-bar-2.0.0-RC1.jar'             → 'foo-bar.jar'
 *   'baz-3.4.5_SNAPSHOT.jar'            → 'baz.jar'
 */
fun stripVersionFromJar(fullPath: String): String {
	val fileName = File(fullPath).name
	val regex = Regex("""^(.*?)-\d+(?:\.\d+)*(?:[._-]?(?:RELEASE|SNAPSHOT|RC\d*|M\d*|GA|FINAL|[A-Za-z]+))*\.jar$""")
	val match = regex.matchEntire(fileName)
	return if (match != null) "${match.groupValues[1]}.jar" else fileName
}

tasks.register("updateSetupFile") {
	dependsOn("buildInfo")
	inputs.property("cryptadVersion", cryptadVersion)
	inputs.files(runtimeClasspath)
	outputs.files(versionIss, depsIss)

	doLast {
		val version = cryptadVersion.orNull ?: "@unknown@"

		val versionFile = versionIss.asFile
		versionFile.delete()
		versionFile.writeText("#define AppVersion \"0.7.5 build $version\"\r\n")

		val depsFile = depsIss.asFile
		depsFile.delete()

		val artifacts = runtimeArtifacts.get()
		artifacts.forEach { artifact ->
			val moduleName = when (val compId = artifact.id.componentIdentifier) {
				is ModuleComponentIdentifier -> compId.module
				is ProjectComponentIdentifier -> compId.projectName
				else -> compId.displayName
			}
			println("Processing dependency: $moduleName")
			when (moduleName) {
				"cryptad" -> depsFile.appendText(
					"Source: \"artifacts\\cryptad.jar\"; DestDir: \"{app}\"; Flags: ignoreversion; AfterInstall: CryptadJarDoAfterInstall\r\n"
				)
				"freenet-ext" -> depsFile.appendText(
					"Source: \"${artifact.file}\"; DestDir: \"{app}\"; DestName: \"freenet-ext.jar\"; Flags: ignoreversion\r\n"
				)
				else -> {
					val destName = stripVersionFromJar(artifact.file.toString())
					depsFile.appendText(
						"Source: \"${artifact.file}\"; DestDir: \"{app}\\libs\"; DestName: \"$destName\"; Flags: ignoreversion;\r\n"
					)
				}
			}
		}
	}
}