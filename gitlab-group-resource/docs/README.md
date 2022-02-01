# Cloudformation::GitLab::Group

An example resource schema demonstrating some basic constructs and validation rules.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "Cloudformation::GitLab::Group",
    "Properties" : {
        "<a href="#hosturl" title="HostURL">HostURL</a>" : <i>String</i>,
        "<a href="#accesstoken" title="AccessToken">AccessToken</a>" : <i>String</i>,
        "<a href="#groupname" title="GroupName">GroupName</a>" : <i>String</i>,
        "<a href="#path" title="Path">Path</a>" : <i>String</i>,
        "<a href="#parentid" title="ParentId">ParentId</a>" : <i>Integer</i>
    }
}
</pre>

### YAML

<pre>
Type: Cloudformation::GitLab::Group
Properties:
    <a href="#hosturl" title="HostURL">HostURL</a>: <i>String</i>
    <a href="#accesstoken" title="AccessToken">AccessToken</a>: <i>String</i>
    <a href="#groupname" title="GroupName">GroupName</a>: <i>String</i>
    <a href="#path" title="Path">Path</a>: <i>String</i>
    <a href="#parentid" title="ParentId">ParentId</a>: <i>Integer</i>
</pre>

## Properties

#### HostURL

URL of the GitLab host server, e.g. https://gitlab.com

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AccessToken

Personal access token used to access the host server

_Required_: Yes

_Type_: String

_Minimum_: <code>20</code>

_Maximum_: <code>250</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### GroupName

Name of the group

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Path

Path of the group

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ParentId

ID of the group's parent

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the UID.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### UID

Unique identifier of the group

