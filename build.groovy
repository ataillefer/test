def msteamsNotification(Map args = [:]) {
    def channelId = args.channelId
    def title = args.title
    def titleColor = args.titleColor ?: 'default'
    def message = args.message


    def workflowUrl = params.WEBHOOK_URL // The URL from user provided as parameter
    def buildStatus = currentBuild.currentResult ?: "N/A"
    def imageUrl = "https://www.jenkins.io/images/logos/jenkins/jenkins.png"

    if (buildStatus != 'SUCCESS') {
        imageUrl = "https://www.jenkins.io/images/logos/fire/fire.png"
        titleColor = "attention"
    }

    def description = currentBuild.description

    def changeset = """
      - [NXBT-3821](https://hyland.atlassian.net/browse/NXBT-3821): Fix nxHelmfile.template
      - [NXBT-3615](https://hyland.atlassian.net/browse/NXBT-3615): Fix potential AccessDeniedException in Git#setupCredentials
      - [NEV-717](https://hyland.atlassian.net/browse/NEV-717): Allow getting the latest Git tag from a remote repository
      - [NEV-717](https://hyland.atlassian.net/browse/NEV-717): Allow getting some ConfigMap data
      - [NEV-719](https://hyland.atlassian.net/browse/NEV-719): Allow downloading GitHub Actions workflow run artifacts
      - ...
    """.stripIndent()
    def facts = [
        [name: 'Changes', value: "${changeset}"],
        [name: 'Committers', value: "<at>Antoine Taillefer</at>"],
    ]
    def actions = [
        [name: 'View build', url: "${BUILD_URL}"],
    ]
    def payload = """
    {
        "channelId": "${channelId}",
        "content": {
            "type": "AdaptiveCard",
            "\$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
            "msTeams": {
                "width": "Full",
                "entities": [
                    {
                        "type": "mention",
                        "text": "<at>Antoine Taillefer</at>",
                        "mentioned": {
                            "id": "antoine.taillefer@hyland.com",
                            "name": "Antoine Taillefer"
                        }
                    }
                ]
            },
            "body": [
                {
                    "type": "ColumnSet",
                    "columns": [
                        {
                            "type": "Column",
                            "width": "auto",
                            "items": [
                                {
                                    "type": "Image",
                                    "url": "${imageUrl}",
                                    "altText": "Jenkins logo",
                                    "size": "small"
                                }
                            ]
                        },
                        {
                            "type": "Column",
                            "items": [
                                    {
                                    "type": "TextBlock",
                                    "text": "${title}",
                                    "wrap": true,
                                    "size": "large",
                                    "weight": "bolder",
                                    "color": "${titleColor}"
                                },
                                {
                                    "type": "TextBlock",
                                    "text": "${description}",
                                    "wrap": true,
                                    "spacing": "none",
                                    "isSubtle": true
                                }
                            ]
                        }
                    ]
                },
                {
                    "type": "TextBlock",
                    "text": "${message}",
                    "wrap": true,
                    "spacing": "small"
                },
                {
                    "type": "TextBlock",
                    "text": "**${facts[0]['name']}:** ${facts[0]['value']}",
                    "wrap": true,
                    "spacing": "small"
                },
                {
                    "type": "TextBlock",
                    "text": "**${facts[1]['name']}:** ${facts[1]['value']}",
                    "wrap": true,
                    "spacing": "small"
                }
            ],
            "actions": [
                {
                    "type": "Action.OpenUrl",
                    "title": "${actions[0].name}",
                    "url": "${actions[0].url}"
                }
            ]
        }
    }
    """

    // Method to send a notification to MS Teams
    httpRequest(
        url: workflowUrl,
        httpMode: 'POST',
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        timeout: 10, // seconds
        requestBody: payload
    )
}

pipeline {
    agent any
    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
                script {
                    currentBuild.description = 'Build 2025.1.14'

                    def o = ['records': [
                        'car': [
                            'name': 'HSV Maloo',
                            'make': 'Holden',
                            'year': 2006,
                            'country': 'Australia',
                        ]
                    ]]
                    def j = writeJSON(
                        json: o,
                        returnText: true,
                        pretty: 4
                    )
                    echo j
                }
            }
        }
    }
    // post {
    //     success {
    //         msteamsNotification(
    //             '19:817f8655db3745389cb64b4f4db5cc18@thread.tacv2',
    //             "nuxeo/nuxeo-lts #${BUILD_NUMBER}: Build success",
    //             'good',
    //             "Successfully built nuxeo-lts on branch ${GIT_BRANCH}"
    //         )
    //         msteamsNotification(
    //             '19:817f8655db3745389cb64b4f4db5cc18@thread.tacv2',
    //             "Release LTS 2021.69",
    //             'good',
    //             "LTS 2021.69 and 2021-HF69 are released and online."
    //         )
    //     }
    //     unsuccessful {
    //         msteamsNotification(
    //             '19:817f8655db3745389cb64b4f4db5cc18@thread.tacv2',
    //             "nuxeo/nuxeo-lts #${BUILD_NUMBER}: Build failure",
    //             'attention',
    //             "Failed to build nuxeo-lts on branch ${GIT_BRANCH}"
    //         )
    //     }
    // }
}
