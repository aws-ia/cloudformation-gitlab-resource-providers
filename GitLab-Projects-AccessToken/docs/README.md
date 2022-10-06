# GitLab::Projects::AccessToken

Creates a Project Access Token in GitLab

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "GitLab::Projects::AccessToken",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#projectid" title="ProjectId">ProjectId</a>" : <i>Integer</i>,
        "<a href="#accesslevel" title="AccessLevel">AccessLevel</a>" : <i>Integer</i>,
        "<a href="#scopes" title="Scopes">Scopes</a>" : <i>[ String, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: GitLab::Projects::AccessToken
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#projectid" title="ProjectId">ProjectId</a>: <i>Integer</i>
    <a href="#accesslevel" title="AccessLevel">AccessLevel</a>: <i>Integer</i>
    <a href="#scopes" title="Scopes">Scopes</a>: <i>
      - String</i>
</pre>

## Properties

#### Name

The name of the Project Access Token to create.

_Required_: Yes

_Type_: String

_Maximum_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ProjectId

The ID (numeric) of the project for which this Access Token is created. The project should exist and the user creating the Access Token should have rights to do this on this project.

_Required_: Yes

_Type_: Integer

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AccessLevel

A valid access level. Default value is 40 (Maintainer). Other allowed values are 10 (Guest), 20 (Reporter), and 30 (Developer).

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Scopes

The scopes this Project Access Token will be used for. The list of supported scopes is in the official GitLab documentation here: https://docs.gitlab.com/ee/user/project/settings/project_access_tokens.html#scopes-for-a-project-access-token .

_Required_: Yes

_Type_: List of String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Id.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

The ID of the Project Access Token

