pipeline {
    agent any

    environment {
        SSH_KEY64 = credentials('SSH_KEY64')
    }

    parameters {
        string(
            name: 'SERVER_IP',
            defaultValue: '44.223.7.233',
            description: 'Target Server'
        )
    }

    stages {

        stage('Configure SSH') {
            steps {
                sh '''
                mkdir -p ~/.ssh
                chmod 700 ~/.ssh

                echo -e "Host *\\n\\tStrictHostKeyChecking no\\n\\n" > /var/lib/jenkins/ .ssh/config
                chmod 600 ~/.ssh/config

                touch ~/.ssh/known_hosts
                chmod 600 ~/.ssh/known_hosts
                '''
            }
        }

        stage('SSH Key Access') {
            steps {
                sh '''
                echo "$SSH_KEY64" | base64 -d > mykey.pem
                chmod 400 mykey.pem

                ssh-keygen -R ${SERVER_IP} || true
                '''
            }
        }

        stage('Deploy Code to Server') {
            steps {
                sh '''
                ssh -i mykey.pem ubuntu@${SERVER_IP} \
                "cd /var/www/html && git pull origin main"
                '''
            }
        }
    }
}
