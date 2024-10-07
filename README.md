This is the codebase for the 2025 STORM Robotics competition.

The server is the robot, and is connected to via the client, which is controlled by the operator.
This client will most likely be running on their laptop, but technically the client can also run on
an Android device. This is mostly for convenience of testing during development.

### Project Structure

This is a Kotlin Multiplatform project targeting Desktop, Android, and Server.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - `commonMain` is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the
      folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      `iosMain` would be the right folder for such calls.

* `/server` is for the Ktor server application.

* `/shared` is for the code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. If preferred, you can add code to the
  platform-specific folders here too.

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…