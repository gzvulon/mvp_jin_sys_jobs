@Library('sysjin') _

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
        echo 'checkout'
        jinGithubCheckout('mvp_jin_sys_jobs', 
            user: 'gzvulon',
            commit_branch: "refs/heads/ivanne/sys_cfg")
    }}

    stage('prepare'){ steps {
        sh 'rm -rf packaged_build'
    }}

    stage('build'){ steps {
        sh 'mkdir packaged_build'
        sh 'echo hi man > packaged_build/msg.txt'
        sh 'cp -r mvp_jin_sys_jobs/ci packaged_build'        
    }}    
}

post {
    always {
        archive 'packaged_build/**/*'
    }
}

}
