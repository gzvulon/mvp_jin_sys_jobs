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

    stage('build') { steps {
        jinBuildJob('sysjin/samples/DeclarativeSample', copy_arts: true)
        sh('ls')
    }}    
}
}
