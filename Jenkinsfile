#!groovy

stage('Checkout') {
    node {
        checkout scm
    }
}

stage('Pre-build') {
    node {
        echo "Node Name: ${env.NODE_NAME}"
        echo "Branch name: ${env.BRANCH_NAME}"
    }
}

stage('Build and Test') {
    node {
        sh './gradlew test'
    }
}
