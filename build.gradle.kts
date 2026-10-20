import java.util.*

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("me.modmuss50.mod-publish-plugin")
    id("com.gradleup.shadow")
}

val minecraft = stonecutter.current.version
val loader = loom.platform.get().name.lowercase()

sc.constants.match(sc.current.project.substringAfter('-'), "fabric", "neoforge", "forge")

version = "${mod.version}+$minecraft"
group = mod.group
base {
    archivesName.set("${mod.id}-$loader")
}

architectury.common(sc.current.project.substringAfter('-'))

repositories {
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.architectury.dev/")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.nucleoid.xyz/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())

    if (loader == "fabric") {
        modImplementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
        modImplementation("com.terraformersmc:modmenu:${mod.dep("modmenu_version")}")
        modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_version")}")
        modImplementation("dev.architectury:architectury-fabric:${mod.dep("architectury_version")}")
    }
    if (loader == "forge") {
        "forge"("net.minecraftforge:forge:${minecraft}-${mod.dep("forge_loader")}")

        configurations.configureEach {
            exclude(group = "net.fabricmc", module = "fabric-log4j-util")
        }

        val mixinExtras = "io.github.llamalad7:mixinextras-common:${mod.dep("mixin_extras")}"
        compileOnly(mixinExtras)
        annotationProcessor(mixinExtras)
        implementation("io.github.llamalad7:mixinextras-forge:${mod.dep("mixin_extras")}")

        modImplementation("dev.architectury:architectury-forge:${mod.dep("architectury_version")}")
    }
    if (loader == "neoforge") {
        "neoForge"("net.neoforged:neoforge:${mod.dep("neoforge_loader")}")
        modImplementation("dev.architectury:architectury-neoforge:${mod.dep("architectury_version")}")
    }
}

loom {
    decompilers {
        get("vineflower").apply {
            options.put("mark-corresponding-synthetics", "1")
        }
    }
    if (loader == "forge") {
        forge.mixinConfigs(
            "persistent_options-common.mixins.json",
            "persistent_options-forge.mixins.json",
        )
    }
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
publishMods {
    val modrinthToken = localProperties.getProperty("publish.modrinthToken", "")
    val curseforgeToken = localProperties.getProperty("publish.curseforgeToken", "")

    file = project.tasks.remapJar.get().archiveFile
    dryRun = modrinthToken.isBlank() || curseforgeToken.isBlank()

    displayName = "${mod.name} ${loader.replaceFirstChar { it.uppercase() }} ${property("mod.mc_title")}-${mod.version}"
    version = mod.version
    type = BETA

    modLoaders.add(loader)

    val targets = property("mod.mc_targets").toString().split(' ')
    modrinth {
        projectId = property("publish.modrinth").toString()
        accessToken = modrinthToken
        targets.forEach(minecraftVersions::add)
        if (loader == "fabric") {
            requires("fabric-api")
            optional("modmenu")
        }
    }

    curseforge {
        projectId = property("publish.curseforge").toString()
        accessToken = curseforgeToken
        targets.forEach(minecraftVersions::add)
        if (loader == "fabric") {
            requires("fabric-api")
            optional("modmenu")
        }
    }
}

java {
    withSourcesJar()
    val javaVersion = if (stonecutter.eval(minecraft, ">=1.20.5")) 21 else 17

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }

    targetCompatibility = JavaVersion.toVersion(javaVersion)
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.withType<JavaExec>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor(java.toolchain)
    )
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
}

tasks.remapJar {
    injectAccessWidener = false
    input = tasks.shadowJar.get().archiveFile
    archiveClassifier = null
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    archiveClassifier = "dev"
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "versioned"
        dependsOn(buildAndCollect)
    }

    rootProject.tasks.register("runActive") {
        group = "versioned"
        dependsOn(tasks.named("runClient"))
    }
}

tasks.processResources {
    properties(
        listOf("fabric.mod.json"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_dep_fabric")
    )
    properties(
        listOf("META-INF/mods.toml", "pack.mcmeta"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_dep_forgelike")
    )
    properties(
        listOf("META-INF/neoforge.mods.toml", "pack.mcmeta"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_dep_forgelike")
    )
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}
