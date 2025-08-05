pipeline {
    agent any

    environment {
        IMAGE_NAME = "yidasom/travelmap:latest"
    }

    stages {
//         stage('Checkout') {
//             steps {
//                 git 'https://github.com/yidasom/TravelMap.git'
//             }
//         }
        stage('Build JAR') {
            steps {
                dir('backend') {
                    sh './gradlew clean build'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                sh '''
                    docker build -t $IMAGE_NAME -f backend/Dockerfile .
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    docker push $IMAGE_NAME
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl apply -f k8s/deployment.yaml'
            }
        }
    }
}
