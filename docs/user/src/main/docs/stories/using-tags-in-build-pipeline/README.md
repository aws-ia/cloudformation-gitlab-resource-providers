# Using tags to mark the deployed version in a staging environment

Our company uses CloudFormation as part of the build pipeline to automatically link a successful deployment with the commit representing the version of the software that was deployed.
Thanks to this, we are always certain that our testing environment has the most recent version of the product deployed.

We can enrich our CloudFormation template to create a GitLab tag every time the build artifacts are successfully deployed.
This can help us keep track of the version that is currently deployed in the staging environment. 
We can easily refer to the Commit SHA reference of the tag to determine whether the version deployed on the staging environment has the desired state (e.g. if a particular commit is included in that build).

Moreover, this best practice guarantees that no one accidentally bypasses the process by deploying without providing a Commit SHA for tracking purposes.

This can be achieved by simply adding the following to the CloudFormation template:
```
Parameters:
  ...
  Timestamp...
  CommitSha...

Resources:
  GitLabTag:
    Type: GitLab::Code::Tag
    Properties:
      Name: {Fn::Join: ["dev-head-deployment-", {Ref: Timestamp}]}
      Ref: {Ref: CommitSha}
      Message: Commit currently deployed to dev-head staging environment
      ProjectId: 123
```