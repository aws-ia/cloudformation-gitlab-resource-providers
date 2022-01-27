# Cloudformation::GitLab::Project

This is a Maven multi-module project for development of CloudFormation GitLab resources. 

The main project encompases all common versions and plugins used by its modules. Each module is an independent project modelling a **single resource type**. A resource type you create is treated as a first-class citizen within CloudFormation: you can manage your resource as you would manage any AWS resource. 

To quickly create a resource module the CloudFormation (`cfn`) CLI from AWS comes in handy. This is a Python 3 executable and thus, so ensure that you have python 3 installed:

```bash
python3 --version
```
If not, install by running:
```bash
brew install python
```
The `cfn` and its dependencies must be installed in each resource module, because it analyzes and (re)generates source files based on context.

### Creating a resource module

Create the directory for your resource named `gitlab-{resource_name}-resource`. In the terminal, enter this directory and install `cfn` but running the following:

```bash
python3 -m venv env
source env/bin/activate
# plugins for other languages may also be added: 
# cloudformation-cli-go-plugin cloudformation-cli-python-plugin cloudformation-cli-typescript-plugin
pip3 install cloudformation-cli cloudformation-cli-java-plugin
```

You are now ready to generate you Maven project for your resource. Project setup wizard can be run with:
```bash
cfn init
```
Go through the wizard to generate a new project. Enter `r` to create a resource type project and introduce the type name as `CloudFormation::GitLab::{resource-name}`
Initializing a resource type project generates source code and test code for you in the src folder.

The log snippet below depicts the process of creating the Maven project for the `CloudFormation::GitLab::User` resource type.
```
$ > cfn init
Initializing new project
Do you want to develop a new resource(r) or a module(m)?.
>> r
What's the name of your resource type?
(Organization::Service::Resource)
>> CloudFormation::GitLab::User
One language plugin found, defaulting to java
Enter a package name (empty for default 'com.cloudformation.gitlab.user'): 
>> 
Choose codegen model - 1 (default) or 2 (guided-aws): 
>> 1
Could not find specified format 'date-time' for type 'string'. Defaulting to 'String'
Could not find specified format 'date-time' for type 'string'. Defaulting to 'String'
Initialized a new project in /{workspace}/cloudformation-gitlab/gitlab-project-user
```

**Note 1:** `cfn` generates a  Maven project, not a Maven module, to include this project as a module in the project, you must modify the generated `pom.xml` as instructed below:

1. Rename the artifact from `cloudformation-gitlab-{resource-name}-handler` to `gitlab-{resource-name}-resource` ( `cfn` really like to call everything a `handler`, but this module is a collection of handlers for a resource type)
2. You must add the `cloudformation-gitlab` as the parent. 
```xml
<parent>
        <groupId>com.cloudformation.gitlab.project</groupId>
        <artifactId>cloudformation-gitlab</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
</parent>
```
3. You must add your project as a module in the [cloudformation-gitlab/pom.xml](./pom.xml).
```xml
    <modules>
        ...
        <module>gitlab-project-{resource-name}</module> 
    </modules>
```
4. Walk through the generated `pom.xml` and strip everything that is already present in the  [cloudformation-gitlab/pom.xml](./pom.xml) . Usually the following configuration elements: `<properties>` , `<dependencies>` , but keep the `aws-cloudformation-rpdk-java-plugin` dependency, `cfn` needs that.
5. In the `build` configuration add the following element:
```xml
<finalName>cloudformation-gitlab-{resource-name}-handler-${version}</finalName>
```

**Note 2:** Or you could duplicate an existing module and replace the resource name everywhere. :)





Congratulations on starting development! Next steps:

1. Write the JSON schema describing your resource, `cloudformation-gitlab-project.json`
1. Implement your resource handlers.

The RPDK will automatically generate the correct resource model from the schema whenever the project is built via Maven. You can also do this manually with the following command: `cfn generate`.

> Please don't modify files under `target/generated-sources/rpdk`, as they will be automatically overwritten.

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.
