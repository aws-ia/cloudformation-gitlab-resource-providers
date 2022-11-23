# GitLab CloudFormation Resources

This collection of [AWS CloudFormation resource types][1] allow GitLab to be controlled using [AWS CloudFormation][2].


| Resource                               | Description                                                       | Documentation                               |
|----------------------------------------|-------------------------------------------------------------------|---------------------------------------------|
| GitLab::Code::Tag                      | This resource type manages a [GitLab Git Tag][3]                  | [/GitLab-Code-Tag][4]                       |
| GitLab::Groups::Group                  | This resource type manages a [GitLab Group][5]                    | [/Gitlab-Groups-Group][6]                   |
| GitLab::Groups::GroupAccessToGroup     | This resource type manages a [GitLab Group Access][7]             | [/GitLab-Groups-GroupAccessToGroup][8]      |
| GitLab::Groups::UserMemberOfGroup      | This resource type manages a [GitLab Group Access][9]             | [/GitLab-Groups-UserMemberOfGroup][10]      |
| GitLab::Projects::AccessToken          | This resource type manages a [GitLab Project Access Token][11]    | [/GitLab-Projects-AccessToken][12]          |
| GitLab::Projects::GroupAccessToProject | This resource type manages a [GitLab Projects Group Access][13]   | [/GitLab-Projects-GroupAccessToProject][14] |
| GitLab::Projects::Project              | This resource type manages a [GitLab Project][15]                 | [/GitLab-Projects-Project][16]              |
| GitLab::Projects::UserMemberOfProject  | This resource type manages a [GitLab Project User Membership][17] | [/GitLab-Projects-UserMemberOfProject][18]  |


## Prerequisites
* [AWS Account][19]
* [AWS CLI][20]
* [GitLab Account][21] and [Access Token][22]
## AWS Management Console

To get started:

1. Sign in to the [AWS Management Console][23] with your account and navigate to CloudFormation.

2. Select "Public extensions" from the left hand pane and filter Publisher by "Third Party".

3. Use the search bar to filter by the "GitLab" prefix.

  Note: All official GitLab resources begin with `GitLab::` and specify that they are `Published by GitLab`.

4. Select the desired resource name to view more information about its schema, and click **Activate**.

5. On the **Extension details** page, specify:
  - Extension name
  - Execution role ARN
  - Automatic updates for minor version releases
  - Configuration

6. In your terminal, specify the configuration data for the registered GitLab CloudFormation resource type, in the given account and region by using the **SetTypeConfiguration** operation:


  For example:

  ```Bash
  $ aws cloudformation set-type-configuration \
  --region us-west-2 --type RESOURCE \
  --type-name GitLab::Code::Tag \
  --configuration-alias default \
  --configuration '{ "GitLabAccess": { "AccessToken": "{{resolve:ssm-secure:/cfn/gitlab/token:1}}", "Url": "{{resolve:ssm-secure:/cfn/gitlab/url:1}}"}}'
  ```

7. After you have your resource configured, [create your AWS stack][24] that includes any of the activated GitLab resources.

For more information about available commands and workflows, see the official [AWS documentation][25].

## Supported regions

The GitLab CloudFormation resources are available on the CloudFormation Public Registry in the following regions:

| Code            | Name                      |
|-----------------|---------------------------|
| us-east-1       | US East (N. Virginia)     |
| us-east-2       | US East (Ohio)            |
| us-west-1       | US West (N. California)   |
| us-west-2       | US West (Oregon)          |
| ap-south-1      | Asia Pacific (Mumbai)     |
| ap-northeast-1  | Asia Pacific (Tokyo)      |
| ap-northeast-2  | Asia Pacific (Seoul)      |
| ap-southeast-1  | Asia Pacific (Singapore)  |
| ap-southeast-2  | Asia Pacific (Sydney)     |
| ca-central-1    | Canada (Central)          |
| eu-central-1    | Europe (Frankfurt)        |
| eu-west-1       | Europe (Ireland)          |
| eu-west-2       | Europe (London)           |
| eu-west-3       | Europe (Paris)            |
| eu-north-1      | Europe (Stockholm)        |
| sa-east-1       | South America (São Paulo) |

