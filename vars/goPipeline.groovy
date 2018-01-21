import com.privatesquare.pipeline.go.Go
import com.privatesquare.pipeline.utils.Git

def call() {
    node("linux") {

        Git git
        Go go
        String nexusRepositoryId
        String builderCredentials
        String version, scmType, zipFile
        def jenkinsProperties

        final String propsFileName = 'jenkins.properties'

        stage('checkout'){
            checkout scm
            scmType = getScmType(scm)
            println "SCM Type: ${scmType}"
        }

        stage('prepare') {

            timeout(time: 10, unit: 'SECONDS') {
                jenkinsProperties = readProperties file: propsFileName
            }

            builderCredentials = "builder"
            nexusRepositoryId = "ATS-releases"

            // generate version
            assert jenkinsProperties.version : 'Please add "version" to your jenkins.properties file'
            String currentVersion = String.valueOf(jenkinsProperties.version)

            version = git.createNextTagVersion(currentVersion, builderCredentials)
        }

        stage('build') {
            def goTool = tool name: 'Go 1.8.1', type: 'go'
            String goPath = env.WORKSPACE

            println "GOPATH: $goPath"

            String outputFolder = "${env.WORKSPACE}/bin"
            sh "mkdir -p ${outputFolder}"

            def outputs = [[
                                   OS: 'darwin',
                                   architecture: '386',
                                   postfix: '-darwin-x86'
                           ], [
                                   OS: 'windows',
                                   architecture: 'amd64',
                                   postfix: '.exe'
                           ], [
                                   OS: 'windows',
                                   architecture: '386',
                                   postfix: '-32.exe'
                           ] , [
                                   OS: 'linux',
                                   architecture: 'amd64',
                                   postfix: '-linux'
                           ], [
                                   OS: 'linux',
                                   architecture: '386',
                                   postfix: '-linux-x86'
                           ]]


            for(output in outputs) {
                String file = "${outputFolder}/${jenkinsProperties.artifactId}${output.postfix}"
                go.build(goPath, output.OS, output.architecture, file, jenkinsProperties.groupId, jenkinsProperties.artifactId)
            }
            zipFile = "${jenkinsProperties.artifactId}.zip"
            if(fileExists(zipFile)) {
                sh "rm $zipFile"
            }

            zip dir: outputFolder, zipFile: zipFile
        }

        stage('tag') {
            git.createNextTag(String.valueOf(jenkinsProperties.version), builderCredentials, utilities)
        }

        stage('publish') {
            NexusArtifact artifact = new NexusArtifact()
            artifact.artifactId = jenkinsProperties.artifactId
            artifact.classifier = ''
            artifact.file = zipFile
            artifact.type = 'zip'

            String groupId = jenkinsProperties.groupId

            nexus.uploadArtifact(
                    nexusUrl,
                    nexusRepositoryId,
                    groupId,
                    version,
                    artifact,
                    builderCredentials)
        }

        stage('cleanup') {
            step([$class: 'WsCleanup'])
        }
    }
}