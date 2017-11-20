#!/usr/bin/env groovy

//  jinBuildJob.groovy
def call(Map map = [:], jobname) {
    def propagate = map.propagate ?: false
    def wait = map.wait ?: true
    def params = map.params ?: [:]
    def copy_arts = map.copy_arts ?: false
    def filter = map.filter ?: null
    if (filter){
        copy_arts = true
    }

    def parameters = []
    for (p in params){
        parameters.add(string(name: p.key, value:p.value))
    }

    def buildResult = build(
        job: jobname,
        parameters: parameters,
        propagate: propagate,
        wait: wait)

    if (copy_arts){
        jinCopyArtifacts(jobname,
            buildNumber: buildResult.getNumber(),
            filter: filter
        )
    }

    return buildResult
}