**Note**: To privately register a resource in any other region, use the provided packages.

## Examples

### User management example using GitLab resources
```yaml
---
AWSTemplateFormatVersion: '2010-09-09'

Description: Manages an organization's standard groups and users within GitLab

Parameters:
  ParentGroupId:
    Type: Number
    Default: 16090842
    Description: Enter the ID of an existing group, e.g. for a "sample-company", where new groups will be created

Resources:
  All:
    Type: GitLab::Groups::Group
    Properties:
      ParentId: { Ref: ParentGroupId }
      Name: All Users
      Path: company-all

  AllIncludesAllDevelopers:
    Type: GitLab::Groups::GroupAccessToGroup
    Properties:
      SharedGroupId: {Ref: All}
      SharedWithGroupId: {Ref: AllDevelopers}
      AccessLevel: Developer
  AllIncludesAllReporters:
    Type: GitLab::Groups::GroupAccessToGroup
    Properties:
      SharedGroupId: {Ref: All}
      SharedWithGroupId: {Ref: AllReporters}
      AccessLevel: Reporter

  AllDevelopers:
    Type: GitLab::Groups::Group
    Properties:
      ParentId: { Ref: ParentGroupId }
      Name: All Developers
      Path: company-all-developers
  AllReporters:
    Type: GitLab::Groups::Group
    Properties:
      ParentId: { Ref: ParentGroupId }
      Name: All Reporters
      Path: company-all-reporters

  DeveloperUser1:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: AllDevelopers}
      Username: User1
      AccessLevel: Developer
  DeveloperSooz:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: AllDevelopers}
      Username: sooz
      AccessLevel: Developer

  ReporterBob:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: AllReporters}
      Username: bob
      AccessLevel: Reporter

  FrontEndDevelopers:
    Type: GitLab::Groups::Group
    Properties:
      ParentId: { Ref: ParentGroupId }
      Name: Front-End Developers
      Path: company-front-end-developers
  FrontEndDeveloperNakomis:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: FrontEndDevelopers}
      Username: Nakomis
      AccessLevel: Developer

  FrontEndReporters:
    Type: GitLab::Groups::Group
    Properties:
      ParentId: { Ref: ParentGroupId }
      Name: Front-End Reporters
      Path: company-front-end-reporters
  FrontEndReporterBob:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: FrontEndReporters}
      Username: bob
      AccessLevel: Reporter
```

