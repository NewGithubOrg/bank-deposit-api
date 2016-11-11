// Jenkinsfile
def buildVersion = null
def short_commit = null
echo "Building ${env.BRANCH_NAME}"
properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5']]])
stage 'Build'
node('docker-cloud') {
    checkout scm
    sh('git rev-parse HEAD > GIT_COMMIT')
    git_commit=readFile('GIT_COMMIT')
    short_commit=git_commit.take(7)
    docker.image('kmadel/maven:3.3.3-jdk-8').inside('-v /data:/data') {
        sh "mvn -Dmaven.repo.local=/data/mvn/repo -DGIT_COMMIT='${short_commit}' -DBUILD_NUMBER=${env.BUILD_NUMBER} -DBUILD_URL=${env.BUILD_URL} clean package"
    }
    stash name: 'pom', includes: 'pom.xml'
    stash name: 'jar-dockerfile', includes: '**/target/*.jar,**/target/Dockerfile'
}

if(!env.BRANCH_NAME.startsWith("PR")){
  checkpoint 'Build Complete'
  stage 'DevTest'
  node('docker-cloud') {
    try {
    unstash 'pom'
    //test in parallel
    parallel(
        integrationTests: {
            docker.image('kmadel/maven:3.3.3-jdk-8').inside('-v /data:/data') {
                sh 'mvn -Dmaven.repo.local=/data/mvn/repo verify'
            }
        }, sonarAnalysis: {
            echo "sonar tests done"
        }, failFast: true
    )
    } catch (x) {
      currentBuild.result = "failed"
      step([$class: 'GitHubCommitStatusSetter', contextSource: [$class: 'ManuallyEnteredCommitContextSource', context: 'Jenkins']])
      //hipchatSend color: 'RED', message: "${env.JOB_NAME} ${env.BUILD_NUMBER} status: ${currentBuild.result} <a href=\'${env.BUILD_URL}\'>Open</a>", room: '123', server: 'xyz.hipchat.com', token: 'A6YX8LxNc4wuNiWUn6qHacfO1bBSGXQ6E1lELi1z', v2enabled: true
      mail body: "Job '${env.JOB_NAME}' has failed.  See <a href=\"${env.BUILD_URL}\">logs</a> for details.", mimeType: 'text/html', subject: "${env.JOB_NAME} FAILURE", to: 'kbhatia@cloudbees.com'
      throw x
    }
  }
}

if(env.BRANCH_NAME=="master"){
  checkpoint 'DevTest Complete'
  def dockerTag = "${env.BUILD_NUMBER}-${short_commit}"
  stage name: 'Version Release', concurrency: 1
  node('docker-cloud') {
    unstash 'pom'

    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    if (matcher) {
        buildVersion = matcher[0][1]
        echo "Release version: ${buildVersion}"
    }
    matcher = null

    stage 'Build Docker Image'
    def mobileDepositApiImage
    //unstash Spring Boot JAR and Dockerfile
    unstash 'jar-dockerfile'
    dir('target') {
        mobileDepositApiImage = docker.build "kishorebhatia/bank-deposit-api:${dockerTag}"
    }

    stage 'Publish Docker Image'
    sh "docker -v"
    //use withDockerRegistry to make sure we are logged in to docker hub registry
    withDockerRegistry(registry: [credentialsId: 'kb-docker-hub']) {
      mobileDepositApiImage.push()
    }
  }
  //set checkpoint before deployment
  checkpoint 'Build Complete'
    stage 'Deploy to QA'
      //Deploy to docker cloud using api
      echo "QA Successful"

  input message: "Deploy to PROD?"
  checkpoint 'Before Production'

    stage 'Deploy to PROD'
    //Deploy to docker cloud using api
    echo "PROD deployment completed"
}

node('docker-cloud') {
  //send commit status to GitHub
  step([$class: 'GitHubCommitStatusSetter', contextSource: [$class: 'ManuallyEnteredCommitContextSource', context: 'Jenkins'], statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'BetterThanOrEqualBuildResult', message: 'Pipeline completed successfully', result: 'SUCCESS', state: 'SUCCESS']]]])
  //update hipchat with success
  currentBuild.result = "success"
  //hipchatSend color: 'GREEN', message: "${env.JOB_NAME} ${env.BUILD_NUMBER} status: ${currentBuild.result} <a href=\'${env.BUILD_URL}\'>Open</a>", room: '123', server: 'xyz.hipchat.com', token: 'A6YX8LxNc4wuNiWUn6qHacfO1bBSGXQ6E1lELi1z', v2enabled: true
}
