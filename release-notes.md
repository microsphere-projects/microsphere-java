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

