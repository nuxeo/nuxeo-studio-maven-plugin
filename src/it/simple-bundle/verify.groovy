import groovy.json.JsonSlurper

File touchFile = new File(basedir, "target/nuxeo-studio-registries.json")
assert touchFile.isFile()

def json = new JsonSlurper().parse(touchFile)
assert json instanceof Map
assert json.operations instanceof ArrayList
assert json.doctypes == null
assert json.schemas instanceof Map
assert json.schemas.entrySet().size() == 2