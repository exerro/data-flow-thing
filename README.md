<h1 align="center">
  Data Flow Thing
</h1>

<p align="center">
  <a href="https://jitpack.io/#exerro/dataflow"><img src="https://jitpack.io/v/exerro/dataflow.svg" alt="JitPack badge"/></a>
  <img src="https://github.com/exerro/data-flow-thing/actions/workflows/CI.yml/badge.svg" alt="Build passing status">
</p>

TODO

## Use the library

#### With Gradle (`build.gradle.kts`)

```kotlin
repositories {
    // ...
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("me.exerro:dataflow:1.0.0")
}
```

#### Download from [releases](https://github.com/exerro/dataflow/releases)

#### For more, see [JitPack](https://jitpack.io/#exerro/dataflow)

<div style="display: none">

# Developer utilities

## Creating a release

* Update version in `build.gradle.kts`
* Run `./gradlew clean build test publishToMavenLocal`
* Update version in this README.
* Run `git tag <version>`
* Run `git push --tags`

</div>
