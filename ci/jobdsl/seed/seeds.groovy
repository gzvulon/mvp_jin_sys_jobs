def slurper = new ConfigSlurper()
// fix classloader problem using ConfigSlurper in job dsl
slurper.classLoader = this.class.classLoader
def config = slurper.parse(readFileFromWorkspace(
    'ci/jobdsl/common/Config.groovy'))

def dslrepo = config.defaults.dslrepo
def dsl_branch = config.defaults.dsl_branch

// --- ext libs --
// http://engineering.curalate.com/2016/09/29/programmatic-jenkins-jobs.html
def ext_libs = 'ci/jobdsl/ext_libs'
def sh_update_ext_libs = """#!/bin/bash
mkdir -p  ${ext_libs} && cd ${ext_libs}
if [ ! -f snakeyaml-1.17.jar ]; then
    wget https://repo1.maven.org/maven2/org/yaml/snakeyaml/1.17/snakeyaml-1.17.jar
else
    echo `readlink -f snakeyaml-1.17.jar || true`
fi"""

// --- main --
folder('config'){
    description("jobs dsl configuration")
}

job("config/JenkinsConfig"){
    label('master')
    description('Custom Jobs seed jobs' +
                'This job is automagically generated.')
    wrappers {
        timestamps()
        buildName('#${BUILD_NUMBER}-#${GIT_BRANCH}-#${GIT_REVISION:0:7}-#${CHANGES}')
    }

    environmentVariables {
        env('DSL_JOB', '*')
        env('DSL_YML', '*')
        keepBuildVariables(true)
        keepSystemVariables(true)
    }
    blockOnUpstreamProjects()
    throttleConcurrentBuilds {
        maxPerNode(1)
        maxTotal(1)
    }    
    steps {
        scm {
            git {
                remote {
                    url("https://github.com/gzvulon/${dslrepo}")
                }
                branch(dsl_branch)
            }
        }
        shell(sh_update_ext_libs)
        shell('ci/jobdsl/lib/update_lib_links.sh')
        dsl {
            external("ci/jobdsl/projects/*/*/*.groovy")
            removeAction('DISABLE')
            additionalClasspath("${ext_libs}/*.jar")
        }
    }
}

job("config/JenkinsConfigCustom"){
    label('master')
    description('Custom Jobs seed jobs.' + 
                'This job is automagically generated.')
    wrappers {
        timestamps()
        buildName('#${BUILD_NUMBER}-#${GIT_BRANCH}-#${GIT_REVISION:0:7}-#${CHANGES}')
    }

    logRotator {
        daysToKeep(360)
        artifactDaysToKeep(360)
    }    
    parameters {
        stringParam('dsl_branch', "${dsl_branch}", 
            'dsl source branch')
        stringParam('script_dir', 'projects/sysjin/*', 
            'job dsl dir')
        stringParam('script_name', '*.groovy', 
            'job dsl pattern')
        stringParam('DSL_JOB', 'sysjin/HealthCheck',
            'job fullname to update or * for all')
        stringParam('DSL_YML', 'infra.yml',
            'job fullname to update or * for all')
    }
    blockOnUpstreamProjects()
    steps {
        scm {
            git {
                remote {
                    url("https://github.com/gzvulon/${dslrepo}")
                }
                branch('$dsl_branch')
            }
        }

        shell(sh_update_ext_libs)
        shell('ci/jobdsl/lib/update_lib_links.sh')
        dsl {
            external('ci/jobdsl/${script_dir}/${script_name}')
            additionalClasspath("${ext_libs}/*.jar")
        }
    }
}
