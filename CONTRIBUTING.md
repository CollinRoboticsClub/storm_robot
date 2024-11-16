### Project Overview

This is a Kotlin Multiplatform project containing code for our **client** and **server**.

The **server** will run on the robot itself and will handle interpreting data sent from the **client** and
making the robot move accordingly.

The **client** is just a graphical program to allow the operator (aka driver, aka person who happens to be
controlling the robot at a given point in time) to communicate with the robot. For this robot, that'll consist
of stuff like "receiving a video stream from the robot" and "telling the robot's wheels to move", etc. But this
could definitely change based on our future design choices.

### Project Structure

The fact that this project is "multiplatform" affects the code structure slightly.
So to help you interpret the purpose of certain directories, (as of writing this)
platform support is as follows:

|             | Client | Server |
|-------------|--------|--------|
| Desktop/JVM | Yes    | Yes    |
| Android     | Yes    | No     |
| Web         | WIP    | No     |

* `/composeApp` is for code that will be shared across all clients.
  It contains several subfolders:
    - `commonMain` is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the
      folder name. This is why we also have `desktopMain`, `androidMain`, and `webMain` folders.

* `/server` is for the server application (Specifically, it's a Ktor application).

* `/shared` is for the code that will be shared between the client *and* the server.
  The most important subfolder is `commonMain`. If preferred, you can add code to the
  platform-specific folders here too.
    * Currently, this just contains simple stuff like constants and data classes

Learn more about:

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Ktor](https://ktor.io/docs/welcome.html)
- [Compose](https://developer.android.com/develop/ui/compose/documentation) (The Android docs are almost perfectly
  applicable even when we're writing multiplatform code)