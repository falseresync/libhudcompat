# LibHudCompat

## Maven

See the latest version on GitHub Releases

```groovy
repositories {
    maven { 
        url "https://jitpack.io"
        content { 
            includeGroup "com.github.falseresync" 
        }
    }
}

dependencies {
    modImplementation include("com.github.falseresync:libhudcompat:[VERSION]")
}
```