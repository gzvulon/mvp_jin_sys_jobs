package lib

// import Esl

// http://engineering.curalate.com/2016/09/29/programmatic-jenkins-jobs.html
import org.yaml.snakeyaml.Yaml
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class EslCtx {

    DslFactory jdsl_ctx
    Map jdsl_env
    PrintStream out
    String CRON_ONCE_IT_TWO_MINUTES = 'H/2 * * * *'

    EslCtx(DslFactory jdsl_ctx, out, jdsl_env){
        this.jdsl_ctx = jdsl_ctx
        this.out = out
        this.jdsl_env = jdsl_env
    }

    def createFoldersForJob(job_name){
        def tokens = job_name.trim().tokenize('/')
        if (tokens.size() > 1) {
            for(def i = 0; i < tokens.size() - 1 ; ++i){
                def dir_name = tokens[0..i].join('/')
                println "esl creating folder: ${dir_name}"
                jdsl_ctx.folder(dir_name)
            }
        }        
    }

    Job createPipe(Map map = [:], job_name, pipe_name){
        def jin_branch = map.jin_branch ?: jdsl_env['DEFAULT_JIN_BRANCH']
        def job_src = map.job_src ?: false
        def user_params = map.user_params ?: []
        def env_params = map.env_params ?: []
        
        def max_per_node = map.max_per_node ?: 1
        def max_total = map.max_total ?: 0

        def trigger_scm = map.trigger_scm ?: false
        def triggered_by = map.triggered_by ?: false
        def cron_cfg = map.cron_cfg ?: false

        def pipeline_path = "ci/pipelines/${pipe_name}"

        def the_description = Esl.createReachDescription(job_name,
            jdsl_env,
            job_src: job_src,
            pipeline_path: pipeline_path)

        createFoldersForJob(job_name)

        def the_job = jdsl_ctx.pipelineJob(job_name)
        the_job.with {
            description(the_description)
            parameters{
                stringParam('jin_branch', jin_branch)
                for(p in user_params){
                    stringParam(p.key, p.value)
                }
            }
            quietPeriod(2)
            Esl.scmJinPipeDefinition(delegate, pipeline_path, '${jin_branch}')
            throttleConcurrentBuilds {
                maxPerNode(max_per_node)
                maxTotal(max_total)
            }            
        }
        the_job.with {
            triggers {
                if (trigger_scm){
                    scm(CRON_ONCE_IT_TWO_MINUTES)
                }
                if(triggered_by){
                    upstream(triggered_by, 'SUCCESS')
                }
                if(cron_cfg){
                    cron(cron_cfg)
                }
            }
        }
        return the_job
    }

    Job createTask(Map map = [:], job_name, task_name){
        return createPipe(map, job_name, 'TaskBuilder.pipe.groovy')
    }

    List<Job> importPipes(Map map = [:], filename) {
        out.println "processing yaml: ${filename}"
        if ( Esl.shouldSkipYml(filename, jdsl_env['DSL_YML'])){
            out.println "skip {filename}: (not match ${jdsl_env.DSL_YML})"
            return [];
        }
        def filepath = "./resources/org/mvp_jin/configs/jobs/${filename}"
        def content = jdsl_ctx.readFileFromWorkspace(filepath)
        def yaml = new Yaml()
        def data = yaml.load(content)
        def job_list = data.jobs
        return importJobList(job_list, filepath)
    }

    List<Job> importJobList(job_list, filepath) {
        def jobs = []
        for (job in job_list){
            out.println "processing job: ${job.name}"
            def the_job = createPipe(job.name, job.pipeline,
                user_params: job?.params?.vars,
                env_params: job?.params?.consts,
                job_src: filepath,
                trigger_scm: job?.trigger_scm,
                triggered_by: job?.triggered_by,
                cron_cfg: job?.cron_cfg)
            jobs.add(the_job)
        }
        return jobs
    }
}
