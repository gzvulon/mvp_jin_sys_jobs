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

    stage('build decalrative') { steps {
        parallel(
            'Hello': {
                jinBuildJob('sysjin/samples/Hello', copy_arts: false)
            },
            'HelloWorld': {
                jinBuildJob('sysjin/samples/HelloWorld', copy_arts: false)
            },
            'ByeWorld': {
                jinBuildJob('sysjin/samples/ByeWorld', copy_arts: false)
            }
       )
    }}   

    stage('build scripted') { steps {
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
