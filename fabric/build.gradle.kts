plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version: String by extra
val codecui_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")
    modRuntimeOnly("net.mehvahdjukaar:codecui-fabric:${codecui_version}")

    modCompileOnly("curse.maven:jei-238222:5603591")
    modCompileOnly("curse.maven:emi-580555:5704405")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-fabric:16.0.777")

    modCompileOnly("curse.maven:yacl-667299:4574163")
    modCompileOnly("curse.maven:modmenu-308702:3920481") {
        exclude(module = "fabric-api")
    }
}
