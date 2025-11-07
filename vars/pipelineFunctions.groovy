

// Checkout
def checkoutGit(String branch, String url) {
    echo "Pulling latest code from ${branch} -> ${url}"
    git branch: branch, url: url
}

// Maven build
def mvnBuild() {
    echo "Maven clean install"
    sh 'mvn clean install'
}

// Tests
def mvnTest() {
    echo "Running unit tests"
    sh 'mvn test'
}

// JaCoCo
def mvnJacoco() {
    echo "Generating JaCoCo report"
    sh 'mvn jacoco:report'
}

def mvnSonar() {
    echo "Running SonarQube analysis (token from Jenkins credential SONAR_TOKEN)"
    withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
        sh 'mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN'
    }
}


def mvnDeploy() {
    echo "Deploying artifacts to Nexus (credential id: NEXUS_CRED)"
    withCredentials([usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
        sh 'mvn deploy -DskipTests -Dnexus.username=$NEXUS_USER -Dnexus.password=$NEXUS_PASS'
    }
}


def dockerBuild(String imageName) {
    echo "Building Docker image ${imageName}"
    withCredentials([usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
        sh """
            docker build \
            --build-arg NEXUS_USERNAME=$NEXUS_USER \
            --build-arg NEXUS_PASSWORD=$NEXUS_PASS \
            -t ${imageName} docker/
        """
    }
}


def dockerPush(String imageName) {
    echo "Pushing Docker image ${imageName}"
    withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_CRED', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
        sh """
            docker login -u $DOCKER_USER -p $DOCKER_PASS
            docker push ${imageName}
        """
    }
}

// Docker-compose up
def dockerComposeUp() {
    echo "Starting docker-compose (docker/docker-compose.yml)"
    sh 'docker-compose -f docker/docker-compose.yml up -d'
}

// Trivy scan
def trivyScan() {
    echo "Running Trivy filesystem scan -> trivy/trivy-report.json"
    sh 'trivy fs --format json -o trivy/trivy-report.json .'
}


    def status = currentBuild.result ?: 'UNKNOWN'
    emailext(
            subject: "Projet Foyer Pipeline Build ${status}",
            body: """
            <html><body>
              <h3>Projet Foyer - Pipeline ${status}</h3>
              <p>Job: ${env.JOB_NAME}</p>
              <p>Build: ${env.BUILD_NUMBER}</p>
              <p><a href='${env.BUILD_URL}'>Console output</a></p>
            </body></html>
        """,

            mimeType: 'text/html',
            attachmentsPattern: 'trivy/trivy-report.json'
    )
}
