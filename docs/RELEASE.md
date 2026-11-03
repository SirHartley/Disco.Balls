# Release

How Disco.Balls is built, packaged and published. Nothing here runs unless a release is asked for: no version bumps, rebuilt jars, tags or published releases for ordinary work. Changelog entry rules are in [CHANGELOG.md](CHANGELOG.md); code owners are in [ARCHITECTURE.md](ARCHITECTURE.md).

## Pieces

| Piece | Path | Role |
|---|---|---|
| Changelog | `Changelog` | Source of the version and the release notes. The newest version heading is the release |
| Mod metadata | `mod_info.json` | Version stamped into the ZIP's copy; loads `jars/MoreLampColourOptions.jar` |
| Version file | `DiscoBalls.version` | Online Version Checker file on `master`, registered in `data/config/version/version_files.csv`; stamped at packaging, committed back after a release |
| Release builder | `jars/src/disco/tools/ReleaseBuild.java` | `build` and `package`, run with `java <file>` |
| Builder checks | `jars/src/disco/tools/ReleaseBuildChecks.java` | Headless checks of changelog parsing, stamping and file filtering |
| Build libraries | `lib/` | `starfarer.api.jar`, `lwjgl.jar`, `lwjgl_util.jar`, `json.jar` and `log4j-1.2.9.jar` from game `0.98a-RC8`, copied from Industrial.Evolution's `lib/` |
| Release jar | `jars/release/MoreLampColourOptions.jar` | Built by `build`, gitignored, runtime classes only; shipped as `jars/MoreLampColourOptions.jar` |
| Release workflow | `.github/workflows/release.yml` | Manual only: preview without a version, publish with one |

The IDE artifact (`jars/MoreLampColourOptions.jar`, gitignored) and the IDE compiler output (`jars/production/`, gitignored) are separate from the release jar, so an IDE build never replaces it.

The release jar is built in CI from `lib/` and never committed, as in Tidy.Panels. Do not add the house template's committed jar and source hash check.

## Versions

Versions use `major.minor.letter`: the first number for a major release, the second for changes players will notice, the letter for fixes and small patches. Reset the lower parts when a higher one changes. The newest `Changelog` section is marked `- UNRELEASED` until it ships.

`1.1.c` was released by hand before this setup; its ZIP is named `Disco.Balls1.0.c.zip` and also carried the source as `jars/src.7z`. Packages from the builder are named `Disco.Balls-<version>.zip` and carry no source.

## Build the release jar

From the repository root, with JDK 17:

```text
java jars/src/disco/tools/ReleaseBuild.java build
```

- Compiles every file under `jars/src` against the jars in `lib/`. No game install is needed. `starfarer_obf.jar` is not needed: the code uses only API types.
- Builds into a fresh `out/release-build-*`, keeps only runtime classes (`disco/` without `disco/tools/`, plus `com/fs/starfarer/api/impl/campaign/rulecmd/MoreColour_*`), and replaces the release jar only after a clean compile. The jar must contain `disco/plugins/ModPlugin.class`.
- After a game update, copy the new jars into `lib/` and update the version in the table above.

## Package

```text
java jars/src/disco/tools/ReleaseBuild.java package          # preview
java jars/src/disco/tools/ReleaseBuild.java package 1.1.d    # release
```

- Needs a release jar from `build`.
- Preview packages the newest changelog section.
- Release requires the version (no `v`) to equal the newest changelog heading, and that section must not be `UNRELEASED`.
- Ships Git-tracked files only: `mod_info.json`, `DiscoBalls.version`, `Changelog`, license and credit files if added, `data/` (`csv json shader ship wpn proj system variant skin`), `graphics/` (`png jpg jpeg`), `sound/` and `sounds/` (`ogg wav`) and the release jar as `jars/MoreLampColourOptions.jar`. Source, `lib/`, docs, IDE, agent and workflow files, and any path under a dot, `unused`, `unedited`, `tools` or `docs` directory are left out.
- Fails if `rules.csv`, `sounds.json` or `sound/carameldansen.ogg` is missing, or if a `graphics/discoballs/` or `sound/` path named in `data/` is not packaged. The mod keeps its music in `sound/`, not vanilla's `sounds/`.
- Output in `out/release/`: `Disco.Balls-<version>.zip` (one `Disco.Balls/` folder, sorted, fixed timestamps), `release-notes.md`, the stamped version file and `version.txt`.

## Publish

1. Finish the newest `Changelog` section and remove `UNRELEASED`.
2. Run `build` and `package <version>` locally as the preview check, then merge to `master`.
3. Start the Release workflow on `master` with `version` set: Actions tab, Release, Run workflow, or `gh workflow run release.yml --ref master -f version=<version>`.
4. The workflow runs the builder checks, builds the jar, packages, creates the release and its tag on the packaged commit, then commits the stamped `DiscoBalls.version` to `master`.
5. Check the release ZIP and notes, and that `master`'s version file names the new version.

The workflow has no push, pull request, tag or schedule trigger. Without a version it only uploads a preview artifact. It refuses to overwrite an existing release, and run on an existing version tag it publishes that tag. It compiles from `lib/` and downloads nothing. Work currently happens on `dev`; the workflow appears in the Actions tab once it is on `master`.

## Maintenance

Update this guide when `ReleaseBuild`, `ReleaseBuildChecks`, `.github/workflows/release.yml`, the version file or the jars in `lib/` change.
