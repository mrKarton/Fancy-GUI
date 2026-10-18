# Fancy GUI
A simple modern GUI library for minecraft fabric
## Uasage

Edit your build.gradle like this to add needed repositories:
```
repositories {
    // Fancy GUI is hosted on JitPack
    maven { url 'https://jitpack.io' }

    // Needed to FancyGUI dependencies
    maven {
        url = uri("https://maven.nucleoid.xyz")
    }
}

dependencies {
    implementation 'com.github.mrKarton:Fancy-GUI:{version}'
}
```
