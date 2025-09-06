// build.gradle.kts (jpackage-based installer)
import java.io.IOException
import java.util.jar.JarFile

plugins {
    // No Java code to compile; helper tasks only
}

val versionIss = layout.projectDirectory.file("cryptad_version.iss")
// Source jpackage app image produced by the :cryptad build
val jpackageSrcDir = layout.projectDirectory.dir("cryptad/build/jpackage/Crypta")
// Staging dir used by Inno Setup script (matches CryptaInstaller_InnoSetup.iss)
val jpackageStageDir = layout.projectDirectory.dir("jpackage")

// Shared property to pass the computed version between tasks
val cryptadVersion = objects.property<String>()

tasks.register("stageJpackage") {
    description = "Stage jpackage app image from :cryptad into ./jpackage (or use pre-staged directory)"
    inputs.dir(jpackageSrcDir)
    outputs.dir(jpackageStageDir)
    doLast {
        val src = jpackageSrcDir.asFile
        val dst = jpackageStageDir.asFile
        when {
            src.exists() -> {
                println("Staging jpackage from ${src}")
                delete(dst)
                copy {
                    from(src)
                    into(dst)
                }
            }
            dst.exists() -> {
                println("Source not found; using already-staged jpackage at ${dst}")
            }
            else -> error("Neither ${src} nor ${dst} exists. Build :cryptad or download jpackage artifact.")
        }
    }
}

tasks.register("buildInfo") {
    dependsOn("stageJpackage")
    doLast {
        // Extract version from the staged jpackage app image manifest
        val jarFile = jpackageStageDir.file("app/bootstrap.jar").asFile
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
