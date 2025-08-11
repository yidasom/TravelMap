pipeline {
    agent any

    environment {
        // 백엔드 이미지 정보
        BACKEND_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BACKEND_IMAGE_NAME = "somlh1212/travelmap-backend:${BACKEND_IMAGE_TAG}"
        BACKEND_IMAGE_LATEST_NAME = "somlh1212/travelmap-backend:latest"

        // 프론트엔드 이미지 정보
        FRONTEND_IMAGE_TAG = "${env.BUILD_NUMBER}"
        FRONTEND_IMAGE_NAME = "somlh1212/travelmap-frontend:${FRONTEND_IMAGE_TAG}"
        FRONTEND_IMAGE_LATEST_NAME = "somlh1212/travelmap-frontend:latest"
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

        stage('Build Backend Docker Image') {
            steps {
                dir('backend') {
                    // 백엔드 이미지를 빌드하고 태그를 지정합니다.
                    sh "docker build -t ${BACKEND_IMAGE_NAME} -t ${BACKEND_IMAGE_LATEST_NAME} ."
                }
            }
        }

        stage('Push Backend Docker Image') {
            steps {
                // 백엔드 이미지를 Docker Hub에 푸시합니다.
                sh "docker push ${BACKEND_IMAGE_NAME}"
                sh "docker push ${BACKEND_IMAGE_LATEST_NAME}"
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    // npm 의존성 설치 및 빌드
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Build Frontend Docker Image') {
            steps {
                dir('frontend') {
                    // 프론트엔드 이미지를 빌드하고 태그를 지정합니다.
                    sh "docker build -t ${FRONTEND_IMAGE_NAME} -t ${FRONTEND_IMAGE_LATEST_NAME} ."
                }
            }
        }

        stage('Push Frontend Docker Image') {
            steps {
                // 프론트엔드 이미지를 Docker Hub에 푸시합니다.
                sh "docker push ${FRONTEND_IMAGE_NAME}"
                sh "docker push ${FRONTEND_IMAGE_LATEST_NAME}"
            }
        }

        stage('Deploy to K8s') {
            steps {
                // Secret, PV, PVC는 한 번만 배포하면 됩니다.
                sh 'kubectl apply -f /home/jenkins/k8s/secret.yaml'
                sh 'kubectl apply -f k8s/db/new-postgres-pv.yaml'
                sh 'kubectl apply -f k8s/db/new-postgres-pvc.yaml'
                sh 'kubectl apply -f k8s/db/postgres-deployment.yaml'

                // PostgreSQL 배포가 완료될 때까지 대기
                sh 'kubectl rollout status deployment/postgres'

                // 프론트엔드와 백엔드 Deployment를 최신 태그로 수정합니다.
                sh "sed -i 's|travelmap-backend:latest|travelmap-backend:${BACKEND_IMAGE_TAG}|g' k8s/deployment.yaml"
                sh "sed -i 's|travelmap-frontend:latest|travelmap-frontend:${FRONTEND_IMAGE_TAG}|g' k8s/frontend-deployment.yaml"

                // 백엔드와 프론트엔드 Deployment를 배포합니다.
                sh 'kubectl apply -f k8s/deployment.yaml'
                sh 'kubectl apply -f k8s/frontend-deployment.yaml'

                // 모든 Deployment가 완료될 때까지 대기
                sh 'kubectl rollout status deployment/travelmap-deployment'
                sh 'kubectl rollout status deployment/travelmap-frontend-deployment'

                // 백엔드와 프론트엔드 Service를 배포합니다.
                sh 'kubectl apply -f k8s/service.yaml'
                sh 'kubectl apply -f k8s/frontend-service.yaml'
            }
        }
    }
}