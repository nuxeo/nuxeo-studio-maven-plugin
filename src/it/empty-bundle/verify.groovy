File touchFile = new File(basedir, "target/nuxeo-studio-registries.json")
assert touchFile.isFile()

// Should also check that the file is an empty json