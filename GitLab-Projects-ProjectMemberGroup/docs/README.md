# GitLab::Projects::ProjectMemberGroup

Adds a group as a member of a GitLab project

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "GitLab::Projects::ProjectMemberGroup",
    "Properties" : {
        "<a href="#projectid" title="ProjectId">ProjectId</a>" : <i>String</i>,
        "<a href="#groupid" title="GroupId">GroupId</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: GitLab::Projects::ProjectMemberGroup
Properties:
    <a href="#projectid" title="ProjectId">ProjectId</a>: <i>String</i>
    <a href="#groupid" title="GroupId">GroupId</a>: <i>String</i>
</pre>

## Properties

#### ProjectId

ID of the project to which the group should be added

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### GroupId

ID of the group which should be added to the project

_Required_: Yes

_Type_: String

_Pattern_: <code>[0-9]+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the MembershipId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### MembershipId

Unique identifier for this membership resource, constructed by concatenating the other IDs

