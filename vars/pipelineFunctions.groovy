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

// SonarQube analysis
def mvnSonar() {
    echo "Running SonarQube analysis (token from Jenkins credential SONAR_TOKEN)"
    withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
        sh 'mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN'
    }
}

// Deploy to Nexus
def mvnDeploy() {
    echo "Deploying artifacts to Nexus (credential id: NEXUS_CRED)"
    withCredentials([usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
        sh 'mvn deploy -DskipTests -Dnexus.username=$NEXUS_USER -Dnexus.password=$NEXUS_PASS'
    }
}

// Docker build (utilise le nom du repo GitHub)
def dockerBuild() {
    def imageName = "ranimahmadi/projet-foyer-crud:${env.BUILD_NUMBER}"
    echo "Building Docker image ${imageName}"
    withCredentials([usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
        sh """
            docker build \
            --build-arg NEXUS_USERNAME=\$NEXUS_USER \
            --build-arg NEXUS_PASSWORD=\$NEXUS_PASS \
            -t ${imageName} \
            -t ranimahmadi/projet-foyer-crud:latest \
            docker/
        """
    }
}

// Docker push
def dockerPush() {
    def imageName = "ranimahmadi/projet-foyer-crud"
    echo "Pushing Docker image ${imageName}"
    withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_CRED', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
        sh """
            echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
            docker push ${imageName}:${env.BUILD_NUMBER}
            docker push ${imageName}:latest
        """
    }
}

// Docker-compose up
def dockerComposeUp() {
    echo "Starting docker-compose with credentials"
    withCredentials([string(credentialsId: 'MYSQL_ROOT_PASSWORD', variable: 'MYSQL_ROOT_PASSWORD')]) {
        sh '''
            export MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD
            docker-compose -f docker/docker-compose.yml up -d
        '''
    }
}

// Trivy scan
def trivyScan() {
    echo "Running Trivy filesystem scan -> trivy/trivy-report.json"
    sh 'mkdir -p trivy && trivy fs --format json -o trivy/trivy-report.json .'
}

// Send email notification
def sendEmail() {
    def status = currentBuild.result ?: 'SUCCESS'
    emailext(
            subject: "Projet Foyer Pipeline Build ${status} - Build #${env.BUILD_NUMBER}",
            body: """
        <html><body>
          <h3>Projet Foyer - Pipeline ${status}</h3>
          <p><strong>Job:</strong> ${env.JOB_NAME}</p>
          <p><strong>Build:</strong> ${env.BUILD_NUMBER}</p>
          <p><strong>Status:</strong> ${status}</p>
          <p><strong>Repository:</strong> https://github.com/ranim-ahmadi/Projet-foyer-crud.git</p>
          <p><a href='${env.BUILD_URL}'>Voir Console Output</a></p>
          <p><a href='${env.BUILD_URL}testReport'>Voir Test Results</a></p>
        </body></html>
        """,
            mimeType: 'text/html',
            to: 'ahmadiranim.pro@gmail.com',
            attachmentsPattern: 'trivy/trivy-report.json'
    )
}