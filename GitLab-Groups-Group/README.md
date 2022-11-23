# GitLab::Groups::Group

This resource type manages a [GitLab Groups Group][5]

[Documentation][4]

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
  --type-name GitLab::Groups::Group \
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

### Shows how to create a Group in GitLab

```yaml
---
AWSTemplateFormatVersion: '2010-09-09'
Description: Shows how to create a group in GitLab. Note a group ParentId is usually required by GitLab.
Resources:
  MySampleGroup:
    Type: GitLab::Groups::Group
    Properties:
      ParentId: 16020673
      Name: my-sample-group
      Path: path-to-sample-group
```

[4]: ./docs/README.md
[5]: https://docs.gitlab.com/ee/user/group/#groups
[19]: https://aws.amazon.com/account/
[20]: https://aws.amazon.com/cli/
[21]: https://about.gitlab.com/
[22]: https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html
[23]: https://aws.amazon.com/console/
[24]: https://console.aws.amazon.com/cloudformation/home
[25]: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/registry.html
