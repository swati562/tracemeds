pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn -B -f srv/pom.xml clean test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -f srv/pom.xml package -DskipTests'
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'cf-credentials', usernameVariable: 'CF_USERNAME', passwordVariable: 'CF_PASSWORD')]) {
                    sh '''
                        curl -L "https://packages.cloudfoundry.org/stable?release=linux64-binary&version=v8&source=github" | tar -zx
                        chmod +x cf8
                        ./cf8 api $CF_API
                        ./cf8 auth $CF_USERNAME $CF_PASSWORD
                        ./cf8 target -o $CF_ORG -s $CF_SPACE
                        ./cf8 push tracemeds-srv -p srv/target/*.jar -b sap_java_buildpack_jakarta
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed — check the logs above.'
        }
    }
}