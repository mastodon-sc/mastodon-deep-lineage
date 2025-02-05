# Release

* [ ] Merge Open Pull Requests
* [ ] Documentation
    - [ ] Update Documentation in the repo
    - [ ] Update Documentation on readthedocs
    - [ ] Screenshots with demo data
    - [ ] Screencasts
    - [ ] Point to example data (e.g. celltrackingchallenge, Kos data from Elephant paper)
* [ ] Check out the latest master branch
* [ ] Update license information
    * [ ] Open command line and navigate to the project root directory
    * [ ] Run "mvn license:update-file-header" and commit
* [ ] Update release date and version in CITATION.cff file and commit
* [ ] Run release Script (de-snapshots, sets a tag and sets the next snapshot version, generates javadoc, runs unit
  tests)
    * [x] Check, if github action is installed, which copies the release to maven.scijava.org
    * [ ] For reference: Release script https://github.com/scijava/scijava-scripts/blob/main/release-version.sh
    * [ ] Clone https://github.com/scijava/scijava-scripts repo
    * [ ] Ensure that one of the git remotes has the name "origin"
    * [ ] Close IntelliJ, if open
    * [ ] Run sh /path/to/release-version.sh from the mastodon-deep-lineage root directory
    * [ ] Confirm version number
    * [ ] The release script pushes to master on github.com
        * This triggers a *github Action* which copies the version to be released to maven.scijava.org.
          cf. https://maven.scijava.org/#nexus-search;quick~mastodon-deep-lineage)
* [ ] Download created jar file from scijava Nexus (https://maven.scijava.org/#nexus-search;quick~mastodon-deep-lineage)
    * [ ] Delete jar-file from last release version from local Fiji installation path
    * [ ] Copy jar file of the new version to local Fiji installation path
    * [ ] Test, if Fiji starts successfully
    * [ ] Test new functionalities of released version in Fiji
* [ ] Copy Jar-File to Mastodon-DeepLineage Update-Site using Fiji/ImageJ Updaters (Fiji > Help > Update...)
    * [ ] Set Updater to Advanced Mode
        * [ ] If needed, add `webdav:user_name_for_update_site` as `Host` under
          `Manage Update Sites > Mastodon-DeepLineage`
    * [ ] Upload mastodon-deep-lineage-release-version.jar
    * [ ] Check Upload success: https://sites.imagej.net/Mastodon-DeepLineage/jars/
* [ ] Add Release documentation: https://github.com/mastodon-sc/mastodon-deep-lineage/releases
* [ ] Check Release on Zenodo https://zenodo.org/account/settings/github/repository/mastodon-sc/mastodon-deep-lineage
  or https://zenodo.org/doi/10.5281/zenodo.10262664
