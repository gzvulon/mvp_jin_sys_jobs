
import lib.Esl

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

