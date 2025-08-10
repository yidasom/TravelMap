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
                // DB 관련 파일 먼저 배포
                sh 'kubectl apply -f k8s/db/postgres-pv.yaml'
                sh 'kubectl apply -f k8s/db/postgres-pvc.yaml'
                sh 'kubectl apply -f k8s/db/postgres-deployment.yaml'

                // 이 부분이 중요합니다. PostgreSQL 배포가 완료될 때까지 기다립니다.
                sh 'kubectl rollout status deployment/postgres'

                // PostgreSQL이 준비된 후에 애플리케이션을 배포합니다.
                sh 'kubectl apply -f k8s/deployment.yaml'
                sh 'kubectl apply -f k8s/service.yaml'
            }
        }
    }
}