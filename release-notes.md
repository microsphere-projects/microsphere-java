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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.7...0.3.8## v0.3.9

# Release Notes - Version 0.3.9

## Dependency Updates
- Bumped **microsphere-bom** to version 0.2.8.
- Updated parent version to 0.3.4.

## Build and Workflow Enhancements
- Synced `main` and `release` branches for improved workflow consistency.  
- Incremented version to the next patch after publishing 0.3.8.

---

**Note:** This release primarily includes dependency updates and build process improvements.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.8...0.3.9## v0.3.10

# Release Notes - Version 0.3.10

## Dependency Updates
- **Bumped** `io.github.microsphere-projects:microsphere-all-bom` to `0.2.9`. ([#289](https://github.com/microsphere-projects/microsphere/issues/289))

## Build and Workflow Enhancements
- Merged `main` into `release` branch. [skip ci]
- Merged `release` into `main` branch. [skip ci]

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.9...0.3.10## v0.3.11

# Release Notes - Version 0.3.11

## Dependency Updates
- Bumped Microsphere BOM to `0.3.0`. ([0edf969d](#))
- Updated Microsphere Build Parent version. ([48fc5fb2](#))

## Build and Workflow Enhancements
- Merged `main` into `release` branch. ([1d67107b](#))
- Merged `release` into `main` branch. ([96804629](#))

## Other Changes
- Bumped version to next patch `0.3.11` post publishing `0.3.10`. ([95f7b037](#))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.10...0.3.11## v0.3.12

# Release Notes: Version 0.3.12

### Dependency Updates
- **Microsphere BOM**: Updated to version `0.3.1`. ([5ade306e](commit-link))

### Build and Workflow Enhancements
- Merged `main` into `release`. ([b1240e5f](commit-link))
- Merged `release` into `main`. ([dd0d50c9](commit-link))

### Other Changes
- Updated version to prepare for the next patch after publishing `0.3.11`. ([b26e6ce1](commit-link))

---

For complete details, see the [full changelog](#).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.11...0.3.12## v0.3.13

# Release Notes - Version 0.3.13

## Dependency Updates
- **Microsphere BOM** upgraded to `0.3.2`. ([2ab2ed87](...))
- **Microsphere Build** updated to `0.3.6`. ([7e6396c6](...))

## Build and Workflow Enhancements
- Merged `main` into `release` and vice versa. ([cc2df07b](...), [3b5f412c](...))
- Bumped version to the next patch post `0.3.12`. ([15cf1b36](...)) 

---

*No new features, bug fixes, or documentation updates in this release.* 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.12...0.3.13## v0.3.14

# Release Notes - Version 0.3.14

## Dependency Updates
- Bumped Microsphere BOM to `0.3.3`. ([31f2d21b](#))
- Bumped Microsphere Build Parent to `0.3.7`. ([647b4672](#))

## Build and Workflow Enhancements
- Merged `main` into `release`. [skip ci] ([c3fef602](#), [114da2cb](#))
- Merged `release` back into `main`. [skip ci] ([0b2dcac2](#))

## Other Changes
- Bumped version to the next patch after publishing `0.3.13`. ([155bb4fc](#))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.13...0.3.14## v0.3.15

# Release Notes - Version 0.3.15

## Dependency Updates
- Upgraded Microsphere BOM to version `0.3.4`.  
- Updated Microsphere Build parent to version `0.3.8`.

## Build and Workflow Enhancements
- Merged `main` branch into `release`, ensuring parity. [skip-ci]  

## Test Improvements
- Adjusted spacing and formatting in various test classes:  
  - `MessagerUtilsTest`  
  - `MapTypeModelTest`  
  - `FormatBeforeTest` super call.  

## Code Quality Improvements
- Standardized indentation and formatting in utility classes:  
  - `SetUtils`  
  - `QueueUtils` factory methods.  
- Trimmed unnecessary whitespace in `StringUtils`.  
- Aligned wrapped `assertTrue` message indentation.  
- Improved Javadoc `@param` formatting in `Compatible`.  
- Aligned parameter indentation in `Maps` methods.  
- Removed unused `FILE_SEPARATOR` import.

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.14...0.3.15## v0.3.16

_Release notes generation failed. Raw commits since 0.3.15:_

```
9fca9561 chore: merge main into release [skip ci]
3631cc86 Restore lambda listener dispatcher test
b36fe068 chore: merge main into release [skip ci]
affc59b6 Support lambda event type resolution
d84c76f4 chore: merge main into release [skip ci]
892ad21c Improve lambda parameter type resolution
d9acdf89 chore: merge main into release [skip ci]
f2ca1f5f Add lambda parameter type resolution utility
6c526acf Fix functional method selection in MethodUtils
885c817e chore: merge main into release [skip ci]
74e2eaeb Tighten LambdaUtils generic typing
eb08167d chore: merge main into release [skip ci]
b134fd58 Fix lambda instantiated method signatures
cb0eb29a chore: merge main into release [skip ci]
2fe70ac0 Add LambdaUtils for runtime lambda creation
d99ac051 Fix typo in method candidate helper name
ca8827e6 chore: merge main into release [skip ci]
17c14ee4 Update UnsafeUtilsTest.java
54c5932c Update MethodUtils.java
6966ad37 chore: merge main into release [skip ci]
fb31a0f1 Extract lambda class name prefix constant
858fe5f9 chore: merge main into release [skip ci]
3ed4c254 Use Runnable in functional method test
62242b52 chore: merge main into release [skip ci]
6245ce7b Add @see links to Modifier enum constants
610e1c97 chore: merge main into release [skip ci]
6655c096 Add functional interface method lookup
9c7d22ec Add aggregate modifier match helpers
f4fb37a7 Add Javadocs for bean method helpers
61841dfa chore: merge main into release [skip ci]
b68334d7 Refine lambda class detection in ClassUtils
1a3df1ae chore: merge main into release [skip ci]
a812fed9 Add interface checks to ClassUtils
102461a8 Add Object override check to MethodUtils
aedae9f5 chore: merge main into release [skip ci]
d6bfd347 Add synthetic and lambda class checks
2b444df9 Make EchoEvent public in tests
814076a2 chore: merge main into release [skip ci]
6db932c1 Use package-style logger name in test
c5208bb9 chore: merge main into release [skip ci]
3b4ed744 Tune test logging with async file appender
863a0b11 chore: merge main into release [skip ci]
773b19f5 Capture and trace Java compiler output
9fb795be chore: merge main into release [skip ci]
03077b0b Disable immediate flush in test logback config
fd1e4cb3 chore: merge main into release [skip ci]
d338a3ed Disable unchecked lint in default options
d46569af chore: merge main into release [skip ci]
caed26bb Remove test logback config file
e06bf8b5 chore: merge main into release [skip ci]
331a777d Consolidate test logback logger config
f88777b3 chore: merge main into release [skip ci]
3dfeb5ca Remove JavaLangAccessUtils and adjust test logging
fc44f22e chore: merge main into release [skip ci]
5839118a Extract annotation testing into new module
40f5528b chore: merge main into release [skip ci]
9d4f02c1 Relax constant pool constants test
44bc6561 chore: merge main into release [skip ci]
ae46d68d Add ConstantPool reflection utilities and tests
7d05d7c7 chore: merge main into release [skip ci]
b9eb1bd7 Use ExceptionUtils.wrap in FieldUtils
3bc47e11 Wrap invocation target exceptions consistently
b369d506 chore: merge main into release [skip ci]
89ed04e3 Handle null fields and prevent reflection cycles
bb7cd0d2 Add JavaLangAccess utility and basic test
327ece9b chore: merge main into release [skip ci]
dfc97f48 Enforce non-null Field in FieldUtils accessors
47a6df96 chore: merge main into release [skip ci]
98ff2172 Improve reflection trace log details
70373b4f Add trace logging for reflective field reads
27de5700 Simplify invokeMethod exception handling
8641fc8d chore: merge main into release [skip ci]
6522801a Bump microsphere-bom to 0.3.8
27550bce chore: merge main into release [skip ci]
f62399e0 Bump microsphere-build parent to 0.3.11
b9884353 chore: merge main into release [skip ci]
51a2fc93 Add typed getFieldValue test assertions
12cde2f7 chore: merge main into release [skip ci]
b548ee3d Use declared static call for enum values
98e32de9 chore: merge main into release [skip ci]
c8940ee1 Simplify enum values lookup in serializer
8fc8754d Update staticFieldBase API and unsafe tests
1db3ef3d chore: merge main into release [skip ci]
b36dff18 Expand memory ops coverage in UnsafeUtilsTest
39157731 chore: merge main into release [skip ci]
c83ebb82 Add field-name atomic ops to UnsafeUtils
f2d3c205 chore: merge main into release [skip ci]
8773057e Add ByteSerializer and expand UnsafeUtils tests
6c497129 chore: merge main into release [skip ci]
13073943 Extract byte-size constants into SizeUtils
5f23c901 chore: merge main into release [skip ci]
acd7b83a Add char and float SPI serializers
82a3969b chore: merge main into release [skip ci]
2bea2862 Refine serializer APIs and enum tests
7efb13c1 chore: merge main into release [skip ci]
5855ab24 Refactor serializer API and add primitive codecs
6e899528 chore: merge main into release [skip ci]
30078e58 Add primitive byte-size constants to IOUtils
ae1124f7 chore: merge main into release [skip ci]
9b3f60fd Add UnsafeUtils off-heap and size tests
e9c4bf21 chore: merge main into release [skip ci]
013c5956 Add field-name CAS helpers to UnsafeUtils
4df3275e chore: merge main into release [skip ci]
4ada2439 Align UnsafeUtils volatile API naming
3f4c2872 chore: merge main into release [skip ci]
7817fa2c Expand UnsafeUtils with full Unsafe wrappers
4bf25614 chore: merge main into release [skip ci]
8a327f36 Add null supplier test for execute()
91f8f846 Align UnsafeUtils volatile array exceptions
4e483472 chore: merge main into release [skip ci]
e1ddc25e Decouple UnsafeUtils from BaseUtils
e95e46ad chore: merge main into release [skip ci]
8ced8d98 Force accessible URL handler instantiation
f8815056 chore: merge main into release [skip ci]
df520478 Fix builder test instantiation path
2079d27e chore: merge main into release [skip ci]
ca486e0f Use static import for LookupMode.ALL
d7a134f5 Remove IMPL_LOOKUP trusted lookup fallback
623cd454 chore: merge main into release [skip ci]
12f96db0 Add newInstance exception behavior tests
1c527757 chore: merge main into release [skip ci]
d09c0e70 Fix invokeMethod argument type matching
1f9c1799 Refactor constructor instantiation utilities
0210787f chore: merge main into release [skip ci]
8525afb6 Add force-access constructor instantiation APIs
864085b2 chore: merge main into release [skip ci]
0ad5bce9 Reorder FieldUtils forceAccess arguments
ebcc5df3 chore: merge main into release [skip ci]
aedeabb9 Use accessible reflective calls in type utilities
50de0bb2 chore: merge main into release [skip ci]
eeb5cd60 Fix JavaFileManager field access in FilerProcessor
d81e24fc chore: merge main into release [skip ci]
f79eba1a Fix class loader field access for loaded classes
57b269ed chore: merge main into release [skip ci]
fe62b348 Force reflective access for JDK internals
c01ed8ae chore: merge main into release [skip ci]
a6de3683 Use MethodHandle in AccessibleObjectUtils
a318cc09 chore: merge main into release [skip ci]
7dfff8d1 Add forceAccess support in reflection helpers
a3350b2b chore: merge main into release [skip ci]
44c11a46 Bump microsphere BOM to 0.3.7
60ea9946 chore: merge main into release [skip ci]
d30db57a Refactor UnsafeUtils to reflective invocation
d25da10d Fix expected exception in PID resolver test
0ed9df20 Refine MethodUtils invocation and cache APIs
2252b512 Add declaring class validity helper
4877130a Use IllegalArgumentException in assertNotNull
15b07d2f Remove final-class check in listener assertion
0ff89c22 chore: merge main into release [skip ci]
e897430d Switch access helpers from MethodHandle to Method
926331ed Remove obsolete reflection access APIs
90f90931 Align MethodUtils invocation error handling
408e0de3 Throw NPE when invokeMethod target not found
fb14aa4c Use Lookup type alias in lookup utils
2660950e Use NPE for Assert.assertNotNull failures
43c17a02 chore: merge main into release [skip ci]
35f7a64e Bump microsphere BOM to 0.3.6
97ccc4d8 chore: merge main into release [skip ci]
b7a31b77 Bump microsphere-bom to 0.3.5
de76e18f chore: merge main into release [skip ci]
13698392 Bump microsphere-build parent to 0.3.10
eac75869 chore: merge main into release [skip ci]
f3a9f009 Refine listener loading in event dispatcher
94478638 Add nullable EventDispatcher factory
02981d65 Bump microsphere-build parent to 0.3.9
f40ba0f0 chore: merge release into main [skip ci]
5ded0961 chore: bump version to next patch after publishing 0.3.15
```

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.15...0.3.16## v0.3.17

_Release notes generation failed. Raw commits since 0.3.16:_

```
2d43ab21 chore: merge main into release [skip ci]
1da7b5dd Fix array method expectations in MethodUtilsTest
1aa91811 chore: merge main into release [skip ci]
8f155dc6 Fix array method filtering with force access
18d3fbe6 Align invokeMethod overload parameter order
1e6ed431 Add zero-arg method case to parameter match test
a118f68b chore: merge release into main [skip ci]
eb7cc735 chore: bump version to next patch after publishing 0.3.16
```

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.16...0.3.17## v0.3.18

_Release notes generation failed. Raw commits since 0.3.17:_

```
71cdf79c chore: merge main into release [skip ci]
9d8e21cb Merge pull request #293 from microsphere-projects/dependabot/maven/org.springframework-spring-framework-bom-7.0.9
5fe0d803 Merge branch 'main' into dependabot/maven/org.springframework-spring-framework-bom-7.0.9
05706174 chore: merge main into release [skip ci]
def48b78 Bump microsphere BOM to 0.3.10
98a7b8d6 Bump microsphere-build to 0.3.14
bdcb1610 Bump org.springframework:spring-framework-bom from 7.0.8 to 7.0.9
089c4e7f chore: merge main into release [skip ci]
552cea36 Merge pull request #292 from microsphere-projects/dependabot/maven/org.apache.maven-apache-maven-3.9.16
22787132 Bump org.apache.maven:apache-maven from 3.9.15 to 3.9.16
b8a3732a chore: merge main into release [skip ci]
66682b1c Update pom.xml
61c5b4ad chore: merge main into release [skip ci]
4edf64b7 Bump microsphere-build to 0.3.12
39f2c6ab chore: merge main into release [skip ci]
5af1f547 Add NIO Files readLines utility and tests
ec442946 chore: merge main into release [skip ci]
7358c625 Add LoggerUtils with level-aware lazy logging
1cae8575 chore: merge release into main [skip ci]
95f13f3f chore: bump version to next patch after publishing 0.3.17
```

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.17...0.3.18## v0.3.19

_Release notes generation failed. Raw commits since 0.3.18:_

```
7b8bce60 chore: merge main into release [skip ci]
821dc49e Fix defaultIfNull null handling
fee46a15 chore: merge release into main [skip ci]
9604ad40 chore: bump version to next patch after publishing 0.3.18
```

**Full Changelog**: https://github.com/microsphere-projects/microsphere-java/compare/0.3.18...0.3.19