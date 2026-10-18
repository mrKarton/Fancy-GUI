# Fancy GUI
A simple modern GUI library for minecraft fabric
## Uasage

Add Github maven repository to your build.grafle:
```
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/mrkarton/Fancy-GUI")

        content {
            includeGroup("com.karton.fancygui")
        }

        credentials {
            username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                    .getOrNull()

            password = providers.gradleProperty("gpr.key")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                    .getOrNull()
        }
    }

    // Needed to FancyGUI dependencies
    maven {
        url = uri("https://maven.nucleoid.xyz")
    }
}

dependencies {
    implementation "com.karton.fancygui:fancy-gui:0.3.2"
}
```
