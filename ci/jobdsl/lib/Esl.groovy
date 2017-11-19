package lib

// http://engineering.curalate.com/2016/09/29/programmatic-jenkins-jobs.html
import org.yaml.snakeyaml.Yaml

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job


class Esl {
    static boolean shouldSkipJob(job_name, dsl_job){
        if (!dsl_job){
            return false
        } else if (dsl_job && dsl_job == '*'){
            return false
        } else if (dsl_job && dsl_job ==~ job_name){
            return false
        }
        return true
    }

    static boolean shouldSkipYml(filename, dsl_yml){
        if (!dsl_yml){
            return false
        } else if (dsl_yml && dsl_yml == '*'){
            return false
        } else if (dsl_yml && dsl_yml ==~ filename){
            return false
        }
        return true
    }

    static def get_branch_for_version(version_name){
        if (version_name == 'master') {
            return version_name
        }
        return "release/${version_name}" + ''
    }    

    static getJinGithubLink(commit_branch, rel_path){
        def github_base = "https://github.com/gzvulon/mvp_jin_sys_jobs/blob"
        def github_link = "${github_base}/${commit_branch}/${rel_path}"
        def html_link = """
            <a href="${github_link}" target="_blank" > 
                ${rel_path}
            </a>
        """
        return html_link
    }
 
    static relFileName(base, filename){
        def root = new File(base)
        def full = new File(filename)
        def rel_path = root.toPath().relativize( 
            full.toPath() ).toFile()
        return rel_path
    }

    static listToTable(tbl){
        def trs = []
        for(def i = 0; i < tbl.size(); ++i){
            def row = tbl[i]
            def tds = []
            for(def j = 0; j < row.size(); ++j){
                def cell = row[j]
                def td = "<td> ${cell} </td>"
                tds.add(td)
            }
            def line = tds.join('')
            def tr ="<tr> ${line} </tr>"
            trs.add(tr)
        }
        def rows = trs.join('\n')
        def table = """<table><tbody>\n${rows}\n</tbody></table>"""
        return table
    }

    static createReachDescription(Map map = [:], job_name, Map env_vars) {
        def descr = map.descr ?: ''
        def pipeline_path = map.pipeline_path ?: null
        
        def file_name = ''
        if (map.job_src){
            file_name = map.job_src
        } else {
            file_name = relFileName("${env_vars.WORKSPACE}", 
                                    "${env_vars.__FILE__}")
        }

        def html_link_jdsl = getJinGithubLink(
            "${env_vars.GIT_COMMIT}", file_name)
        
        def jin_trigger = "../jin/tools/jin -s ${env_vars.JENKINS_SERVER} " +
                        "run-job ${job_name}"
        def tbl = [
            ['created by', 
             """<a href="${env_vars.JOB_NAME}">${env_vars.JOB_NAME}<a> """],
            ['branch', "${env_vars.GIT_BRANCH}"],
            ['commit', "${html_link_jdsl}"],
            ['cli ', "<code> ${jin_trigger} </code>"]
        ]
        
        if (pipeline_path){
            def html_link_pipe = getJinGithubLink(
                "${env_vars.DEFAULT_JIN_BRANCH}", pipeline_path)            
            tbl.add(['running pipeline', "${html_link_pipe}"])
        }
        def table = listToTable(tbl)
        
        def the_description = """${descr}\n<br>${table}"""
        return the_description
    }

    static scmJinPipeDefinition(ctx, pipeline_path, the_branch){
        def jin_dir =  'sysjin'
        def jin_url =  'https://github.com/gzvulon/mvp_jin_sys_jobs.git'
        def script_path = "${jin_dir}/${pipeline_path}"
        ctx.with {
            definition {
                cpsScm {
                    scm {
                        git {
                            remote {
                                url(jin_url)
                            }
                            extensions {
                                relativeTargetDirectory(jin_dir)
                            }
                            branch(the_branch)
                        }
                    }
                    scriptPath(script_path)
                }
            }
        }
    }

    static scmJinCheckout(Map map = [:], ctx, name){
        def commit_branch = map.commit_branch ?: 'master'
        def target_dir = map.target_dir ?: name
        def target_url = "https://github.com/gzvulon/${name}.git"
        def do_shallow = map.do_shallow ?: false
        ctx.with {
            scm {
                git {
                    remote {
                        url(target_url)
                    }
                    branch("refs/heads/${commit_branch}")
                    extensions {
                        relativeTargetDirectory(target_dir)
                        cloneOptions {
                            timeout(500)
                            if(do_shallow){
                                shallow()
                            }
                        }
                        checkoutOption {
                            timeout(120)
                        }
                    }
                }
            }
        }
    }

