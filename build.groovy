library identifier: "platform-ci-shared-library@v0.0.53"

def jiraUrl = nxJira.getServerBrowseURL()

def getChange(commitMessage) {
  def parts = commitMessage.split(':', 2)
  if (parts.size() > 1) {
    def jiraIssue = parts[0]
    def message = parts[1]
    return "[${jiraIssue}](${jiraUrl}${jiraIssue}): ${message}"
  } else {
    return commitMessage
  }
}

def getUserEmail(fullName) {
  def parts = fullName.split(' ', 2)
  if (parts.size() <=1) {
    // nxUtils.log(message: "Cannot get email for user full name: ${fullName}")
    return null
  }
  return "${parts[0]}.${parts[1]}@hyland.com"
}

def send(Map args = [:]) {
    def channelId = args.channelId ?: '19:817f8655db3745389cb64b4f4db5cc18@thread.tacv2'
    def title = args.title // required
    def description = args.description
    def icon = args.icon
    def iconColor = args.iconColor ?: 'default'
    def message = args.message
    def changeset = args.changeset
    def committers = args.committers
    // to not display any actions, pass an empty list
    def actions = args.actions == null ? [
      [name: 'View build', url: "${BUILD_URL}"],
    ] : actions
    
    // Microsoft Adaptive card JSON defintion with its minimum content: post title
    def adaptiveCard = [
      'channelId': channelId,
      'content': [
        'type': 'AdaptiveCard',
        '\$schema': 'http://adaptivecards.io/schemas/adaptive-card.json',
        'msTeams': [
          'width': 'full'
        ],
        'body': [
          [
            'type': 'ColumnSet',
            'columns': [
              [
                'type': 'Column',
                'spacing': 'small',
                'items': [
                  [
                    'type': 'TextBlock',
                    'text': title,
                    'wrap': true,
                    'size': 'large',
                    'weight': 'bolder'
                  ]
                ]
              ]
            ]
          ]
        ],
        'actions': []
      ]
    ]
    
    def content = adaptiveCard['content']
    def body = content['body']
    def headerColumns = body[0]['columns']

    if (description) {
      headerColumns[0]['items'].add([
        'type': 'TextBlock',
        'text': description,
        'wrap': true,
        'isSubtle': true,
        'spacing': 'none'
      ]
      )
    }

    if (icon) {
      headerColumns.add(0, [
        'type': 'Column',
        'width': 'auto',
        'items': [[
          'type': 'Icon',
          'name': icon,
          'size': 'small',
          'style': 'filled',
          'color': iconColor
        ]]
      ])
    }

    if (message) {
      body.add([
        'type': 'TextBlock',
        'text': message,
        'wrap': true,
        'spacing': 'small'
      ])
    }

    if (changeset) {
      def text = changeset.join('\\n- ')
      body.add([
        'type': 'TextBlock',
        'text': "**Changes:** ${text}",
        'wrap': true,
        'spacing': 'small'
      ])
    }

    if (committers) {
      def entities = []
      def atCommitters = []
      committers.each { committer -> 
        def atCommitter = "<at>${committer}</at>"
        entities.add([
          'type': 'mention',
          'text': atCommitter,
          'mentioned': [
            'id': getUserEmail(committer),
            'name': committer
          ]
        ])
        atCommitters.add(atCommitter)
      }
      content['msTeams']['entities'] = entities
      def text = atCommitters.join(', ')
      body.add([
        'type': 'TextBlock',
        'text': "**Committers:** ${text}",
        'wrap': true,
        'spacing': 'small'
      ])
    }

    actions.each {
      action -> content['actions'].add([
        'type': 'Action.OpenUrl',
        'title': action.name,
        'url': action.url
      ])
    }
    
    def payload = writeJSON(
        json: adaptiveCard,
        returnText: true,
        pretty: 4
    )

    // nxUtils.log(message: "JSON payload for Teams webhook: ${payload}")
    echo "JSON payload for Teams webhook: ${payload}"

    withCredentials([string(credentialsId: 'teams', variable: 'TEAMS_WEBHOOK_URL')]) {
      httpRequest(
        url: TEAMS_WEBHOOK_URL,
        httpMode: 'POST',
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        timeout: 10, // seconds
        requestBody: payload
      )
    }
    
}

def changeset = []
def committers = []

pipeline {
  agent any
  stages {
    stage('Hello') {
      steps {
        echo 'Hello World'
        script {
          currentBuild.description = 'Build 2025.1.14'
          for (changeLogSet in currentBuild.changeSets) {
            def repositoryBrowser = changeLogSet.getBrowser()
            for (item in changeLogSet.getItems()){
              if (repositoryBrowser.getChangeSetLink(item).toString().startsWith(GIT_URL)) {
                changeset.add(getChange(item.getComment()))
                committers.add(item.getAuthorName())
              }
            }
          }
        }
      }
    }
  }
  post {
    success {
      send(
        title: "Build success: nuxeo/nuxeo-lts #${BUILD_NUMBER}",
        description: currentBuild.description,
        icon: 'CheckmarkCircle',
        iconColor: 'good',
        message: "Successfully built nuxeo-lts on branch ${GIT_BRANCH}",
        changeset: changeset,
        committers: committers
      )
      // send(
      //   title: "Release LTS 2021.69",
      //   icon: 'CheckmarkCircle',
      //   iconColor: 'good',
      //   message: "LTS 2021.69 and 2021-HF69 are released and online."
      // )
      // send(
      //   title: "Build failure: nuxeo/nuxeo-lts #${BUILD_NUMBER}",
      //   description: currentBuild.description,
      //   icon: 'ErrorCircle',
      //   iconColor: 'attention',
      //   message: "Failed to build nuxeo-lts on branch ${GIT_BRANCH}",
      //   changeset: changeset,
      //   committers: committers
      // )
      // send(
      //   title: "Release LTS 2021.60",
      //   icon: 'ErrorCircle',
      //   iconColor: 'attention',
      //   message: "Failed to release Nuxeo 2021.60 from build 2021.60.7"
      // )
    }
  }
}
