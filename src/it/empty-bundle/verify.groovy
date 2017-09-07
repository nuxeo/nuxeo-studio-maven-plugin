import groovy.json.JsonSlurper

File touchFile = new File(basedir, "target/nuxeo-studio-registries.json")
assert touchFile.isFile()

def json = new JsonSlurper().parse(touchFile)
assert json instanceof Map
assert json.entrySet().isEmpty()