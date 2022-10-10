# GitLab::Projects::GroupAccessToProject

Adds a group as a member of a GitLab project

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "GitLab::Projects::GroupAccessToProject",
    "Properties" : {
        "<a href="#projectid" title="ProjectId">ProjectId</a>" : <i>Integer</i>,
        "<a href="#groupid" title="GroupId">GroupId</a>" : <i>Integer</i>,
        "<a href="#accesslevel" title="AccessLevel">AccessLevel</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: GitLab::Projects::GroupAccessToProject
Properties:
    <a href="#projectid" title="ProjectId">ProjectId</a>: <i>Integer</i>
    <a href="#groupid" title="GroupId">GroupId</a>: <i>Integer</i>
    <a href="#accesslevel" title="AccessLevel">AccessLevel</a>: <i>String</i>
</pre>

## Properties

#### ProjectId

ID of the project to which the group should be added

_Required_: Yes

_Type_: Integer

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### GroupId

ID of the group which should be added to the project

_Required_: Yes

_Type_: Integer

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AccessLevel

The access level to grant to this group for the project, e.g. 'guest', 'developer', or 'maintainer'. Note the GitLab API may not allow all values.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>None</code> | <code>Minimal Access</code> | <code>Guest</code> | <code>Reporter</code> | <code>Developer</code> | <code>Maintainer</code> | <code>Owner</code> | <code>Admin</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### MembershipId

Unique identifier for this membership resource, constructed by concatenating the other IDs

