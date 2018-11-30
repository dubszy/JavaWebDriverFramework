#!groovy

node {
    try {
        stage('Checkout') {
            checkout scm
        }

        stage('Pre-build') {
            echo "Node Name: ${env.NODE_NAME}"
            echo "Branch name: ${env.BRANCH_NAME}"
            echo "Git Commit: ${env.GIT_COMMIT}"
            echo "Git Previous Commit: ${env.GIT_PREVIOUS_COMMIT}"
            echo "Git Previous Successful Commit: ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}"
        }

        stage('Build and Test') {
            sh './gradlew test'
        }
    } catch (err) {
        throw err
    }
}