package com.privatesquare.pipeline.go


class Go implements Serializable {

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
    void build(final String goTool, final String goPath, final String OS, final String architecture, final String output, final String groupId, final String artifactId) {
        String fullPackageName = getFullPackageName(groupId, artifactId)
        steps.println "[INFO] OS : ${OS}"
        steps.println "[INFO] Architecture : ${architecture}"
       
        steps.print "${steps.PATH}"
        steps.print "${steps.env.PATH}"
        env.PATH=${steps.PATH};${goTool}/bin
        steps.print "$env.PATH"
        steps.withEnv(["GOROOT=${goTool}", "PATH=${steps.PATH};${goTool}/bin", "GOPATH=${goPath}"]) {
            steps.dir(goPath) {
                steps.withEnv(["GOOS=$OS", "GOARCH=$architecture"]) {
                    steps.sh "${goTool}/bin/go build -o $output $fullPackageName"
                }
            }
        }
    }
}