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
or in a properties file `.gitlab_cfn_tests` in current directory or any ancestor such as your home directory:

```
# a valid personal access token for gitlab.com
GITLAB_CFN_TESTS_ACCESS_TOKEN=xxxxx-a1b2c3secret

# numeric user ID corresponding to the above token
GITLAB_CFN_TESTS_USER_ID=123

# numeric user ID and username of a _different_ user
GITLAB_CFN_TESTS_USER_ID_TO_ADD=456
GITLAB_CFN_TESTS_USERNAME_TO_ADD=Bob
```

With the above set, `mvn clean install` should work.

The tests create projects and groups, but will typically clean up after themselves.
If they do not (e.g. because they are interrupted) the tool `GitLabCleanup.java` can be used to find and delete test resources.

Wherever possible, test resources in GitLab should be created with the prefix `cfn-test` to make them easy to find and support automated cleanup.


### Serverless and Contract Tests

This project can also use [CloudFormation Serverless and Contract Tests](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test.html),
using [AWS Serverless Application Model (SAM)](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html).
These allow:

* Local testing of resource code as lambdas, to ensure payload encapsulation and results for individual requests
* CFN contract tests, to ensure compliance with CFN expectations (some of which are a little surprising)
  and to test sequences of requests possibly including callbacks (although GitLab resources are quick enough they do not use callbacks)

This project does not perform this testing automatically, nor does it maintain test artifacts in all cases,
due to the time to run and overhead of creating dependencies.  The Java Live tests give good coverage,
and the resources are tested as installed to CloudFormation.
However from time to time it may be useful to run serverless tests, to confirm obscure contract compliance
and to test serverless locally.

To run sererless tests, first ensure `sam` is set up and available (per the links above).


#### Local Serverless Single Request Testing

To run a single local serverless test, simply create a file containing the payload, including the type configuration;
e.g. the following `sam-tests/create-1.json`:

```
{
    "credentials": {
        # Real STS credentials need to go here.
        "accessKeyId": "",
        "secretAccessKey": "",
        "sessionToken": ""
    },
    "action": "CREATE",
    "request": {
        # Can be any UUID.
        "clientRequestToken": "4b90a7e4-b790-456b-a937-0cfdfa211dfe", 
        "desiredResourceState": {
            # specify properties required as per resource schema
            "Name": "test" 
        },
        "logicalResourceIdentifier": "TestResource"
    },
    "callbackContext": null,
    "typeConfiguration": {
      "gitLabAccess": {
        # set your actual GitLab access token here
        "accessToken": "pat12-34567890abcdef"
      }
    }
}
```

(Remove the lines which start with `#`.)

The test can then be invoked and the output inspected using the CLI:

```
sam local invoke TestEntrypoint --event sam-tests/create-1.json
```


#### CFN Contract Testing

To use the standsrd `cfn test` contract testing automation, which runs a series of commands,
including callbacks, and some create-update-delete cycles, you must first set the type configuration
to use. This is done in `~/.cfn-cli/typeConfiguration.json`:
```
{
  "gitLabAccess": {
    "accessToken": "pat12-34567890abcdef"
  }
}
```

For most resources it is also necessary to set specific parameters to pass for CREATE and UPDATE,
e.g. in `GitLab-Projects-Project`:

```
{
    "CREATE": {
        "/Name": "cfn-test-sample-project"
    },
    "UPDATE": {
        "/Name": "cfn-test-sample-project-renamed"
    }
}
```

If there are resources in GitLab which must exist prior to execution, add them manually
(or in `template.yml` and you can then refer to them per the instructions above). 

Next start the lambda:

```
sam local start-lambda
```

And in another terminal window run the tests:

```
cfn test
```

If tests do not pass, it can be useful to inspect `rpdk.log` as well as the output in each of the terminal windows,
and the [Python test suite source code](https://github.com/aws-cloudformation/cloudformation-cli/blob/master/src/rpdk/core/contract/suite/).
In particular it is easy to interrupt tests leaving GitLab in a state where the resource already exists;
run the `GitLabCleanup` class described previously and try again.


## Registering Types and Running Examples

First build the resources (optionally `-DskipTests` for speed or if a live test environment is not available):

```
mvn clean install
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
aws --output yaml --no-cli-pager cloudformation set-type-configuration \
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

### Design Notes

Due to the highly regular nature of the GitLab API, much of the work is done by abstract superclasses.
However in order to be able to leverage `cfn generate` to autogenerate model classes on schema changes,
there is a bit of a subtle pattern to route from the generated `HandlerWrapper` to the generated-once-and-required
`CreateHandler` (and others) then back to a single concrete combined handler extending the abstract superclass.

The scaffolding is thus a bit clumsy, but it means the actual code needed to be written and maintained for
any resource is quite small, and the majority of the CFN contract behaviour including error checks and
required responses is handled automatically.

There is also scaffolding for tests allowing a default pattern of CRUD to be easily and automatically written,
extended with other tests where desired. 


### How to Add Code for a New GitLab Resource

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

