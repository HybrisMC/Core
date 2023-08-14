/*
 * HybrisMC, a Minecraft toolchain and client
 * Copyright (C) 2023, The HybrisMC Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import dev.hybrismc.meta.*
import kotlinx.coroutines.runBlocking
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.flow.*
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.*
import java.io.File
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlin.io.path.*
import kotlin.math.max

fun Project.toolchain(block: ToolchainPluginExtension.() -> Unit) = configure(block)
fun RepositoryHandler.spongeMaven() = maven("https://repo.spongepowered.org/repository/maven-public/")

private val allModules = mutableListOf<MutableProjectState>()
fun allMappings() = allModules.associate { it.mcVersionID to it.mappings }
fun gameJarOf(versionID: String): Path =
    getMinecraftDir().resolve("versions").resolve(versionID).resolve("$versionID.jar")

abstract class OnCompletion : FlowAction<OnCompletion.Parameters> {
    interface Parameters : FlowParameters {
        @get:Input
        val result: Property<BuildWorkResult>

        @get:Input
        val state: Property<MutableProjectState>
    }

    override fun execute(parameters: Parameters) {
        allModules -= parameters.state.get()
    }
}

abstract class ToolchainPlugin : Plugin<Project> {
    @get:Inject
    protected abstract val flowScope: FlowScope

    @get:Inject
    protected abstract val flowProviders: FlowProviders

    override fun apply(target: Project) {
        if (target.path == ":core") throw GradleException("Can not apply toolchain to core project!")

        val state = MutableProjectState(target).also {
            allModules += it
            it.setupProject()
        }

        // Thanks gradle, very cool
        flowScope.always(OnCompletion::class) {
            parameters.result.set(flowProviders.buildWorkResult)
            parameters.state.set(state)
        }
    }
}

class MutableProjectState(private val target: Project) {
    private val coreProject = target.project(":core")
    val http = createDefaultHTTP()
    val cache: Path by lazy {
        target.projectDir.toPath().resolve(".gradle").resolve("toolchain-cache").also { it.createDirectories() }
    }

    val extension = target.extensions.create<ToolchainPluginExtension>("toolchain")
    val mappingsConfig: Configuration = target.configurations.create("mappings") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    val mcVersion get() = extension.minecraftVersion.orNull ?: throw GradleException("Specify a Minecraft version!")
    val mcVersionID get() = mcVersion.id
    val gameJar: Path get() = gameJarOf(mcVersionID)

    val mappingFileCacheName by lazy {
        val mappingDep = retrieveMappingDep()
        if (mappingDep is FileCollectionDependency) {
            val file = mappingDep.files.singleOrNull() ?: error("More than one file in mappings collection!")
            val sha = MessageDigest.getInstance("SHA-1").digest(file.absolutePath.encodeToByteArray())
            "mapped-file-hashed-${sha.joinToString("") { "%02x".format(it) }}.jar"
        } else {
            val classifier = (mappingDep as? ModuleDependency)?.artifacts?.firstOrNull()?.classifier
                ?.let { "-$it" } ?: ""

            "mapped-${mappingDep.group}-${mappingDep.name}-${mappingDep.version}$classifier.jar"
        }
    }

    val mapOutput: Path get() = cache.resolve(mappingFileCacheName)
    val versionMeta by lazy {
        runBlocking {
            http.fetchVersion(mcVersionID)
                ?: throw GradleException("Version was not found in version manifest, cannot launch!")
        }
    }

    fun setupProject() {
        target.repositories.spongeMaven()
        target.dependencies.add("implementation", coreProject)

        target.afterEvaluate {
            require(mappings.classes.isNotEmpty())
            configurations.getByName("implementation").extendsFrom(mappingsConfig)

            if (!gameJar.exists()) {
                println("Downloading game JAR for deobfuscation environment")
                runBlocking { http.download(versionMeta.downloads.client.url, gameJar) }
            }

            if (!mapOutput.exists()) remapGame()
            dependencies.add("compileOnly", files(mapOutput))

            tasks.register("remapGame") {
                outputs.upToDateWhen { false }
                doLast { remapGame() }
            }

            val runConfig = rootDir.toPath().resolve(".run")
                .also { it.createDirectories() }.resolve("Run $mcVersionID.run.xml")

            if (!runConfig.exists()) {
                runConfig.writeText(
                    """
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Run $mcVersionID" type="GradleRunConfiguration" factoryName="Gradle">
    <ExternalSystemSettings>
      <option name="executionName" />
      <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}/${projectDir.relativeTo(rootDir).path}" />
      <option name="externalSystemIdString" value="GRADLE" />
      <option name="scriptParameters" value="--stacktrace" />
      <option name="taskDescriptions">
        <list />
      </option>
      <option name="taskNames">
        <list>
          <option value="launchGame" />
        </list>
      </option>
    </ExternalSystemSettings>
    <method v="2" />
  </configuration>
</component>
                    """.trimIndent()
                )
            }
        }

        target.tasks.register<JavaExec>("launchGame") {
            val toolchains = target.extensions.getByType<JavaToolchainService>()
            val java = target.extensions.getByType<JavaPluginExtension>()

            // Gradle...
            javaLauncher.set(toolchains.launcherFor {
                languageVersion.set(
                    JavaLanguageVersion.of(
                        max(
                            versionMeta.javaVersion.majorVersion,
                            java.toolchain.languageVersion.get().asInt()
                        )
                    )
                )

                vendor.set(java.toolchain.vendor)
                implementation.set(java.toolchain.implementation)
            })

            dependsOn(target.tasks.named("build"))

            workingDir(cache.resolve("game").also { it.createDirectories() })
            mainClass.set("dev.hybrismc.core.MainKt")
            args(mcVersionID, "--version", mcVersionID)

            doFirst {
                // Make sure to download natives if required
                val librariesDir = getMinecraftDir().resolve("libraries")

                runBlocking {
                    val nativesDownloads = versionMeta.relevantLibraries
                        .mapNotNull { it.downloads.natives }
                        .map { it.asDownload(librariesDir) }

                    http.downloadAll(
                        nativesDownloads
                            .filterNot { (_, d) -> d.exists() }
                            .also { if (it.isNotEmpty()) println("Downloading ${it.size} missing natives") }
                    )

                    val nativesDir = cache.resolve("natives").also {
                        it.deleteRecursive()
                        it.createDirectories()
                    }

                    nativesDownloads.forEach { (_, zip) ->
                        runCatching { unzip(zip, nativesDir) }.onFailure {
                            println("Failed unzipping $zip, ignoring")
                            it.printStackTrace()
                        }
                    }

                    classpath(*buildClasspath().toTypedArray())
                    jvmArgs(
                        "-Dterminal.ansi=true",
                        "-Djava.library.path=${nativesDir.absolutePathString()}",
                    )
                }
            }
        }
    }

    private fun buildClasspath(): List<File> {
        val thisJar = target.tasks.named("jar").get().outputs.files.files
        val deps = target.configurations.getByName("runtimeClasspath").files
        return (deps + thisJar).toList()
    }

    private fun Path.deleteRecursive() {
        if (isDirectory()) listDirectoryEntries().forEach { it.deleteRecursive() }
        deleteIfExists()
    }

    private fun unzip(file: Path, to: Path) {
        ZipFile(file.toFile()).use { zip ->
            zip.entries().asSequence().filterNot { it.isDirectory }.forEach { entry ->
                entry.name.split("/")
                    .fold(to) { acc, curr -> acc.resolve(curr) }
                    .also { it.parent.createDirectories() }
                    .writeBytes(zip.getInputStream(entry).readBytes())
            }
        }
    }

    private fun retrieveMappingDep() = mappingsConfig.dependencies.singleOrNull() ?: throw GradleException(
        "Exactly one mapping dependency must be present. " +
                "Found ${mappingsConfig.dependencies.size}."
    )

    private fun Configuration.asSingleFile(dependency: Dependency) = files(dependency).singleOrNull()
        ?: error("Configuration '$name' can only have a single dependency!")

    private fun retrieveMappingContainer() = mappingsConfig.asSingleFile(retrieveMappingDep())

    val mappings by lazy { getMappings(retrieveMappingContainer()) }
    val requiredNamespaces = setOf("official", "named", "intermediary")

    fun remapGame() {
        val missingNamespaces = requiredNamespaces.filter { it !in mappings.namespaces }
        if (missingNamespaces.isNotEmpty()) throw GradleException("Missing namespaces $missingNamespaces")

        remapJar(mappings, gameJar.toFile(), mapOutput.toFile())
    }

    // TODO
    private fun getMappings(container: File): Mappings {
        val mappingsFile = if (container.extension == "jar") target.zipTree(container)
            .matching { include("mappings/mappings.tiny") }.singleOrNull()
            ?: throw GradleException(
                "No Tiny mappings were found in mapping dependency. If you meant MCP, a file should be used"
            )

        else container

        return loadMappings(mappingsFile.readLines())
    }
}

interface ToolchainPluginExtension {
    val minecraftVersion: Property<MinecraftVersion>
}