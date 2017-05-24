File touchFile = new File(basedir, "target/nuxeo-studio-registries.json")
assert touchFile.isFile()

// Should also check if the file is correctly containing those operations