@Library('projet_foyer') _

pipeline {
    agent any

    environment {
        IMAGE_NAME = 'foyer-crud-app:1.0.0'
    }

    stages {
        stage('Checkout') {
            steps {
                script {

                    pipelineFoyer.checkoutGit('main', 'https://github.com/ranim-ahmadi/Projet-foyer-crud.git')
                }
            }
        }
        stage('Maven Build')      { steps { script { pipelineFoyer.mvnBuild() } } }
        stage('JUnit Tests')      { steps { script { pipelineFoyer.mvnTest() } } }
        stage('JaCoCo')           { steps { script { pipelineFoyer.mvnJacoco() } } }
        stage('SonarQube')        { steps { script { pipelineFoyer.mvnSonar() } } }
        stage('Deploy to Nexus')  { steps { script { pipelineFoyer.mvnDeploy() } } }
        stage('Docker Build')     { steps { script { pipelineFoyer.dockerBuild(env.IMAGE_NAME) } } }
        stage('Docker Push')      { steps { script { pipelineFoyer.dockerPush(env.IMAGE_NAME) } } }
        stage('Docker Compose')   { steps { script { pipelineFoyer.dockerComposeUp() } } }
        stage('Trivy Scan')       { steps { script { pipelineFoyer.trivyScan() } } }
    }

    post {
        always { script { pipelineFoyer.sendEmail() } }
    }
}
