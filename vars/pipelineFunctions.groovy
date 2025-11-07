

def checkoutGit(String branch, String url) {
    echo "Pulling latest code from ${branch}..."
    git branch: branch, url: url
}

def mvnBuild() {
    echo "Building Maven project..."
    sh 'mvn clean install'
}

def mvnTest() {
    echo "Running JUnit tests..."
    sh 'mvn test'
}

def mvnJacoco() {
    echo "Generating JaCoCo coverage report..."
    sh 'mvn jacoco:report'
}

def mvnSonar() {
    echo "Running SonarQube analysis..."
    withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
        sh 'mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN'
    }
}

def mvnDeploy() {
    echo "Deploying to Nexus..."
    withCredentials([usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
        sh 'mvn deploy -DskipTests -Dnexus.username=$NEXUS_USER -Dnexus.password=$NEXUS_PASS'
    }
}

def dockerBuild(String imageName) {
    echo "Building Docker image ${imageName}..."
    withCredentials([usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
        sh """
            docker build \
            --build-arg NEXUS_USERNAME=$NEXUS_USER \
            --build-arg NEXUS_PASSWORD=$NEXUS_PASS \
            -t ${imageName} .
        """
    }
}

def dockerPush(String imageName) {
    echo "Pushing Docker image ${imageName}..."
    withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_CRED', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
        sh """
            docker login -u $DOCKER_USER -p $DOCKER_PASS
            docker push ${imageName}
        """
    }
}

def dockerComposeUp() {
    echo "Running docker-compose up..."
    sh 'docker-compose up -d'
}

def trivyScan() {
    echo "Scanning file system with Trivy..."
    sh 'trivy fs --format json -o trivy/trivy-report.json .'
}
