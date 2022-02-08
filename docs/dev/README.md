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

```
GITLAB_CFN_TESTS_ACCESS_TOKEN=xxxxxxx        # a valid personal access token for gitlab.com
GITLAB_CFN_TESTS_USER_ID=123                 # numeric user ID corresponding to the above token
GITLAB_CFN_TESTS_USER_ID_TO_ADD=456          # numeric user ID of a _different_ user
GITLAB_CFN_TESTS_USERNAME_TO_ADD=bob         # username of this different user
```

With the above set, `mvn clean install` should work.

The tests create projects and groups, but will typically clean up after themselves.
If they do not (e.g. because they are interrupted) the tool `GitLabCleanup.java` can be used to find and delete test resources.
(They are also easy to spot at GitLab as they all have the prefix "cfn-test".) 

### Serverless Tests

TODO


## Registering Types and Running Examples

First build the resources (deleting the `tests.jar` as that confuses `cfn submit`):

```
mvn clean install
rm {.,GitLab-*}/target/*-tests.jar
```

And register them, either individually:

```
cd GitLab-$XXX-$YYY
cfn submit
# aws cloudformation set-type-default-version ...    # required if previously installed
```

Or looping through all of them (optionally include the `set-type-configuration` command from above in the loop,
to set the type configuraition for each resource):

```
for x in GitLab-* ; do
  cd $x
  TYPE=$(echo $x | sed s/-/::/g)
  echo Registering $TYPE...
  cfn submit
  VERSION=$(aws --output yaml --no-cli-pager cloudformation list-type-versions --type RESOURCE --type-name $TYPE | \
    grep 'Arn: arn:' | sort | tail -1 | sed 's/.*\///')
  echo Using version $VERSION of $TYPE
  aws cloudformation set-type-default-version --type RESOURCE --type-name $TYPE --version-id $VERSION 
  cd ..
done
```

### Setting Type Configuration

If this is the first time registering, you will need to set up the type configuration used for
each of these types, containing the access token for connecting to GitLab.
We recommend entering the access token into Systems Manager, say under path `/cfn/gitlab/access-token`,
and then referring to it, e.g. as `{{resolve:ssm-secure:/cfn/gitlab/access-token}}`.

Once stored in SSM, it can be set for each resource type as follows:

```
TYPE=GitLab::${XXX}::${YYY}
SSM_PATH_TO_ACCESS_TOKEN=/cfn/gitlab/access-token
aws cloudformation set-type-configuration \
  --type RESOURCE --type-name $TYPE \
  --configuration-alias default \
  --configuration '{"GitLabAccess": {"AccessToken": "{{resolve:ssm-secure:'${SSM_PATH_TO_ACCESS_TOKEN}'}}"}}'
```

Or looping through all of them:

```
SSM_PATH_TO_ACCESS_TOKEN=/cfn/gitlab/access-token
for x in GitLab-* ; do
  TYPE=$(echo $x | sed s/-/::/g)
  echo Setting type configuration for $TYPE...
  aws --output yaml --no-cli-pager cloudformation set-type-configuration \
    --type RESOURCE --type-name $TYPE \
    --configuration-alias default \
    --configuration '{"GitLabAccess": {"AccessToken": "{{resolve:ssm-secure:'${SSM_PATH_TO_ACCESS_TOKEN}'}}"}}'
done
```


### Running Examples

Edit the values in the relevant example YAML (e.g. picking a valid parent group ID) and deploy with:

```
aws cloudformation create-stack --stack-name gitlab-test --template-body file://docs/path/to/resource/example.yaml
```

You should see the stack update in the CloudFormation console, and see the result in GitLab.

Thereafter you can `update-stack` and `delete-stack` in the usual way, combining these resources with others,
passing references, as per normal CloudFormation..


## Defining New Resources

The recommended way to add a new resource is to use `cfn init` in the directory for the new resource,
e.g. `GitLab-Xxxx-Yyyyy`,
using a package such as `com.gitlab.aws.cfn.resources.xxxx.yyyy`.

Then to prepare the code, we typically:

* Copy the `XxxxResourceHandler` from one of the other projects, renaming as appropriate.
* Modify the 5 handlers as per other projects (making each `extends XxxxResourceHandler.BaseHandlerAdapter {}`)
* Delete the `example_inputs` folder, `.gitignore` file, and `src/resources` folder
* Delete the generated tests `*.java`
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
* Copy SAM tests from another project and edit to do the right thing for this resource.
* Create an example in `docs-extra/example.yaml` and test it (as above).
