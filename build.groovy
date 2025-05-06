library identifier: "platform-ci-shared-library@v0.0.53"

import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject

def getJobView() {
  def views = Jenkins.instance.views.findAll {
    view -> view.displayName != 'All'
  }
  def currentJob = Jenkins.instance.getItemByFullName(currentBuild.fullProjectName)
  while (!(currentJob instanceof Hudson)) {
    echo "currentJob = ${currentJob}"
    echo "currentJob.displayName = ${currentJob.displayName}"
    for (view in views) {
      if (view.contains(currentJob)) {
        def viewName = view.displayName
        echo "View: ${viewName} contains job: ${currentJob.displayName}"
        return viewName
      }
    }
    currentJob = currentJob.parent
    echo "currentJob.parent=${currentJob}"
  }
  // def topLevelProject = currentBuild.fullProjectName.split('/', 2)[0]
  // echo "topLevelProject = ${topLevelProject}"
  // if (topLevelProject) {
  //   def topLevelItem = Jenkins.instance.getItem(topLevelProject)
  //   if (topLevelItem) {
  //     echo "topLevelItem = ${topLevelItem}"
  //     def views = Jenkins.instance.views.findAll {
  //       view -> view.displayName != 'All'
  //     }.each { view ->
  //       if (view.contains(topLevelItem)) {
  //         def viewName = view.displayName
  //         echo "View: ${viewName} contains job: ${topLevelItem.displayName}"
  //         return viewName
  //       }
  //     }
  //   }
  // }
  return null
}

def getJobName() {
  def currentJob = Jenkins.instance.getItemByFullName(currentBuild.fullProjectName)
  if (currentJob.parent instanceof WorkflowMultiBranchProject) {
    echo "multibranch"
    echo "parentJobName=${currentJob.parent.displayName}"
  } else {
    echo "regular"
    echo "jobName=${currentJob.displayName}"
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
