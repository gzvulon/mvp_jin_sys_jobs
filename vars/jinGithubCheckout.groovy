// jinGithubCheckout.groovy
def call(Map map = [:], String name) {
    def commit_branch = map.commit_branch ?: 'refs/heads/master'
    def target_dir = map.target_dir ?: name
    def user = 'gzvulon'
    def target_url = "https://github.com/${user}/${name}"

    def shallow = map.shallow ?: false
    def depth = map.depth ?: 0
    def clean = map.clean ?: false
    def do_poll = map.do_poll ?: true
    println "--== checkout will clean: ${clean}"

    def extensions = [
        [$class: 'RelativeTargetDirectory', relativeTargetDir: target_dir],
        // [$class: 'GitLFSPull'],
        [$class: 'CheckoutOption', timeout: 500],
        [$class: 'CloneOption', timeout: 500, depth: depth, shallow: shallow],
        [$class: 'AuthorInChangelog']
    ]
    
    if (!do_poll){
        // checkout(...,poll:false,...) does not work JENKINS-36195
        extensions.addAll([
            [$class: 'DisableRemotePoll'],
            [$class: 'PathRestriction', 
                excludedRegions: '*', includedRegions: '']
        ])
    }

    checkout(poll: do_poll, scm:[$class: 'GitSCM',
        poll: do_poll,
        branches: [[name: commit_branch]],
        doGenerateSubmoduleConfigurations: false,
        extensions: extensions,
        submoduleCfg: [],
        userRemoteConfigs: [[url: target_url]]
    ])

    if (clean) {
        sh "sudo git -C ${target_dir} clean -fdx > git_clean.log"
    }
}
