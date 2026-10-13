pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        AWS_DEFAULT_REGION = 'ap-south-1'
        AWS_ACCOUNT_ID     = '470451076070'
        ECR_REPOSITORY     = '470451076070.dkr.ecr.ap-south-1.amazonaws.com/retail-banking-app'
        EKS_CLUSTER_NAME   = 'retail-banking-eks'
        HELM_RELEASE       = 'banking-app'
        IMAGE_TAG          = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Application') {
            steps {
                dir('application') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('application') {
                    sh '''
                        docker build \
                          -t "${ECR_REPOSITORY}:${IMAGE_TAG}" .
                    '''
                }
            }
        }

        stage('Login to ECR') {
            steps {
                sh '''
                    aws ecr get-login-password \
                      --region "${AWS_DEFAULT_REGION}" \
                    | docker login \
                      --username AWS \
                      --password-stdin \
                      "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"
                '''
            }
        }

        stage('Push Image to ECR') {
            steps {
                sh '''
                    docker push "${ECR_REPOSITORY}:${IMAGE_TAG}"
                '''
            }
        }

        stage('Deploy to EKS with Helm') {
            steps {
                sh '''
                    aws eks update-kubeconfig \
                      --region "${AWS_DEFAULT_REGION}" \
                      --name "${EKS_CLUSTER_NAME}"

                    helm upgrade --install "${HELM_RELEASE}" helm/banking-app \
                      --namespace default \
                      --set image.repository="${ECR_REPOSITORY}" \
                      --set-string image.tag="${IMAGE_TAG}"

                    kubectl rollout status deployment/"${HELM_RELEASE}" \
                      --namespace default \
                      --timeout=180s
                '''
            }
        }
    }

    post {
        success {
            echo 'CI/CD pipeline completed successfully.'
        }
        failure {
            echo 'CI/CD pipeline failed. Check the stage logs above.'
        }
    }
}
