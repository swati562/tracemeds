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
    }

    post {
        success {
            echo 'Build and tests passed successfully.'
        }
        failure {
            echo 'Build or tests failed — check the logs above.'
        }
    }
}