pipeline {
    agent any

    stages {
        stage('Get Code') {
            steps {
                git changelog: false, poll: false, url: 'https://github.com/mhassan-cpu/Rest-Assured-Library.git'
            }
        }

        stage('Run Test') {
            steps {
                bat 'cd /d "D:\\Training\\Library" && mvn clean verify -Denv="http://localhost:3000"'

            }
        }
    }
}