# GitLab::Projects::GroupAccessToProject

This resource type manages a [GitLab Project Group Access][13]

[Documentation][26]

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
  --type-name GitLab::Projects::GroupAccessToProject \
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

### Shows how to share a GitLab project with a GitLab group with a given access level.

```yaml
---
AWSTemplateFormatVersion: '2010-09-09'
Description: Shows how to share a GitLab project with a GitLab group with a given access level.
Resources:
  MyGroupSharingAProject:
    Type: GitLab::Projects::GroupAccessToProject
    Properties:
      ProjectId: 33430825
      GroupId: 16020673
      AccessLevel: Maintainer
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
[26]: ./docs/README.md
