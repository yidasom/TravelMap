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
                    // 모든 캐시를 지우고 다시 빌드
                    sh './gradlew clean build'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('backend') {
                    // 고유한 태그와 'latest' 태그, 두 개의 태그로 이미지를 빌드합니다.
                    sh "docker build -t ${IMAGE_NAME} -t ${IMAGE_LATEST_NAME} ."
                }
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
                // Secret을 먼저 배포 🔑
                sh 'kubectl apply -f /home/jenkins/k8s/secret.yaml'
                // DB 관련 파일 먼저 배포
                sh 'kubectl apply -f k8s/db/new-postgres-pv.yaml'
                sh 'kubectl apply -f k8s/db/new-postgres-pvc.yaml'
                sh 'kubectl apply -f k8s/db/postgres-deployment.yaml'

                // PostgreSQL 배포가 완료될 때까지 대기
                sh 'kubectl rollout status deployment/postgres'

                // `k8s/deployment.yaml` 파일의 이미지 태그를 최신 빌드 태그로 수정합니다.
                sh "sed -i 's|travelmap:latest|travelmap:${IMAGE_TAG}|g' k8s/deployment.yaml"

                // 수정된 Deployment를 배포합니다.
                sh 'kubectl apply -f k8s/deployment.yaml'

                // Deployment가 완료될 때까지 대기
                sh 'kubectl rollout status deployment/travelmap-deployment'

                // Service 배포
                sh 'kubectl apply -f k8s/service.yaml'
            }
        }
    }
}
