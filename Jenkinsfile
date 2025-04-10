pipeline {
    agent any
    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
            }
        }
    }
  post {
    success {
        office365ConnectorSend(
            webhookUrl: "${params.WEBHOOK_URL}",
            status: "${currentBuild.result}",
            message: "nuxeo/nuxeo-lts #${BUILD_NUMBER}: Build success",
            factDefinitions: [
                [template: "Successfully built nuxeo-lts on branch ${GIT_BRANCH}."],
                [name: 'Description', template: "Build 2025.1.14"],
                [name: 'Changes', template: "\\n- [NXBT-3821](https://hyland.atlassian.net/browse/NXBT-3821): Fix nxHelmfile.template\\n- [NXBT-3615](https://hyland.atlassian.net/browse/NXBT-3615): Fix potential AccessDeniedException in Git#setupCredentials\\n- [NEV-717](https://hyland.atlassian.net/browse/NEV-717): Allow getting the latest Git tag from a remote repository\\n- [NEV-717](https://hyland.atlassian.net/browse/NEV-717): Allow getting some ConfigMap data\n- [NEV-719](https://hyland.atlassian.net/browse/NEV-719): Allow downloading GitHub Actions workflow run artifacts\\n- ..."],
                [name: 'Committers', template: "<at>Antoine Taillefer</at>"],
                [name: 'actionTitle_1', template: "View build"],
                [name: 'actionUrl_1', template: "https://jenkins.platform.dev.nuxeo.com/job/nuxeo/job/lts/job/nuxeo/job/2025/623/"],
            ],
            color: 'good'
        )
    }
    unsuccessful {
        office365ConnectorSend(
            webhookUrl: "${params.WEBHOOK_URL}",
            status: "${currentBuild.result}",
            message: "nuxeo/nuxeo-lts #${BUILD_NUMBER}: Build failure",
            factDefinitions: [
                [template: "Successfully built nuxeo-lts on branch ${GIT_BRANCH}."],
                [name: 'Description', template: "Build 2025.1.14"],
                [name: 'Changes', template: "\\n- [NXBT-3821](https://hyland.atlassian.net/browse/NXBT-3821): Fix nxHelmfile.template\\n- [NXBT-3615](https://hyland.atlassian.net/browse/NXBT-3615): Fix potential AccessDeniedException in Git#setupCredentials\\n- [NEV-717](https://hyland.atlassian.net/browse/NEV-717): Allow getting the latest Git tag from a remote repository\\n- [NEV-717](https://hyland.atlassian.net/browse/NEV-717): Allow getting some ConfigMap data\n- [NEV-719](https://hyland.atlassian.net/browse/NEV-719): Allow downloading GitHub Actions workflow run artifacts\\n- ..."],
                [name: 'Committers', template: "<at>Antoine Taillefer</at>"],
                [name: 'actionTitle_1', template: "View build"],
                [name: 'actionUrl_1', template: "https://jenkins.platform.dev.nuxeo.com/job/nuxeo/job/lts/job/nuxeo/job/2025/623/"],
            ],
            color: 'attention'
        )
    }
  }
}
