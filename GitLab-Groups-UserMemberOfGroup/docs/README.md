# GitLab::Groups::UserMemberOfGroup

Adds a user as a member of a GitLab group

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "GitLab::Groups::UserMemberOfGroup",
    "Properties" : {
        "<a href="#groupid" title="GroupId">GroupId</a>" : <i>Integer</i>,
        "<a href="#userid" title="UserId">UserId</a>" : <i>Integer</i>,
        "<a href="#accesslevel" title="AccessLevel">AccessLevel</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: GitLab::Groups::UserMemberOfGroup
Properties:
    <a href="#groupid" title="GroupId">GroupId</a>: <i>Integer</i>
    <a href="#userid" title="UserId">UserId</a>: <i>Integer</i>
    <a href="#accesslevel" title="AccessLevel">AccessLevel</a>: <i>String</i>
</pre>

## Properties

#### GroupId

ID of the group to which the user should be added

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UserId

ID of the user to add to the group

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AccessLevel

The access level to grant to this user in the group, e.g. 'guest', 'developer', or 'maintainer'. Note the GitLab API may not allow all values.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>None</code> | <code>Minimal Access</code> | <code>Guest</code> | <code>Reporter</code> | <code>Developer</code> | <code>Maintainer</code> | <code>Owner</code> | <code>Admin</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the MembershipId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### MembershipId

Unique identifier for this membership resource, constructed by concatenating the other IDs

