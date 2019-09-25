#!/usr/bin/env groovy

// https://jenkins.io/doc/book/pipeline/jenkinsfile/


//def sbtToolCommand = """"${tool name: 'sbt-1.2.8', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt"""
def sbtToolCommand = 'sbt'
def sbtExtraCommand = './sbt'

def nexusCredentialsId = '4cbe1d13-fccb-4389-b3ce-d237bd86e758'
def nexusCredentialsPath = './nexus-credentials'
def sbtCommand = sbtExtraCommand
def sbtOptions = "\
  -no-colors -batch \
  -Dsbt.ivy.home=./.ivy2/ \
  -Dsbt.repository.config=./repositories-sigma \
  -Dsbt.boot.credentials=$nexusCredentialsPath \
  -Dsbt.override.build.repos=true \
  -sbt-launch-repo http://nexus.sigma.sbrf.ru:8099/nexus/content/repositories/ivy-releases_proxy \
"


pipeline {
    agent any
    parameters {
        booleanParam defaultValue: true,  description: 'clean', name: 'DO_CLEAN'
        booleanParam defaultValue: false, description: 'package', name: 'DO_PACKAGE'
        booleanParam defaultValue: false, description: 'publish to nexus', name: 'DO_PUBLISH'
    }
//    tools {
//        sbt 'sbt-1.2.6'
//    }
    stages {
        stage('Clean') {
            when {
              beforeAgent true
              expression { params.DO_CLEAN }
            }
            steps {
                configFileProvider([configFile(fileId: nexusCredentialsId, targetLocation: nexusCredentialsPath)]) {
                  //sh "$sbtCommand $sbtOptions clean"
                }
            }
        }
        stage('Build') {
            steps {
                configFileProvider([configFile(fileId: nexusCredentialsId, targetLocation: nexusCredentialsPath)]) {
                  sh "$sbtCommand $sbtOptions compile"
                }
            }
        }
        stage('Test') {
            steps {
                configFileProvider([configFile(fileId: nexusCredentialsId, targetLocation: nexusCredentialsPath)]) {
                  sh "$sbtCommand $sbtOptions test"
                }
            }
        }
        stage('Integration Test') {
            steps {
                configFileProvider([configFile(fileId: nexusCredentialsId, targetLocation: nexusCredentialsPath)]) {
                  sh "$sbtCommand $sbtOptions it:test"
                }
            }
        }
        stage('Package') {
            when {
              beforeAgent true
              expression { params.DO_PACKAGE }
            }
            steps {
                configFileProvider([configFile(fileId: nexusCredentialsId, targetLocation: nexusCredentialsPath)]) {
                  sh "$sbtCommand $sbtOptions assembly"
                }
            }
        }
        stage('Deploy') {
            when {
              beforeAgent true
              expression { params.DO_PUBLISH }
            }
            steps {
                configFileProvider([configFile(fileId: nexusCredentialsId, targetLocation: nexusCredentialsPath)]) {
                  sh "$sbtCommand $sbtOptions tfs-pipeline/publish"
                }
            }
        }
    }
    post {
        always {
            //scanForIssues tool: scala(reportEncoding: 'UTF-8')
            //publishIssues id: 'scala', ignoreQualityGate: true, issues: [], name: 'Scala Compiler', sourceCodeEncoding: 'UTF-8'
            junit allowEmptyResults: true, testResults: '**/target/test-reports/*.xml'
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'kafka/target/test-reports/html/', reportFiles: 'index.html', reportName: 'kafka test', reportTitles: ''])
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'service/target/test-reports/html', reportFiles: 'index.html', reportName: 'service test', reportTitles: ''])
        }
    }
}
