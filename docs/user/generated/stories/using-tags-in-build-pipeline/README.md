# Tag a project automatically for every active CI/CD deployment

It is becoming increasingly common to use CloudFormation as part of continuous deployment. This ensures that the latest code on a nominated branch is available, whether for review by dev/test (in a staging or develop branch) or for end-users (in a release branch).

With GitLab tags integration available from CloudFormation, it's trivial to have tags to indicate which commit is deployed.
This can help us keep track of the version that is currently deployed, for example in a staging environment or even a live environment. 
We can easily refer to the Commit SHA reference of the tag to determine whether the version deployed on the staging environment has the desired state (e.g. if a particular commit is included in that build).

Simply add the following to your the CloudFormation template which is deployed and updated as part of the CD pipeline:

```
# Parameters:
#  Timestamp
#  CommitSha
#  ProjectId
#  TagPrefix

Resources:
  GitLabTag:
    Type: GitLab::Code::Tag
    Properties:
      Name: {Fn::Join: [{Ref: TagPrefix}, "-", {Ref: Timestamp}]}
      Ref: {Ref: CommitSha}
      Message: Commit currently deployed to dev-head staging environment
      ProjectId: {Ref: ProjectId}
```

Given a prefix `dev-branch-staging-env`, this will create a tag `dev-branch-staging-env-${timestamp}` for the version currently deployed, and it will remove old tags whenever updated or undeployed!

Not only does this make it clear what is currently deployed to any enviroment, this best practice guarantees that no one accidentally bypasses the process by deploying without providing a Commit SHA for tracking purposes.
