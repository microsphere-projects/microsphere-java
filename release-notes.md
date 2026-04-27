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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.2.8...0.2.9