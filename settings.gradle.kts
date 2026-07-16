pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LegalTranscriberAndroid"

include(":app")

includeBuild("../kmp_app") {
    dependencySubstitution {
        substitute(module("com.legal.transcriber:shared")).using(project(":shared"))
    }
}