    static String get_branch_for_version(String version_name){
        if (version_name == 'master') {
            return version_name
        }
        return "release/${version_name}" + ''
    }


    static getDefaultDeployRepo(){
        return 'jenkins-tests'
    }

    static getRealRepo(repo, deploy_env){
        if (deploy_env != 'production' && deploy_env != 'prod') {
            return 'stage-' + repo
        } else {
            return repo
        }
    }

    static addUploadToArtifactory(Map map = [:],
        context, deploy_env, pattern, target)
    {
        def repo = getRealRepo(map.repo ?: getDefaultDeployRepo(), deploy_env)
        def release_repo = getRealRepo('jenkins-final', deploy_env)

        // Create the upload spec.
        def real_target = "${repo}/${target}"
        def upload_spec = """{
            "files": [
                    {
                        "pattern": "${pattern}",
                        "target": "${real_target}",
                        "flat" : "false"
                    }
                ]
            }"""

        context.with {
            wrappers {
                artifactoryGenericConfigurator {
                    details {
                        artifactoryName('jinhub')
                        artifactoryUrl('http://jinhub/artifactory')
                        deployReleaseRepository(null)
                        deploySnapshotRepository(null)
                        resolveReleaseRepository(null)
                        resolveSnapshotRepository(null)
                        userPluginKey(null)
                        userPluginParams(null)
                    }

                    asyncBuildRetention(false)
                    deployBuildInfo(true)
                    includeEnvVars(true)
                    envVarsPatterns {
                        includePatterns('*')
                        excludePatterns('*PASSWORD*,*password*,*secret*,*key*')
                    }
                    useSpecs(true)
                    uploadSpec {
                        spec(upload_spec)
                        filePath(null)
                    }
                    downloadSpec {
                        spec('')
                        filePath(null)
                    }

                    discardOldBuilds(false)
                    discardBuildArtifacts(false)
                    multiConfProject(false)
                    overrideBuildName(false)

                    resolverDetails(null)
                    deployerCredentialsConfig(null)
                    resolverCredentialsConfig(null)
                    deployPattern('')
                    resolvePattern('')
                    matrixParams('')
                    artifactoryCombinationFilter('')
                    customBuildName(null)
                }
            }
        }
    }


    static addDownloadFromArtifactory(Map map = [:],
        context, deploy_env, pattern, base_path)
    {
        def repo = getRealRepo(map.repo ?: getDefaultDeployRepo(), deploy_env)

        // Create the upload spec.
        def real_pattern = "${repo}/${base_path}/${pattern}"        
        def download_spec = """{
            "files": [
                    {
                        "pattern": "${real_pattern}",
                        "flat" : "false"
                    }
                ]
            }"""

        context.with {
            wrappers {
                artifactoryGenericConfigurator {
                    details {
                        artifactoryName('jinhub')
                        artifactoryUrl('http://jinhub/artifactory')

                        deployReleaseRepository(null)
                        deploySnapshotRepository(null)
                        resolveReleaseRepository(null)
                        resolveSnapshotRepository(null)
                        userPluginKey(null)
                        userPluginParams(null)
                    }

                    deployBuildInfo(true)
                    includeEnvVars(true)
                    envVarsPatterns {
                        includePatterns('*')
                        excludePatterns('*PASSWORD*,*password*,*secret*,*key*')
                    }                
                    useSpecs(true)
                    uploadSpec {
                        spec('')
                        filePath(null)
                    }
                    downloadSpec {
                        spec(download_spec)
                        filePath(null)
                    }

                    discardOldBuilds(false)
                    discardBuildArtifacts(false)
                    multiConfProject(false)
                    overrideBuildName(false)

                    resolverDetails(null)
                    deployerCredentialsConfig(null)
                    resolverCredentialsConfig(null)
                    deployPattern('')
                    resolvePattern('')
                    matrixParams('')
                    artifactoryCombinationFilter('')
                    customBuildName(null)
                    
                    asyncBuildRetention(false)
                }
            }
        }
    }

}
