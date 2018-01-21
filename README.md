# Go pipeline

## Usage

```Jenkinsfile
@Library(['stpl-pipeline-go', 'stpl-pipeline-core']) _
goPipeline()
```

And add a jenkins.properties-file (description in [Configuration](#Configuration))

## Project setup

We assume that if your groupId is ```com.abnamro.myDepartment``` and your artificatId is ```myApplication```, then your application can be found in the folder ```src/com/abnamro/myDepartment/myApplcation``` (relative to the rootfolder of your git-project). 

## Configuration <a name="Configuration"></a>

|name|required|description|example
|---|---|---|---|
|groupId|yes|full qualified domain name of your project|com.abnamro.stpl.support
|artifactId|yes|name of your application|hockeyapp
|version|yes|major.minor.increment|1.0.0
|systemLetterCode|yes|3- or 4-letter acronym for your project|STPL

Example: [jenkins.properties](jenkins.properties)