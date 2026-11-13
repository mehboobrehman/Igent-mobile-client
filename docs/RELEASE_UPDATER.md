# Release Process for Igent Mobile Client Updates

To release a new version of the Igent Android application and enable the in-app updater:

1.  **Prepare Version**: Update `version.properties` with the new version (MAJOR, MINOR, PATCH, BUILD).

2.  **Build Release APK**: Run `./gradlew assembleRelease` to generate the release APK.
    The output APK file will be located in `app/build/outputs/apk/release/` and named `Igent-release-v<version>.apk`.

3.  **Generate Checksum**: Generate the SHA-256 checksum for the release APK:
    ```bash
    sha256sum app/build/outputs/apk/release/Igent-release-v<version>.apk > SHA256SUMS
    ```

4.  **Create GitHub Release**:
    - Tag the release (e.g., `v<version>`).
    - Attach the generated `Igent-release-v<version>.apk` to the release.
    - Attach the `SHA256SUMS` file to the release.
    - Publish the release.

The Android in-app updater will automatically detect the new release using the GitHub `latest` releases API.
