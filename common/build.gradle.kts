plugins {
    id("com.possible-triangle.common")
}

common {
    //pinned so the build doesn't need to hit maven.neoforged.net to list versions
    neoformVersion = "1.21.1-20240808.144430"
    accessWidener()
}

val moonlight_version: String by extra

dependencies {
    modCompileOnly("net.mehvahdjukaar:moonlight-common:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-common:${moonlight_version}")

    modCompileOnly("curse.maven:jei-238222:5603591")
    modCompileOnly("curse.maven:emi-580555:5704405")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:16.0.777")
}
