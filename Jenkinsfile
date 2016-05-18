#!groovy

properties(
  [ [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', daysToKeepStr: '30'] ]
  , [$class: 'GithubProjectProperty', projectUrlStr: 'http://github.com/lstephen/ootp-ai']
  ])

def construi(target) {
  wrap([$class: 'AnsiColorBuildWrapper', colorMapName: 'xterm']) {
    sh "construi ${target}"
  }
}

def construi_on_node(target) {
  node('construi') {
    checkout scm
    construi target
  }
}

stage 'Build'
construi_on_node 'build'

if (env.OOTPAI_SITE != null) {
  stage 'Run'
  node('construi') {
    checkout scm
    currentBuild.description = "Run ${env.OOTPAI_SITE}"

    withCredentials([
      [ $class: 'FileBinding'
        , variable: 'GIT_SSH_KEY'
        , credentialsId: 'cfbecb37-737f-4597-86f7-43fb2d3322cc' ]
      ]) {
        construi 'run'
    }
  }
}

