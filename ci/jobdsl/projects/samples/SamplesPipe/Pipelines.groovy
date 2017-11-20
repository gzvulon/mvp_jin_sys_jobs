import lib.EslCtx

// --------------------------------
// should be places in script file 
def getScriptEnvVars(){
    def env = [:]
    env['__FILE__'] = "${__FILE__}"
    env['WORKSPACE'] = "${WORKSPACE}"
    env['GIT_COMMIT'] = "${GIT_COMMIT}"
    env['GIT_BRANCH'] = "${GIT_BRANCH}"
    env['JENKINS_SERVER'] = "${JENKINS_SERVER}"
    // env['DEFAULT_JIN_BRANCH'] = "${DEFAULT_JIN_BRANCH}"
    env['JOB_NAME'] = "${JOB_NAME}"
    return env
}
def esl = new EslCtx(this, out, getScriptEnvVars())
// --------------------------------

folder('sysjin')
folder('sysjin/samples')
esl.createPipe('sysjin/samples/PipeSample', 'PipeSample.pipe.groovy',
    jin_branch: 'ivanne/sys_cfg')
