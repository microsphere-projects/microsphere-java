# Release Notes

## v0.2.7

# Release Notes - Version 0.2.7

## New Features
- **Artifact Detector Enhancements**:  
  - Added `Version.ofVersion(Class)` for artifact detection. ([6a2e5273](#))
  - Refactored `ArtifactDetector` detection API. ([68cc094c](#))
- **Version Improvements**:  
  - Added a test for `javax.annotation.Nullable`. ([d2e39cd7](#))
  - Refactored `Version` behavior. ([04feb0ef](#))

## Bug Fixes
- Added workflow permissions and cleaned branch lists to fix sync issues. ([874d276f](#))
- Skipped syncing fork branches that are ahead of the main branch. ([3e9f72ba](#))

## Other Changes
- Added trace log when resources are not archives. ([86d32c79](#))
- Removed project-specific name from script documentation. ([e0c43275](#))
- Updated `maven-publish` workflow with improved checkout and permissions. ([bd2aa9f4](#))
- Introduced a new workflow to sync fork branches from the upstream repository. ([7b30819a](#))
- Bumped `io.github.microsphere-projects:microsphere-all-bom`. ([d2b2164d](#))
- Added Copilot-generated release notes as a workflow step. ([bada3a10](#))

---

For specific changes, refer to the commit hashes linked above.

## v0.2.8

# Release Notes - Version 0.2.8

## Dependency Updates
- Bumped `io.github.microsphere-projects:microsphere-all-bom` to version `0.2.0`. ([#270](https://github.com/microsphere-projects/microsphere-all-bom))

## Build and Workflow Enhancements
- Enhanced GitHub Actions Maven workflow for tidier execution.  
- Updated `dependabot.yml` for improved dependency management.  
- Improved release notes generation and release creation process.  

## Other Changes
- Removed unnecessary newlines in files for improved formatting.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.2.7...0.2.8## v0.2.9

# Release Notes for Version 0.2.9

## Dependency Updates
- **Spring Framework BOM**: Upgraded from `7.0.6` to `7.0.7`. ([#271](https://github.com/microsphere-projects/microsphere-java/pull/271))
- **microsphere-bom**: Updated to `0.2.1`.

## Build and Workflow Enhancements
- Upgraded Maven Wrapper to `3.9.14`.  
- Formatted Java versions matrix spacing for enhanced readability.  

## Other Changes
- Version bumped to `0.2.9` post-release of `0.2.8` for upcoming development.

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.2.8...0.2.9## v0.3.0

# Release Notes - Version 0.3.0

## Dependency Updates
- Updated Maven wrapper to version 3.9.15 using Aliyun mirror. ([bbba1673](#))
- Switched Maven wrapper to use the official Maven Central repository. ([ccf5dd64](#))
- Updated Microsphere BOM dependencies. ([3c12fd82](#))
- Imported Logging and Testing BOMs to improve dependency management. ([24ea9826](#))

## Build and Workflow Enhancements
- Merged main into release and vice versa to sync branches. ([67b7c00c](#), [37bb7254](#), [994381b6](#), [aa650593](#))
- Bumped version to next patch after publishing 0.2.9. ([0e0813e0](#))

---

For more details, refer to the full changelog in the repository.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.2.9...0.3.0## v0.3.1

# Release Notes - Version 0.3.1

## New Features
- Collect multiple artifacts and add trace log for enhanced observability. ([db9cb00f](#))

## Dependency Updates
- Adopt `microsphere-all-bom` to streamline dependencies.  
- Remove outdated `testing/javaee` BOMs. ([56fa380e](#))

## Other Changes
- Version bump to prepare for post-0.3.0 development. ([c18526da](#)) 

**Note**: This release includes minor enhancements and dependency refinements to improve overall functionality. 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.0...0.3.1## v0.3.2

# Release Notes - Version 0.3.2

## Build and Workflow Enhancements
- Added Maven server credentials to the CI/CD workflow for improved deployment handling. ([61662808](https://example.com))
- Adjusted Maven workflows and implemented a minor script fix. ([f0b76c57](https://example.com))

## Dependency Updates
- Upgraded Microsphere parent and BOM versions for better compatibility. ([c20b4517](https://example.com))

## Other Changes
- Version bumped to the next patch following 0.3.1 release. ([a9ebadf8](https://example.com))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.1...0.3.2## v0.3.3

# Release Notes - Version 0.3.3

## New Features
- Improved `loadService` method to utilize `ListUtils.first/last` for enhanced readability and efficiency. ([commit](https://github.com/microsphere-projects/commit/1f865f56))

## Documentation
- Adjusted spacing between badges in README for clearer layout. ([commit](https://github.com/microsphere-projects/commit/299fd82c))
- Fixed a typo in README ("lang-model"). ([commit](https://github.com/microsphere-projects/commit/91f34cc2))

## Dependency Updates
- Upgraded `microsphere-all-bom` to version `0.2.4`. ([commit](https://github.com/microsphere-projects/commit/b1d25a40), [commit](https://github.com/microsphere-projects/commit/0d082a7c))

## Build and Workflow Enhancements
- Merged `main` into `release` branch multiple times to sync updates. ([commit](https://github.com/microsphere-projects/commit/86f7a170), [commit](https://github.com/microsphere-projects/commit/8f6bca60), [commit](https://github.com/microsphere-projects/commit/d650f39b))
- Merged `release` into `main` to finalize updates. ([commit](https://github.com/microsphere-projects/commit/537a8a55))

---

This update includes general enhancements, improved documentation, and dependency updates to keep the project up-to-date.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.2...0.3.3## v0.3.4

# Release Notes - Version 0.3.4

## Bug Fixes
- Added null checks and logging in `JarUtils` for improved error handling. ([8a15d444](https://github.com/microsphere-projects/commit/8a15d444))
- Fixed `JarEntry/JarFile` independence issue with a clarifying comment in `findJarEntry`. ([2595480f](https://github.com/microsphere-projects/commit/2595480f))
- Applied 10 bug fixes from code review to ensure stability improvements. ([73afeb04](https://github.com/microsphere-projects/commit/73afeb04))

## Documentations
- Added `onboarding-plan.md` to assist new team members with onboarding. ([d2c3cb4f](https://github.com/microsphere-projects/commit/d2c3cb4f))
- Rewritten `README.md` with comprehensive content and improved structure. ([51abcbdf](https://github.com/microsphere-projects/commit/51abcbdf))

## Build and Workflow Enhancements
- Added `.github` agent prompt templates for API, code, and tests. ([4e7efbea](https://github.com/microsphere-projects/commit/4e7efbea), [b6bf6752](https://github.com/microsphere-projects/commit/b6bf6752))
- Customized onboarding prompt with organization context and background. ([f66b08c8](https://github.com/microsphere-projects/commit/f66b08c8))

## Other Changes
- Refactored various utilities in `JarUtils` and `StreamArtifactResourceResolver` for clearer, concise code:
  - Replaced `IOUtils` methods for cleanup and byte array operations. ([b4cae21d](https://github.com/microsphere-projects/commit/b4cae21d), [d69cdf02](https://github.com/microsphere-projects/commit/d69cdf02))
  - Simplified `ResourceProcessor.exists` logic. ([6f39ef55](https://github.com/microsphere-projects/commit/6f39ef55))

---

✔️ **Upgrade recommended**: Incorporates important bug fixes and clearer documentation updates.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.3...0.3.4## v0.3.5

# Release Notes: Version 0.3.5

## New Features
- Introduced `nullSafe` and `defaultIfNull` helpers in `ObjectUtils` and associated test cases. ([c24a8d3a](https://github.com/microsphere-projects/...))
- Added `readLines` API in `IOUtils` with improved charset handling and default to `FILE_ENCODING`. ([fa15f203](https://github.com/microsphere-projects/...))
- Included `nullSafeClassLoader` for safer class loader handling across multiple modules. ([87d29011](https://github.com/microsphere-projects/...))
- Added service provider class discovery mechanism. ([54f42608](https://github.com/microsphere-projects/...))

## Documentation
- Added **user-guide.md** targeting developers with varying experience levels. ([006fd686](https://github.com/microsphere-projects/...))
- Linked the new **User Guide** in the Documentation table of `README.md`. ([e56a00df](https://github.com/microsphere-projects/...))

## Dependency Updates
- Bumped `microsphere-bom` version to `0.2.5`. ([4bdfc619](https://github.com/microsphere-projects/...))
- Updated parent POM version to `0.3.0`. ([5ee3806d](https://github.com/microsphere-projects/...))

## Test Improvements
- Added comprehensive test cases for `nullSafe` and `defaultIfNull`. ([d5799b3f](https://github.com/microsphere-projects/...))
- Enhanced `IOUtils` test suite with additional charset validation. ([a0d18c08](https://github.com/microsphere-projects/...))

## Build and Workflow Enhancements
- Cleaned up redundant `@since` tags and improved Javadocs across multiple files. ([4ed7692d](https://github.com/microsphere-projects/...))
- Merged multiple updates and changes from the `main` branch into `release`. ([f32bd04b](https://github.com/microsphere-projects/...), [f675a18f](https://github.com/microsphere-projects/...), etc.)
- Updated prompt templates and onboarding files. ([cf27bf1e](https://github.com/microsphere-projects/...))
  
## Other Changes
- Made extensive use of `SetUtils` and `MapUtils` helper methods across codebase for cleaner factory method implementations. ([fc3449c6](https://github.com/microsphere-projects/...), [3181249a](https://github.com/microsphere-projects/...))
- Improved null handling in various utilities by using `nullSafe` and `defaultIfNull` methods. ([ce0d215f](https://github.com/microsphere-projects/...), [d75c00dd](https://github.com/microsphere-projects/...))

For a detailed list of changes, see the [Full Changelog](https://github.com/microsphere-projects/.../compare/0.3.4...0.3.5).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.4...0.3.5## v0.3.6

# Release Notes: Version 0.3.6

## New Features
- **Factory Methods Added**:
  - Introduced new factory methods for collections, including:
    - `TreeSet`, `ArrayList`, `LinkedList`, `ArrayDeque`
    - Concurrent collections: `newFixedLinkedHashSet`, `newFixedHashSet`, `newFixedLinkedHashMap`, `newFixedHashMap`, etc. ([#286](https://github.com/microsphere-projects/issues/286), [#283](https://github.com/microsphere-projects/issues/283)).
  - Added `ThreadSafe` and `NotThreadSafe` annotations for better code clarity. ([#282](https://github.com/microsphere-projects/issues/282))

## Bug Fixes
- Fixed type mismatches and reverted inappropriate changes in `ReversedDequeTest`.  
- Corrected raw type constructors and local variable types in `SetUtils`.  
- Fixed import issues and removed wildcard imports across core classes.

## Documentation
- Revised JavaDocs for collection utility methods for better clarity.  
- Corrected mismatches in method documentation, ensuring accurate terminology (e.g., `mappings` to `elements`).

## Test Improvements
- Added comprehensive test cases for newly introduced collection factory methods.  

## Build and Workflow Enhancements
- Removed duplicated line separators, trailing whitespace, and unnecessary final newlines across Java files for cleaner codebase.  

## Other Changes
- Refactored collection constructor return types from interfaces to concrete collection types.  
- Organized imports and restored blank lines between import sections in Java files.  

**Full Changelog**: [v0.3.5...v0.3.6](https://github.com/microsphere-projects/compare/v0.3.5...v0.3.6)  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.5...0.3.6## v0.3.7

# Release Notes - Version 0.3.7

## Dependency Updates
- Bumped `microsphere-bom.version` to `0.2.6`. ([a8ffffda](#))

## Build and Workflow Enhancements
- Bumped parent POM version to `0.3.1`. ([21140f70](#))
- Version updated to next patch after publishing `0.3.6`. ([442c5a47](#))
- Merged `main` into `release` for version alignment. ([8cc3b499](#), [ef638e22](#))

---

No new features, bug fixes, or documentation updates in this release.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.6...0.3.7## v0.3.8

# Release Notes - Version 0.3.8

## 🌟 New Features
- Added `loadProperties` helper utility with tests. ([8e42d20b](#))
- Introduced `arrayToString` utility with tests. ([b78cb2c9](#))

## 📖 Documentation
- Reformatted README for improved readability (line wrapping and adjusted layout). ([225828ae](#), [cfbcc8ac](#))

## 🔗 Dependency Updates
- Updated `microsphere-bom.version` to `0.2.7`. ([a68740cf](#))
- Bumped `org.springframework:spring-framework-bom` from `7.0.7` to `7.0.8`. ([8c33b6e0](#))
- Bumped parent project version to `0.3.3`. ([79ef1703](#))

## 🧪 Test Improvements
- Included tests for the new utility methods (`loadProperties` and `arrayToString`). ([8e42d20b](#), [b78cb2c9](#))

## 🛠️ Build and Workflow Enhancements
- Added `.understand-anything` analysis artifacts. ([4bbfbf8c](#))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.7...0.3.8