library identifier: "platform-ci-shared-library@v0.0.53"

import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject

def getJobView() {
  def topLevelProject = currentBuild.fullProjectName.split('/', 2)[0]
  echo "topLevelProject = ${topLevelProject}"
  if (topLevelProject) {
    def topLevelItem = Jenkins.instance.getItem(topLevelProject)
    if (topLevelItem) {
      echo "topLevelItem = ${topLevelItem}"
      def views = Jenkins.instance.views.findAll {
        view -> view.displayName != 'All'
      }.each { view ->
        if (view.contains(topLevelItem)) {
          def viewName = view.displayName
          echo "View: ${viewName} contains job: ${topLevelItem.displayName}"
          return viewName
        }
      }
    }
  }
  return null
}

def getJobName() {
  def currentJob = Jenkins.instance.getItemByFullName(currentBuild.fullProjectName)
  echo "currentJob = ${currentJob}"
  if (currentJob instanceof WorkflowMultiBranchProject) {
    echo "is multibranch"
    echo "currentJob.parent.displayName=${currentJob.parent.displayName}"
  } else {
    echo "regular"
    echo "currentJob.displayName=${currentJob.displayName}"
  }
}

pipeline {
  agent any
  stages {
    stage('Hello') {
      steps {
        echo 'Hello World'
        script {
          getJobView()
          getJobName()
        }
      }
    }
  }
}
