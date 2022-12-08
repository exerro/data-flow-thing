<h1 align="center">
  Data Flow Thing
</h1>

<p align="center">
  <a href="https://jitpack.io/#exerro/data-flow-thing"><img src="https://jitpack.io/v/exerro/data-flow-thing.svg" alt="JitPack badge"/></a>
  <img src="https://github.com/exerro/data-flow-thing/actions/workflows/CI.yml/badge.svg" alt="Build passing status">
</p>

Experimental data flow concept letting you define "nodes" with "sockets" and
connect the sockets together in a "configuration". All sockets are statically
typed, and input sockets must always be connected to exactly one output socket.

Connections between sockets are implemented as hot streams with a configurable
buffer size and configurable overflow strategies.

## Example code

See [`main.kt`](https://github.com/exerro/data-flow-thing/blob/main/src/test/kotlin/me/exerro/dataflow/main.kt)
for an example test application. To run this, clone the repository and run
`./gradlew testApplication`.

## Use the library

#### With Gradle (`build.gradle.kts`)

```kotlin
repositories {
    // ...
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("me.exerro:data-flow-thing:1.0.0")
}
```

#### Download from [releases](https://github.com/exerro/data-flow-thing/releases)

#### For more, see [JitPack](https://jitpack.io/#exerro/data-flow-thing)

<!--
# Developer utilities

## Creating a release

* Update version in `build.gradle.kts`
* Run `./gradlew clean build test publishToMavenLocal`
* Update version in this README.
* Run `git tag <version>`
* Run `git push --tags`
-->
