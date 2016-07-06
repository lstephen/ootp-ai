#!groovy

properties(
  [ [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', daysToKeepStr: '30'] ]
  , [$class: 'GithubProjectProperty', projectUrlStr: 'http://github.com/lstephen/ootp-ai']
  , [$class: 'ParametersDefinitionProperty',
      parameterDefinitions:
      [ [$class: 'ChoiceParameterDefinition', name: 'OOTPAI_SITE', choices: "NONE\nBTHUSTLE\nCBL\nHFTC\nLBB\nGABL\nSAVOY\nTWML"]
      , [$class: 'BooleanParameterDefinition', name: 'OOTPAI_CLEAR_CACHE', defaultValue: false]
      , [$class: 'BooleanParameterDefinition', name: 'OOTPAI_PLAYOFFS', defaultValue: false]
      , [$class: 'BooleanParameterDefinition', name: 'URL_TRIGGER', defaultValue: false]
      ]
    ]
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

stage 'Wait'
if (URL_TRIGGER && URL_TRIGGER == 'true') {
  sleep time: 1, unit: 'HOURS'
}

stage 'Build'
construi_on_node 'build'

stage 'Run'
if (env.BRANCH_NAME == 'master' && OOTPAI_SITE != 'NONE') {
  node('construi') {
    checkout scm
    currentBuild.description = "Run ${OOTPAI_SITE}"

    withCredentials([
      [ $class: 'FileBinding'
        , variable: 'GIT_SSH_KEY'
        , credentialsId: 'cfbecb37-737f-4597-86f7-43fb2d3322cc' ]
      ]) {
      withEnv(
        [ "OOTPAI_SITE=${OOTPAI_SITE}"
        , "OOTPAI_PLAYOFFS=${OOTPAI_PLAYOFFS}"
        , "OOTPAI_CLEAR_CACHE=${OOTPAI_CLEAR_CACHE}"
        ]) {
        construi 'run'
      }
    }
  }
}

