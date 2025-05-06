library identifier: "platform-ci-shared-library@v0.0.53"

def getJobView() {
  def parents = currentBuild.fullProjectName.split('/').collect {
    name -> Jenkins.instance.getItem(name)
  }.findAll {
    item -> item != null
  }
  def views = Jenkins.instance.views.findAll {
      view -> view.displayName != 'All'
  }
  for (item in parents) {
    echo "item = ${item}"
    for (view in views) {
      if (view.contains(item)) {
          def viewName = view.displayName
          echo "View: ${viewName} contains job: ${item.displayName}"
          return viewName
      }
    }
  }
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
