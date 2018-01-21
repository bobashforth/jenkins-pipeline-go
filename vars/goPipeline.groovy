import com.privatesquare.pipeline.go.Go
import com.privatesquare.pipeline.utils.Git

def call() {
    node() {
        def go = new Go(this)
        def git = new Git(this)
        String nexusRepositoryId
        String scmCredentialsId, nexusCredentialsId
        String version, scmType, zipFile
        def jenkinsProperties

        final String propsFileName = 'jenkins.yml'

        step([$class: 'WsCleanup'])

        stage('checkout'){
            checkout scm
            scmType = getScmType(scm)
            println "[INFO] SCM type : ${scmType}"
        }

        stage('prepare') {

            timeout(time: 10, unit: 'SECONDS') {
                jenkinsProperties = readYaml file: propsFileName
            }

            scmCredentialsId = "bitbucket"
            nexusCredentialsId = "builder"
            nexusRepositoryId = "Go-releases"

            // generate version
            assert jenkinsProperties.version : 'Please add paramter "version" to your jenkins.yml file'
            String currentVersion = String.valueOf(jenkinsProperties.version)

            version = git.createNextTagVersion(currentVersion)
        }

        stage('build') {
            def goTool = tool name: 'go-1.9.2', type: 'go'
            String goPath = env.WORKSPACE

            println "[INFO] GOPATH : $goPath"

            String outputFolder = "${env.WORKSPACE}/bin"
            sh "mkdir -p ${outputFolder}"

            def outputs = [[
                                   OS: 'darwin',
                                   architecture: 'amd64',
                                   postfix: '-darwin'
                           ], [
                                   OS: 'darwin',
                                   architecture: 'amd64',
                                   postfix: '-darwin-x86'
                           ], [
                                   OS: 'windows',
                                   architecture: '386',
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
                go.build(goTool, goPath, output.OS, output.architecture, file, jenkinsProperties.groupId, jenkinsProperties.artifactId)
            }
            zipFile = "${jenkinsProperties.artifactId}.zip"
            if(fileExists(zipFile)) {
                sh "rm $zipFile"
            }

            zip dir: outputFolder, zipFile: zipFile
        }

        stage('tag') {
            git.createNextTag(String.valueOf(jenkinsProperties.version), scmCredentialsId)
        }

        stage('publish') {
            String artifactId = jenkinsProperties.artifactId
            String groupId = jenkinsProperties.groupId
            String packaging = "zip"

            uploadToNexus(
                    nexusRepositoryId,
                    groupId,
                    artifactId,
                    version,
                    packaging,
                    zipFile)
        }

        stage('cleanup') {
            //step([$class: 'WsCleanup'])
        }
    }
}