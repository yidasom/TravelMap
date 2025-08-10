pipeline {
    agent any

    environment {
        IMAGE_NAME = "somlh1212/travelmap:latest"
    }

    stages {
        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh './gradlew clean build'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t $IMAGE_NAME -f backend/Dockerfile ."
            }
        }

        stage('Push Docker Image') {
            steps {
//                 sh "echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin"
                sh "docker push $IMAGE_NAME"
            }
        }

        stage('Deploy to K8s') {
            steps {
                sh 'kubectl apply -f k8s/deployment.yaml'
                sh 'kubectl apply -f k8s/service.yaml'
                // DB 관련 파일들도 배포 로직에 포함
                sh 'kubectl apply -f k8s/db/postgres-pv.yaml'
                sh 'kubectl apply -f k8s/db/postgres-pvc.yaml'
                sh 'kubectl apply -f k8s/db/postgres-deployment.yaml'
            }
        }
    }
}