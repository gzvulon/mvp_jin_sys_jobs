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
def job_sh_script(Map map = [:], jdsl, job_name, the_script) {
    if (Esl.shouldSkipJob(job_name, "${DSL_JOB}")) {
        return
    }

    def the_title = map.the_script ?: 'HELLO'

    def the_job = jdsl.job(job_name){
        label('master')
        steps {
            shell("echo ${the_title}")
            shell("echo running ${the_script}")
            shell(jdsl.readFileFromWorkspace(the_script))
        }

        publishers {
            archiveArtifacts {
                pattern('packaged_build/**/*')
                onlyIfSuccessful()
            }
        }
    }
    return the_job
}

folder('sysjin')
folder('sysjin/samples')

job_sh_script(this, 'sysjin/samples/HelloWorld', 
    'ci/jobdsl/projects/samples/SamplesJdsl/sample.sh',
    the_title: 'HelloWorld')

job_sh_script(this, 'sysjin/samples/ByeWorld', 
    'ci/jobdsl/projects/samples/SamplesJdsl/sample.sh',
    the_title: 'ByeWorld')
    

