library identifier: "platform-ci-shared-library@v0.0.53"


pipeline {
  agent any
  options {
    disableConcurrentBuilds(abortPrevious: true)
  }
  stages {
    stage('Hello') {
      steps {
        echo 'Hello World'
      }
    }
  }
}
