pipeline {
    agent any

    environment {
        IMAGE_NAME = "iamsnaaz/cicd-pipeline-demo"
        TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/iamsnaaz/Poc4_demo.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                sh 'echo "No tests yet"'
            }
        }

        // stage('SonarQube Analysis') {
        //     steps {
        //         withSonarQubeEnv('sonar-server') {
        //             sh 'mvn sonar:sonar'
        //         }
        //     }
        // }

        

        stage('Trivy File System Scan') {
            steps {
                sh '''
                mkdir -p trivy-cache

                trivy fs . \
                  --cache-dir trivy-cache \
                  --format table \
                  --output trivy-fs-report.txt \
                  --exit-code 0 || true
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$TAG .'
            }
        }

        stage('Trivy Image Scan') {
            steps {
                sh '''
                trivy image \
                  --cache-dir trivy-cache \
                  --format table \
                  --output trivy-image-report.txt \
                  --severity HIGH,CRITICAL \
                  --exit-code 0 \
                  $IMAGE_NAME:$TAG || true
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-creds',
                    usernameVariable: 'USER',
                    passwordVariable: 'PASS'
                )]) {
                    sh 'echo $PASS | docker login -u $USER --password-stdin'
                    sh 'docker push $IMAGE_NAME:$TAG'
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                export KUBECONFIG=/var/lib/jenkins/.kube/config

                kubectl apply -f k8s/deployment.yaml
                kubectl apply -f k8s/service.yaml

                kubectl set image deployment/cicd-app app=$IMAGE_NAME:$TAG

                kubectl rollout status deployment/cicd-app
                """
            }
        }
    }

post {
    always {
        emailext(
            to: 'sadiyanaazpoc@gmail.com',
            recipientProviders: [[$class: 'RequesterRecipientProvider']],
            subject: "Build ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: """
Build Status: ${currentBuild.currentResult}

Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
URL: ${env.BUILD_URL}
""",
            attachLog: true,
            attachmentsPattern: 'trivy-*.txt'
        )
    }
}
