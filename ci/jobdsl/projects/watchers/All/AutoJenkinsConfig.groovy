def active_branch = 'master'
def github_username = 'gzvulon'
def github_project = 'mvp_jin_sys_jobs'

job("config/AutoJenkinsConfig"){
    description('Jin jobs config watcher')
    label('master')
    quietPeriod(2)
    triggers {
        scm('* * * * *')
    }
    throttleConcurrentBuilds {
        maxPerNode(1)
        maxTotal(1)
    }
    scm {
        git {
            remote {
                url("https://github.com/${github_username}/${github_project}.git")
            }
            branch(active_branch)
        }
    }
    steps {
        shell('ls')
        downstreamParameterized {
            trigger('config/JenkinsConfig')
        }
    }
}
