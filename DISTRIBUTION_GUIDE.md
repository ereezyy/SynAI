# SynapseAI Distribution Guide

This guide summarizes the changes made to make the project ready for distribution and provides additional steps needed to complete the distribution process.

## Changes Made

1. **App Signing Configuration**
   - Updated `app/build.gradle` to load signing configuration from `keystore.properties`
   - Updated `wear/build.gradle` to load signing configuration from `keystore.properties`
   - Created a template `keystore.properties` file with placeholder values

2. **Build Optimization**
   - Enabled `minifyEnabled` and `shrinkResources` for the wear module's release build
   - Updated ProGuard rules for the wear module to ensure proper functionality with code shrinking

3. **Version Information**
   - Updated version name to follow semantic versioning (1.0.0) in both app and wear modules

## Additional Steps for Distribution

1. **Create a Keystore for App Signing**
   - Run the following command to create a keystore:
     ```
     keytool -genkey -v -keystore synapseai.keystore -alias synapseai -keyalg RSA -keysize 2048 -validity 10000
     ```
   - Follow the prompts to enter password, name, organization, etc.
   - Store the keystore file in a secure location

2. **Configure Keystore Properties**
   - Create a `keystore.properties` file based on the template
   - Update with actual keystore path and passwords
   - Ensure the file is not committed to version control

3. **Run the Build Script**
   - Execute `build_release.bat` to create signed APK and AAB files
   - The script will:
     - Verify keystore configuration
     - Clean the project
     - Run tests
     - Build signed AAB and APK
     - Create the release package

4. **Verify the Release**
   - Use the verification checklist in `verification_checklist.md`
   - Test the signed APK on real devices
   - Verify all features work correctly

5. **Deploy to Google Play Store**
   - Log in to Google Play Console
   - Create a new release
   - Upload the AAB file from `release_package/app/app-release.aab`
   - Complete the release information
   - Submit for review

## Security Considerations

1. **Keystore Security**
   - Store keystore files securely
   - Do not share keystore passwords via unsecured channels
   - Never commit keystore files or passwords to version control

2. **Release Package Security**
   - Store the release package in a secure location
   - Limit access to authorized personnel only
   - Consider encrypting the package during transmission

## Troubleshooting

If you encounter issues during the build process:

1. **Keystore Issues**
   - Ensure the keystore path is correct and uses double backslashes on Windows
   - Verify that the keystore passwords are correct

2. **Build Failures**
   - Check Gradle output for specific errors
   - Ensure all dependencies are using compatible versions

3. **Test Failures**
   - Review test results and fix any failing tests before final release

## References

- See `RELEASE_README.md` for an overview of the release process
- See `release_package_guide.md` for detailed instructions on using the release package
- See `windows_deployment_guide.md` for Windows-specific deployment instructions
- See `verification_checklist.md` for pre-release verification steps