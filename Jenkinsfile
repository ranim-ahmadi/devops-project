@Library('jenkins-shared-library') _

pipeline {
    agent any
    environment {
        IMAGE_NAME = 'myapp:1.0.0'
    }
    stages {
        stage('Checkout Git') { steps { script { pipelineFunctions.checkoutGit('main', 'https://github.com/yourusername/devops-project.git') } } }
        stage('Maven Build') { steps { script { pipelineFunctions.mvnBuild() } } }
        stage('JUnit Tests') { steps { script { pipelineFunctions.mvnTest() } } }
        stage('JaCoCo Report') { steps { script { pipelineFunctions.mvnJacoco() } } }
        stage('SonarQube Analysis') { steps { script { pipelineFunctions.mvnSonar() } } }
        stage('Deploy to Nexus') { steps { script { pipelineFunctions.mvnDeploy() } } }
        stage('Build Docker Image') { steps { script { pipelineFunctions.dockerBuild(env.IMAGE_NAME) } } }
        stage('Push Docker Image') { steps { script { pipelineFunctions.dockerPush(env.IMAGE_NAME) } } }
        stage('Docker Compose Up') { steps { script { pipelineFunctions.dockerComposeUp() } } }
        stage('Trivy Scan') { steps { script { pipelineFunctions.trivyScan() } } }
    }
}
