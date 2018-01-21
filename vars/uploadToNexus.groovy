
def call(final String nexusUrl,
         final String repositoryId,
         final String groupId,
         final String version,
         NexusArtifact[] artifacts,
         final String credentialsId,
         final String nexusVersion = 'nexus2',
         final String nexusProtocol = 'https') {

    uploadArtifacts(nexusUrl, repositoryId, groupId, version,artifacts, credentialsId, nexusVersion, nexusProtocol)
}