import groovy.json.JsonSlurper

File touchFile = new File(basedir, "nuxeo-studio-registries.json")
assert touchFile.isFile()

// Should also check if the file is correctly containing contributions
def json = new JsonSlurper().parse(touchFile)
assert json instanceof Map
assert json.lifecycles instanceof Map
assert json.schemas instanceof Map