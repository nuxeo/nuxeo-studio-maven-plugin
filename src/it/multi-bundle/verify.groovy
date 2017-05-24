outputFile = "nuxeo-studio-registries.json"
assert new File(basedir, "target/" + outputFile).isFile()
assert !new File(basedir, "multi-bundle-core/target/" + outputFile).isFile()
assert !new File(basedir, "multi-bundle-server/target/" + outputFile).isFile()