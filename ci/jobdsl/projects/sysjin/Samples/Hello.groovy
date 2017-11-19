
import lib.Esl
// --------------------------------
// should be places in script file 
def getScriptEnvVars(){
    def env = [:]
    env['__FILE__'] = "${__FILE__}"
    env['WORKSPACE'] = "${WORKSPACE}"
    env['GIT_COMMIT'] = "${GIT_COMMIT}"
    env['GIT_BRANCH'] = "${GIT_BRANCH}"
    env['JENKINS_SERVER'] = "${JENKINS_SERVER}"
    env['DEFAULT_JIN_BRANCH'] = "${DEFAULT_JIN_BRANCH}"
    env['JOB_NAME'] = "${JOB_NAME}"
    return env
}
// --------------------------------
def job_hello(Map map = [:], jdsl, job_name) {
    if (Esl.shouldSkipJob(job_name, "${DSL_JOB}")) {
        return
    }

    def the_job = jdsl.job(job_name){
        steps {
            shell("echo hello")
        }
    }
    return the_job
}

folder('sysjin')
folder('sysjin/samples')
job_hello(this, 'sysjin/samples/Hello')

