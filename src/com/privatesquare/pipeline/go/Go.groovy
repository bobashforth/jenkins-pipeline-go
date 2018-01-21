package com.privatesquare.pipeline.go


class Go implements Serializable {

    String goTool
    def steps
    Go(steps) {this.steps = steps}

    /**
     * Creates a package name for usage with Go
     */
    String getFullPackageName(final String groupId, final String artifactId) {
        String separator = '/'
        String path = groupId.replace('.', separator)
        return path + separator + artifactId
    }

    /**
     * Build Go code
     */
    void build(final String goPath, final String OS, final String architecture, final String output, final String groupId, final String artifactId) {
        String fullPackageName = getFullPackageName(groupId, artifactId)
        steps.withEnv(["GOROOT=${goTool}", "PATH=$PATH;${goTool}/bin", "GOPATH=${goPath}"]) {
            steps.dir(goPath) {
                steps.withEnv(["GOOS=$OS", "GOARCH=$architecture"]) {
                    steps.sh "go build -o $output $fullPackageName"
                }
            }
        }
    }
}