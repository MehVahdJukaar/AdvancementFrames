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
}
