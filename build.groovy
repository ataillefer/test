library identifier: "platform-ci-shared-library@v0.0.53"

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
        'spacing': 'none',
        'isSubtle': true
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
      body.add([
        'type': 'TextBlock',
        'text': "**Changes:** ${changeset}",
        'wrap': true,
        'spacing': 'small'
      ])
    }

    if (committers) {
      content['msTeams']['entities'] = [[
        'type': 'mention',
        'text': '<at>Antoine Taillefer</at>',
        'mentioned': [
          'id': 'antoine.taillefer@hyland.com',
          'name': 'Antoine Taillefer'
        ]
      ]]
      body.add([
        'type': 'TextBlock',
        'text': "**Committers:** ${committers}",
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

def changeset = """
  - [NXBT-3821](https://hyland.atlassian.net/browse/NXBT-3821): Fix nxHelmfile.template
  - [NXBT-3615](https://hyland.atlassian.net/browse/NXBT-3615): Fix potential AccessDeniedException in Git#setupCredentials
  - [NEV-717](https://hyland.atlassian.net/browse/NEV-717): Allow getting the latest Git tag from a remote repository
  - [NEV-717](https://hyland.atlassian.net/browse/NEV-717): Allow getting some ConfigMap data
  - [NEV-719](https://hyland.atlassian.net/browse/NEV-719): Allow downloading GitHub Actions workflow run artifacts
  - ...
""".stripIndent()

def committers = '<at>Antoine Taillefer</at>'

pipeline {
  agent any
  stages {
    stage('Hello') {
      steps {
        echo 'Hello World'
        script {
          currentBuild.description = 'Build 2025.1.14'
        }
      }
    }
  }
  post {
    success {
      send(
        title: "nuxeo/nuxeo-lts #${BUILD_NUMBER}: Build success",
        description: currentBuild.description,
        icon: 'CheckmarkCircle',
        iconColor: 'good',
        message: "Successfully built nuxeo-lts on branch ${GIT_BRANCH}",
        changeset: changeset,
        committers: committers
      )
      send(
        title: "Release LTS 2021.69",
        icon: 'CheckmarkCircle',
        iconColor: 'good',
        message: "LTS 2021.69 and 2021-HF69 are released and online."
      )
    }
    unsuccessful {
      send(
        title: "nuxeo/nuxeo-lts #${BUILD_NUMBER}: Build failure",
        description: currentBuild.description,
        icon: 'ErrorCircle',
        iconColor: 'attention',
        message: "Failed to build nuxeo-lts on branch ${GIT_BRANCH}",
        changeset: changeset,
        committers: committers
      )
      send(
        title: "Release LTS 2021.60",
        icon: 'ErrorCircle',
        iconColor: 'attention',
        message: "Failed to release Nuxeo 2021.60 from build 2021.60.7"
      )
    }
  }
}