### Setting up a new project with correct users and groups
```yaml
---
AWSTemplateFormatVersion: '2010-09-09'

Description: Creates GitLab structure for a new project kick off, including projects, groups and users

Parameters:
  ParentGroupId:
    Type: Number
    Default: 15776179
    Description: Enter the ID of an existing group, e.g. for a "sample-company", where new groups will be created

Resources:
  FrontEnd:
    Type: GitLab::Projects::Project
    Properties:
      Name: AcmeProject-FrontEnd
  BackEnd:
    Type: GitLab::Projects::Project
    Properties:
      Name: AcmeProject-BackEnd

  ProjectGroups:
    Type: GitLab::Groups::Group
    Properties:
      Name: AcmeProject-AllGroups
      ParentId: { Ref: ParentGroupId }
      Path: acme-project-groups
  ProjectManagers:
    Type: GitLab::Groups::Group
    Properties:
      Name: AcmeProject-ProjectManagers
      ParentId: { Ref: ProjectGroups }
      Path: acme-project-project-managers
  FrontEndDevelopers:
    Type: GitLab::Groups::Group
    Properties:
      Name: AcmeProject-FrontEndDevelopers
      ParentId: { Ref: ProjectGroups }
      Path: acme-project-frontend-developers
  BackEndDevelopers:
    Type: GitLab::Groups::Group
    Properties:
      Name: AcmeProject-BackEndDevelopers
      ParentId: { Ref: ProjectGroups }
      Path: acme-project-backend-developers


  ProjectManagerMemberOfGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: ProjectManagers }
      Username: cloudsoft_admin_geralt
      AccessLevel: Maintainer
  FrontEndDeveloperMemberOfGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: FrontEndDevelopers }
      Username: cloudsoft_developer_ciri
      AccessLevel: Developer
  BackEndDeveloperMemberOfGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: BackEndDevelopers }
      Username: cloudsoft_developer_jaskier
      AccessLevel: Developer
  FullStackDeveloperMemberOfFrontEndGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: { Ref: FrontEndDevelopers }
      Username: cloudsoft_developer_yennefer
      AccessLevel: Developer
  FullStackDeveloperMemberOfBackEndGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: { Ref: BackEndDevelopers }
      Username: cloudsoft_developer_yennefer
      AccessLevel: Developer

  ManagersInFrontEndProject:
    Type: GitLab::Projects::GroupAccessToProject
    Properties:
      ProjectId: { Ref: FrontEnd }
      GroupId: { Ref: ProjectManagers }
      AccessLevel: Maintainer
  ManagersInBackEndProject:
    Type: GitLab::Projects::GroupAccessToProject
    Properties:
      ProjectId: { Ref: BackEnd }
      GroupId: { Ref: ProjectManagers }
      AccessLevel: Maintainer
  DevelopersInFrontEndProject:
    Type: GitLab::Projects::GroupAccessToProject
    Properties:
      ProjectId: {Ref: FrontEnd }
      GroupId: {Ref: FrontEndDevelopers}
      AccessLevel: Developer
  DevelopersInBackEndProject:
    Type: GitLab::Projects::GroupAccessToProject
    Properties:
      ProjectId: { Ref: BackEnd }
      GroupId: { Ref: BackEndDevelopers }
      AccessLevel: Developer

  DeveloperVesemirInFrontEndProject:
    Type: GitLab::Projects::UserMemberOfProject
    Properties:
      ProjectId: { Ref: FrontEnd }
      Username: cloudsoft_developer_vesemir
      AccessLevel: Developer
```

### Tag a project automatically for every active CI/CD deployment
It is becoming increasingly common to use CloudFormation as part of continuous deployment. This ensures that the latest code on a nominated branch is available, whether for review by dev/test (in a staging or develop branch) or for end-users (in a release branch).

With GitLab tags integration available from CloudFormation, it's trivial to have tags to indicate which commit is deployed.
This can help us keep track of the version that is currently deployed, for example in a staging environment or even a live environment.
We can easily refer to the Commit SHA reference of the tag to determine whether the version deployed on the staging environment has the desired state (e.g. if a particular commit is included in that build).

Simply add the following to your the CloudFormation template which is deployed and updated as part of the CD pipeline:

```yaml

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

[1]: https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-types.html
[2]: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/Welcome.html
[3]: https://docs.gitlab.com/ee/topics/git/tags.html
[4]: GitLab-Code-Tag
[5]: https://docs.gitlab.com/ee/user/group/#groups
[6]: GitLab-Groups-Group
[7]: https://docs.gitlab.com/ee/user/group/access_and_permissions.html
[8]: GitLab-Groups-GroupAccessToGroup
[9]: https://docs.gitlab.com/ee/user/group/manage.html#add-users-to-a-group
[10]: GitLab-Groups-UserMemberOfGroup
[11]: https://docs.gitlab.com/ee/user/project/settings/project_access_tokens.html#project-access-tokens
[12]: GitLab-Projects-AccessToken
[13]: https://docs.gitlab.com/ee/api/members.html#give-a-group-access-to-a-project
[14]: GitLab-Projects-GroupAccessToProject
[15]: https://docs.gitlab.com/ee/user/project/working_with_projects.html
[16]: GitLab-Projects-Project
[17]: https://docs.gitlab.com/ee/user/project/members/#add-users-to-a-project
[18]: GitLab-Projects-UserMemberOfProject
[19]: https://aws.amazon.com/account/
[20]: https://aws.amazon.com/cli/
[21]: https://about.gitlab.com/
[22]: https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html
[23]: https://aws.amazon.com/console/
[24]: https://console.aws.amazon.com/cloudformation/home
[25]: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/registry.html
