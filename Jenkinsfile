#!/usr/bin/env groovy

pipeline {

    agent {
        docker { dockerfile true }
    }

    stages {
        stage('Test') {
            steps {
                echo 'Running tests...'
                sh './gradlew test'
            }
        }
    }
}