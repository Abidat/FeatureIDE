script{
    setBuildStatus = {String message, String context, String state ->
        
        withCredentials([string(credentialsId: 'github-commit-status-token', variable: 'TOKEN')]) {
            
            sh """
                set -x
                curl \"https://api.github.com/repos/FeatureIDE/FeatureIDE/statuses/$GIT_COMMIT?access_token=$TOKEN\" \
                    -H \"Content-Type: application/json\" \
                    -X POST \
                    -d \"{\\\"description\\\": \\\"$message\\\", \\\"state\\\": \\\"$state\\\", \\\"context\\\": \\\"$context\\\", \\\"target_url\\\": \\\"$BUILD_URL\\\"}\"
            """
        } 
    }
}


pipeline {
    agent any
    
    tools {
        maven 'Maven 3.5.2'
    }
    
    stages {
        stage ('Initialize') {
            
            steps {  
                script {
                    setBuildStatus("Compiling", "compile", "pending");
                    def causes = currentBuild.getBuildCauses('com.cloudbees.jenkins.GitHubPushCause').shortDescription
                    if(!causes) {
                        causes = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').shortDescription
                    }
                    currentBuild.displayName = "#${BUILD_NUMBER} ${GIT_BRANCH} ${causes}"
                }
      			sh '''
               		echo "PATH = ${PATH}"
               		echo "M2_HOME = ${M2_HOME}"
                    echo "causes"
               	'''
            }
        }

        stage ('Test') {
            steps {
                sh 'mvn clean test' 
            }
        }

        stage ('Package') {
        	steps {
        		sh 'mvn clean package'
        	}
        }

        stage ('Verify') {
        	steps {
                wrap([$class: 'Xvfb', additionalOptions: '', assignedLabels: '', autoDisplayName: true, debug: true, displayNameOffset: 0, installationName: 'default', parallelBuild: true, screen: '']) {
                    sh 'mvn clean verify'
                }
        	}
        }
    }
    post {
        //always { 
        //}
        success{
            script{
                setBuildStatus("Build complete", "compile", "success")
            }
        }
        unsuccessful {
            script {
                setBuildStatus("Failed", "pl-compile", "failure")
                def author = ""
                if(currentBuild && !currentBuild.changeSets.isEmpty()) {
                    author += "Commit author: ${currentBuild.changeSets.getAt(0).getItems()[0].getAuthor()}"
                } else {
                    author += "${currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').shortDescription}"
                    author = author.substring(1, author.length()-1)
                }
                def gitBranch = currentBuild.displayName.substring(currentBuild.displayName.indexOf('/')+1, currentBuild.displayName.indexOf('['))
                emailext body: "Result can be found at:'${currentBuild.absoluteUrl}' \n \n${author} \n \nGitbranch: ${gitBranch} ", subject: "Failed Branch: ${gitBranch}", to: 'c.orsinger@tu-braunschweig.de'
            }
        }
    }    
}

