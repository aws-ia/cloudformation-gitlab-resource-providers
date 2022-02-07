# GitLab CloudFormation Resources - Developer Documentation

## Read Me First: Potential Surprises

* Some surprising folders are blown away during the build.
  * `GitLab-*/docs/`: so any custom docs should be placed in **docs-extra**
  * `generated/` including **/docs/generated/user/**:
    so any user-facing docs should be placed e.g. in `docs-extra/` so they are copied

* The projects mainly use Live Tests. These require access to GitLab and parameters as described below.


## Building and Pre-Requisites

This project uses the Resource Provider Development Kit to create customr resource types for AWS CloudFormation.

You should first set up the `aws` and `cfn` command-line tools following the instructions
[here](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-walkthrough.html).

You should then be able to build the project without tests with: `mvn clean install -DskipTests`


## Tests

### Live Java Tests

This project uses live tests. There isn't much else to test! The live tests require an access token for
connecting to GitLab and a few other arguments, set in the follow environment variables
(or in files named similarly, e.g. `~/.gitlab_cfn_tests/access_token`):

  GITLAB_CFN_TESTS_ACCESS_TOKEN=xxxxxxx    # a valid personal access token for gitlab.com
  GITLAB_CFN_TESTS_USER_ID                 # numeric user ID corresponding to the above token
  GITLAB_CFN_TESTS_USER_ID_TO_ADD          # numeric user ID of a _different_ user
  GITLAB_CFN_TESTS_USERNAME_TO_ADD         # username of this different user

With the above set, `mvn clean install` should work.

The tests create projects and groups, but will typically clean up after themselves.
If they do not (e.g. because they are interrupted) the tool `GitLabCleanup.java` can be used to find and delete test resources.
(They are also easy to spot at GitLab as they all have the prefix "cfn-test".) 

### Serverless Tests

TODO


## Registering Types and Running Examples

First, enter an access token into SSM (once) and then for each resource type, set the type configuration:

```
aws cloudformation set-type-configuration \
--region eu-north-1 \
--type RESOURCE \
--type-name GitLab::${XXX}::${YYY} \
--configuration-alias default \
--configuration '{"GitLabAccess": {"AccessToken": "{{resolve:ssm-secure:/cfn/gitlab/alex/access-token}}"}}'
```

Then build and register the resource:

```
cd GitLab-$XXX-$YYY
mvn clean install
cfn submit
```

If it has previously been installed, you may need to update the default version:

```
aws cloudformation set-type-default-version ...
```

And if doing a lot of development, you will hit the limit on number of versions and need to delete old ones: 

```
aws cloudformation deregister-type ...
```

Edit the values in the relevant example YAML (e.g. picking a valid parent group ID) and deploy with:

```
aws cloudformation create-stack --stack-name gitlab-${XXX}-${YYY}-test --template-body file://docs-extra/example.yaml
```


## Defining New Resources

The recommended way to add a new resource is to use `cfn init` in the directory for the new resource,
e.g. `GitLab-Xxxx-Yyyyy`,
using a package such as `com.gitlab.aws.cfn.resources.xxxx.yyyy`.

Then to prepare the code, we typically:

* Copy the `XxxxResourceHandler` from one of the other projects, renaming as appropriate.
* Modify the 5 handlers as per other projects (making each `extends XxxxResourceHandler.BaseHandlerAdapter {}`)
* Delete the generated tests
* Copy the `XxxxCrudlLiveTest` from one of the other projects, renaming as appropriate.
* Copy a `docs-extra/example.yaml` from one of the other projects, renaming as appropriate.
* Modify the `pom.xml` to match one of the other projects
* Modify the `template.yml` to match one of the other projects (remove "-handler" and change version to 1.0.0-SNAPSHOT)
* Replace most of the `gitlab-xxxx-yyyy.json` with the entries from another project, renaming as appropriate.
* Add `GitLab-Xxxx-Yyyy` to the parent `pom.xml`.

Then to develop:

* Edit the properties in `gitlab-xxxx-yyyy.json` as appropriate.
* Run `cfn generate`.
* Edit the `XxxxResourceHandler` and `XxxxCrudlLiveTest` to do the right thing for this resource.
* Create an example in `docs-extra/example.yaml` and test it (as above).
