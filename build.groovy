library identifier: "platform-ci-shared-library@v0.0.53"


pipeline {
  agent any
  options {
    disableConcurrentBuilds()
  }
  stages {
    stage('Hello') {
      steps {
        echo 'Hello World'
      }
    }
  }
}
