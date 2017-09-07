import groovy.json.JsonSlurper

outputFile = "nuxeo-studio-registries.json"
touchFile = new File(basedir, "target/" + outputFile)
assert touchFile.isFile()
assert !new File(basedir, "multi-bundle-core/target/" + outputFile).isFile()
assert !new File(basedir, "multi-bundle-server/target/" + outputFile).isFile()

// Should also check if the file is correctly containing contributions
def json = new JsonSlurper().parse(touchFile)
assert json instanceof Map
assert json.operations instanceof ArrayList
assert json.operations.size == 2