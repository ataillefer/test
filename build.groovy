library identifier: "platform-ci-shared-library@v0.0.53"

def getJobView() {
  def parents = currentBuild.fullProjectName.split('/').collect {
    name -> Jenkins.instance.getItem(name)
  }.findAll {
    item -> item != null
  }
  for (item in parents) {
    echo "item = ${item}"
  }
  echo "currentBuild.fullProjectName = ${currentBuild.fullProjectName}"
  echo "getItemByFullName = ${Jenkins.instance.getItemByFullName(currentBuild.fullProjectName)}"
  return null
}

pipeline {
  agent any
  stages {
    stage('Hello') {
      steps {
        echo 'Hello World'
        script {
          getJobView()
        }
      }
    }
  }
}
