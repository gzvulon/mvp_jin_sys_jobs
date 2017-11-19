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
def job_healthcheck(Map map = [:], jdsl, job_name) {
    if (Esl.shouldSkipJob(job_name, "${DSL_JOB}")) {
        return
    }
    def the_description = Esl.createReachDescription(job_name,
        getScriptEnvVars())

    def script_path = new File(__FILE__).absolutePath
    def build_name = '#${BUILD_NUMBER}-${GIT_REVISION:0:7}'

    def the_job = jdsl.job(job_name){
        description("Managed Job. Jin Healthcheck. Generated from ${script_path}")
        // TODO: label('simple')
        label('master')
        parameters {
            stringParam('branch', 'ivanne/sys_cfg', 'jin branch')
        }
        environmentVariables {
            keepBuildVariables(true)
            keepSystemVariables(true)
        }
        logRotator {
            daysToKeep(180)
            artifactNumToKeep(60)
            artifactDaysToKeep(60)
        }
        quietPeriod(2)
        wrappers {
            timestamps()
            colorizeOutput('xterm')
            buildName(build_name)
            timeout {
                noActivity(270)
                failBuild()
                writeDescription('Build failed due to timeout after {0} minutes')
            }
        }
        scm {
            git {
                remote {
                    url('https://github.com/gzvulon/mvp_jin_sys_jobs.git')
                }
                branch('*/${branch}')
                extensions {
                    relativeTargetDirectory('mvp_jin_sys_jobs')
                }
            }
        }
        steps {
            shell('rm -rf packaged_build && mkdir -p packaged_build')
            shell('tar -czvf packaged_build/jincli.tar.gz mvp_jin_sys_jobs/ci')
            shell('echo $(env) > packaged_build/env.txt')
            shell('echo ${BUILD_URL} > packaged_build/build_url.txt')
            shell('echo HELLO')
        }

        publishers {
            archiveArtifacts {
                pattern('packaged_build/**/*')
                onlyIfSuccessful()
            }
            postBuildScripts {
                steps {
                    shell('echo post build')
                }
                onlyIfBuildSucceeds(false)
            }
        }
    }
    return the_job
}

folder('sysjin')
job_healthcheck(this, 'sysjin/HealthCheck')

