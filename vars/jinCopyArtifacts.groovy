#!/usr/bin/env groovy

//  jinCopyArtifacts.groovy
def call(Map map = [:], jobname) {
    def fingerprintArtifacts = map.fingerprintArtifacts ?: false
    def filter = map.filter ?: '**/*'

    def selector = []
    if (map.buildNumber) {
        selector = [$class: 'SpecificBuildSelector',
                    buildNumber: map.buildNumber.toString()]
    } else {
        selector =  [$class: 'StatusBuildSelector', stable: false]
    }

    step([$class              : 'CopyArtifact', 
          fingerprintArtifacts: fingerprintArtifacts,
          projectName         : jobname,
          selector            : selector,
          filter: filter
    ])
}
