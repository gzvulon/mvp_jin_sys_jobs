@Library('sysjin') _

pipeline {
agent { label "master" }
options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr:'60'))
    timeout(time: 60, unit: 'MINUTES')
}
stages {
    stage('cleanup') { steps {
        sh('rm -rf *')
    }}

    stage('build one') { steps {
        jinBuildJob('sysjin/samples/PipeSample')
        sh('ls')
    }}

    stage('build many') { steps {
        parallel(
            'DeclarativeSample': {
                jinBuildJob('sysjin/samples/DeclarativeSample', copy_arts: true)
            },
            'LibsDeclarativeSample': {
                jinBuildJob('sysjin/samples/LibsDeclarativeSample', copy_arts: true)
            }
       )
    }}   

    stage('build many 2') { steps {
        script {
            parallel(jinMakeParallel([
                'DeclarativeSample',
                'LibsDeclarativeSample'
            ], { shortname -> 
                jinBuildJob("sysjin/samples/${shortname}", 
                    copy_arts: true)
            }))
        }
    }}

    stage('report') { steps {
        sh('ls')
        sh('ls packaged_build')
    }}
    
}
}
