mvn --log-file build.log --errors --fail-at-end --file rifGenericLibrary --file rifServices clean validate compile package install war:war war:inplace
