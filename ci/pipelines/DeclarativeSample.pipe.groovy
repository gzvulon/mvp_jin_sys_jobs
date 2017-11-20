
pipeline {
agent { label "master" }
options {
    timestamps()

    // we don't fill up our storage!
    buildDiscarder(logRotator(numToKeepStr:'60'))

    // let's time it out after an hour.
    timeout(time: 60, unit: 'MINUTES')

}
stages {
    stage('cleanup') { steps {
        sh('rm -rf *')
    }}

    stage('checkout') { steps {
        checkout([$class: 'GitSCM', 
            branches: [[name: 'ivanne/sys_cfg']], 
            doGenerateSubmoduleConfigurations: false, 
            extensions: [
                [$class: 'CloneOption', 
                    depth: 0, noTags: false, reference: '', shallow: true], 
                [$class: 'CheckoutOption'], 
                [$class: 'RelativeTargetDirectory', 
                    relativeTargetDir: 'mvp_jin_sys_jobs']], 
            submoduleCfg: [], 
            userRemoteConfigs: [[url: 'https://github.com/gzvulon/mvp_jin_sys_jobs']]])
    }}

    stage('prepare'){ steps {
        sh 'rm -rf packaged_build'
    }}

    stage('create'){ steps {
        sh 'mkdir packaged_build'
        sh 'echo hi man > packaged_build/msg.txt'
    }}    
}

post {
    always {
        archive 'packaged_build/**/*'
    }
}

}
