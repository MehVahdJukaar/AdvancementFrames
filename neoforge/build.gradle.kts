plugins {
    id("com.possible-triangle.neoforge")
}

neoforge {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    modCompileOnly("curse.maven:jei-238222:5603591")
    modCompileOnly("curse.maven:emi-580555:5704405")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:16.0.777")

    modCompileOnly("curse.maven:repurposed-structures-368293:4823487")
    modCompileOnly("curse.maven:framedblocks-441647:5143589")
    modCompileOnly("curse.maven:create-328085:6247669")
}
