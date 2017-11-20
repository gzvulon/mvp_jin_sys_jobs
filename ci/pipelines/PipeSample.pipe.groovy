timestamps {
node ('master') {
timeout(time: 3, unit: 'HOURS') {
    stage('sample'){
        sh('echo hello')
        sh('ls ..')
    }
}}}
