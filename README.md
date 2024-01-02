# LibHudCompat

## Maven
```groovy
repositories {
    maven {
        url "https://repo.repsy.io/mvn/falseresync/default"
        content {
            includeGroup "dev.falseresync"
        }
    }
}

dependencies {
    modImplementation include("dev.falseresync:libhudcompat:[VERSION]")
}
```