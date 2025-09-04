// build.gradle.kts (jlink-based installer)
import java.io.IOException
import java.util.jar.JarFile

plugins {
    // No Java code to compile; helper tasks only
}

val versionIss = layout.projectDirectory.file("cryptad_version.iss")
val jlinkTarDir = layout.projectDirectory.dir("artifacts")
val jlinkOutDir = layout.projectDirectory.dir("artifacts/cryptad-jlink-dist")

// Shared property to pass the computed version between tasks
val cryptadVersion = objects.property<String>()

tasks.register("unpackJlink") {
    inputs.files(fileTree(jlinkTarDir) { include("cryptad-jlink-*.tar.gz") })
    outputs.dir(jlinkOutDir)
    doLast {
        val fromArtifacts = fileTree(jlinkTarDir) { include("cryptad-jlink-*.tar.gz") }.files
        val fromRoot = fileTree(layout.projectDirectory) { include("cryptad-jlink-*.tar.gz") }.files
        val tars = (fromArtifacts + fromRoot).toSet()
        if (tars.isEmpty()) error("No cryptad-jlink-*.tar.gz found in ${jlinkTarDir.asFile} or project root")
        val newest = tars.maxByOrNull { it.lastModified() }!!
        println("Unpacking jlink: ${newest.name}")
        delete(jlinkOutDir)
        copy {
            from(tarTree(resources.gzip(newest)))
            into(jlinkOutDir)
        }
    }
}

tasks.register("buildInfo") {
    dependsOn("unpackJlink")
    doLast {
        val jarFile = jlinkOutDir.file("lib/cryptad.jar").asFile
        val impl = try {
            if (jarFile.exists()) {
                JarFile(jarFile).use { jf ->
                    jf.manifest?.mainAttributes?.getValue("Implementation-Version")
                }
            } else null
        } catch (_: IOException) { null }

        // Expect two parts: version and git_rev. Examples: "1 03df3c9137" or "1;03df3c9137"
        val (verNum, gitRev) = run {
            val raw = (impl ?: "").trim()
            if (raw.isEmpty()) return@run ("@unknown@" to "unknown")
            val parts = raw.replace(';', ' ').trim().split(Regex("\\s+"), limit = 2)
            val v = parts.getOrNull(0)?.trim().orEmpty()
            val g = parts.getOrNull(1)?.trim().orEmpty()
            (v.ifBlank { "@unknown@" } to g.ifBlank { "unknown" })
        }
        val combined = "v${'$'}verNum+${'$'}gitRev"
        cryptadVersion.set(combined)
        println("Cryptad version (from manifest): ${'$'}combined (raw='${'$'}impl')")
    }
}

tasks.register("updateSetupFile") {
    dependsOn("buildInfo")
    inputs.property("cryptadVersion", cryptadVersion)
    outputs.file(versionIss)
    doLast {
        val version = cryptadVersion.orNull ?: "v0+unknown"
        val versionFile = versionIss.asFile
        versionFile.writeText("#define AppVersion \"${'$'}version\"\r\n")
        println("Wrote ${versionFile}")
    }
}
