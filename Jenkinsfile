pipeline {
    agent any

    environment {
        // 빌드 번호를 포함한 고유한 이미지 태그를 생성합니다.
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        IMAGE_NAME = "somlh1212/travelmap:${IMAGE_TAG}"
        IMAGE_LATEST_NAME = "somlh1212/travelmap:latest"
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
                // 고유한 태그와 'latest' 태그, 두 개의 태그로 이미지를 빌드합니다.
                sh "docker build -t ${IMAGE_NAME} -t ${IMAGE_LATEST_NAME} -f backend/Dockerfile ."
            }
        }

        stage('Push Docker Image') {
            steps {
                // 두 개의 이미지를 모두 Docker Hub에 푸시합니다.
                sh "docker push ${IMAGE_NAME}"
                sh "docker push ${IMAGE_LATEST_NAME}"
            }
        }

        stage('Deploy to K8s') {
            steps {
                // DB 관련 파일 먼저 배포
                sh 'kubectl apply -f k8s/db/postgres-pv.yaml'
                sh 'kubectl apply -f k8s/db/postgres-pvc.yaml'
                sh 'kubectl apply -f k8s/db/postgres-deployment.yaml'

                // PostgreSQL 배포가 완료될 때까지 기다립니다.
                sh 'kubectl rollout status deployment/postgres'

                // PostgreSQL이 준비된 후에 애플리케이션을 배포합니다.
                // 이전에 'kubectl apply'를 사용했으나, 이제는 'kubectl set image'를 사용해
                // 이미지 태그를 직접 업데이트하여 강제로 재배포를 유도합니다.
                sh "kubectl set image deployment/travelmap-deployment travelmap=${IMAGE_NAME}"
                sh 'kubectl rollout status deployment/travelmap-deployment'
                sh 'kubectl apply -f k8s/service.yaml'
            }
        }
    }
}