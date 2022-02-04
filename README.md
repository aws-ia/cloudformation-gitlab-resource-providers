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

## Creating a resource module

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
Enter a package name (empty for default 'com.cloudformation.gitlab.projecttoken'): 
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

Now that you have a module modelling your resource, the next step is to develop and test your implementation. 

## Important observation

**Note:** When running `cfn init` the resource type's schema file is generated that is named `cloudformation-gitlab-{resource-name}.json`. Do not rename this file! Renaming will cause `cfn` to malfunction and complain about not finding the resource model. 

This file is referenced in the `pom.xml` file and it **must** be part of the resulting artifact, otherwise when creating a stack using the resource a `NullPointerException` will be thrown.

## Building the project

The code uses [Lombok](https://projectlombok.org/), and [you need to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes. For example, in IntelliJ IDEA you need to check that the Lombok plugin is installed and enabled, and that `annotation processing` is enabled for the project.

You can build the full project or modules independently by running:
```bash
mvn clean install
```

The RPDK will automatically generate the correct resource model from the schema whenever the project is built via Maven. You can also do this manually with the following command: 

```bash
cfn generate
```

## AWS CloudFormation resources

A resource type you create is treated as a first-class citizen within CloudFormation: you can manage your resource as you would manage any AWS resource. The Software Development Life Cycle (SDLC) of a resource type can be summarized as follows:

* First, you install prerequisite tools you will use for development and testing of your resource type.
* You then start to develop and run tests for your resource type.
* When ready, you submit the resource type to the AWS CloudFormation registry : you can choose to register a private or a public extension.
* Manage your resource type with CloudFormation: you describe the resource type and its properties in your CloudFormation template(s), like you would do with any AWS resource type.

If you use third-party resources in your infrastructure and applications, you can now model and automate those resources by developing them as resource types for use within CloudFormation. A `resource` type includes a resource type specification and handlers that control API interactions with the underlying AWS or third-party services. These interactions include create, read,update,delete, and list (CRUDL) operations for resources. Use resource types to model and provision resources using CloudFormation. Developers can use cfn-guard either locally while editing templates or automatically as part of a CI/CD pipeline to stop deployment of non-compliant resources. If resources in the template fail the rules, cfn-guard provides developers information to help identify non-compliant resources.

### Resource Schema

A Resource Schema is a JSON-formatted text file that defines properties and attributes for a specific resource type. This is an example of a resource schema
```json
{
    "typeName": "myORG::myService::myResource",
    "properties": {
        "Name": {
            "description": "The name of the resource.",
            "type": "string",
            "pattern": "^[a-zA-Z0-9_-]{0,64}$",
            "maxLength": 64
        }
    },
    "createOnlyProperties": [
        "/properties/Name"
    ],
    "identifiers": [
        [
            "/properties/Name"
        ]
    ],
    "additionalProperties": false
}

```

In order to be considered valid, your resource type's schema must adhere to the [Resource type definition schema](https://github.com/aws-cloudformation/cloudformation-cli/blob/master/src/rpdk/core/data/schema/provider.definition.schema.v1.json)
This meta-schema provides a means of validating your resource specification during resource development.

Once you have defined your resource schema, you can use the CloudFormation CLI `validate` command to verify that the resource schema is valid.

```bash
cfn validate
```

In terms of testing, the resource schema also determines:

What unit test stubs are generated in your resource package, and what contract tests are appropriate to run for the resource. When you run the CloudFormation CLI `generate` command, the CloudFormation CLI generates empty unit tests based on the properties of the resource and their attributes.

Which contract tests are appropriate for CloudFormation CLI to run for your resources. When you run the `test`  command, the CloudFormation CLI runs the appropriate contract tests, based on which handlers are included in your resource schema. To learn more about modeling a resource type, see the Modeling resource types for use in AWS CloudFormation  page.

### Type Configuration
Additionally, in the Resource Schema we can specify a type configuration, which allows us to pass authentication credentials for the GitLab account outside of CloudFormation template.

In order to achieve that, we should modify the resource schema to have a `typeConfiguration`:
```json
    "typeConfiguration": {
        "properties": {
            "GitLabAuthentication": {
                "$ref": "#/definitions/Credentials"
            }
        },
        "additionalProperties": false,
        "required": [
            "GitLabAuthentication"
        ]
    }
```
The configuration above refers to a `Credentials` definition so this needs to be specified as well in the resource schema, as follows:
```json
"definitions": {
  "Credentials": {
    "type": "object",
    "properties": {
      "HostUrl": {
        "description": "URL of the GitLab Server",
        "type": "string"
      },
      "AuthToken": {
        "description": "Authentication Token",
        "type": "string"
      }
    },
    "additionalProperties": false
  }
}
```
Above, we have a definition for a `Credentials` object with 2 properties: `HostUrl` and `AuthToken`. 

Using `cfn generate` on such schema, `TypeConfigurationModel` and `Credentials` classes are auto generated, with the former containing an object of the latter.
In order to use the Credentials in the handlers, we pass the `TypeConfigurationModel` to `handleRequest()` and we can then use it as follows:
```
TypeConfigurationModel tcm; //passed to the handleRequest() method
Credentials credentials = tcm.getGitLabAuthentication();
credentials.getHostUrl();
credentials.getAuthToken();
```

Once the handlers are implemented to use the `typeConfiguration`, we can set the credentials using the `AWS cli`:
```
aws cloudformation set-type-configuration \
    --region eu-west-1 \
    --type RESOURCE \
    --type-name CloudFormation::GitLab::Project\
    --configuration-alias default \
    --configuration "{\"GitLabAuthentication\": {\"HostUrl\": \"https://gitlab.com\", \"AuthToken\": \"glpat-5YPGKq-7gtk5R3GA6stH\"}}"
```

Then you can deploy the stack using your original input and credentials are pulled from the type configuration specified above.

### Templates

AWS CloudFormation gives users an easy way to model, provision, and manage related AWS and third-party resources in a declarative language. AWS CloudFormation uses a template (i.e. Infrastructure as Code) for provisioning and managing resources. For more details on the service and language refer to the resources below.

More reference information about templates can be found [here](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/template-reference.html).

### Cloudformation CLI

The CloudFormation Command Line Interface (CLI) is an open-source tool that enables you to develop and test AWS and third-party extensions, such as resource types or modules, and register them for use in AWS CloudFormation. The CloudFormation CLI provides a consistent way to model and provision both AWS and third-party extensions through CloudFormation. The CloudFormation CLI includes commands to manage each step of creating your extensions. For more information on CloudFormation CLI commands see, CloudFormation CLI referenceCloudFormation CLI reference .

An extension is an artifact, registered in the CloudFormation registry, which augments the functionality of CloudFormation in a native manner. Extensions can be registered by Amazon, APN partners, AWS Marketplace sellers, and the developer community.

You can use the CloudFormation CLI to register extensions – both those you create yourself, in addition to ones shared with you – with the CloudFormation registry. Extensions enable CloudFormation capabilities to create, provision, and manage these custom types in a safe and repeatable manner, just as you would any AWS resource. For more information on the CloudFormation registry, see Using the CloudFormation registry  in the CloudFormation User Guide.

Reference:
* [CloudFormation CLI](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/what-is-cloudformation-cli.html)
* [CloudFormation registry](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/registry.html)

## Modelling a Resource

### Resource Schema

When you initiate the resource type project, an example resource type schema file is included to help start you modeling your resource type.
This is a JSON file named for your resource, and contains an example of a typical resource type schema.
In this project the schema file is named `cloudformation-gitlab-{resource-name}.json`, where `{resource-name}` is a placeholder replacing an actual GitLab resource name such as : project, group, user, etc. You should customize this file according to the specifications of your resource. 

## Resource Handlers

With the Java data classes generated, we can now start writing the handlers that actually implement the resource’s functionality. When you generate your resource package, the CloudFormation CLI stubs out empty handler functions, each of which each corresponds to a specific event in the resource lifecycle. You add logic to these handlers to control what happens to your resource type at each stage of its lifecycle.

* `create`: CloudFormation invokes this handler when the resource is initially created during stack create operations.
* `read`: CloudFormation invokes this handler as part of a stack update operation when detailed information about the resource's current state is required.
* `update`: CloudFormation invokes this handler when the resource is updated as part of a stack update operation.
* `delete`: CloudFormation invokes this handler when the resource is deleted, either when the resource is deleted from the stack as part of a stack update operation, or the stack itself is deleted.
* `list`: CloudFormation invokes this handler when summary information about multiple resources of this resource type is required.
