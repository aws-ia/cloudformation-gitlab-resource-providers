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
        "<a href="#username" title="Username">Username</a>" : <i>String</i>,
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
    <a href="#username" title="Username">Username</a>: <i>String</i>
    <a href="#accesslevel" title="AccessLevel">AccessLevel</a>: <i>String</i>
</pre>

## Properties

#### GroupId

ID of the group to which the user should be added

_Required_: Yes

_Type_: Integer

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### UserId

ID (numeric) of the user to add to the group. Either this or Username but not both should be supplied.

_Required_: No

_Type_: Integer

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Username

Username (handle, e.g. often written starting with '@') of the user to add to the group. Either this or the UserId but not both should be supplied.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AccessLevel

The access level to grant to this user in the group, e.g. 'Guest', 'Developer', or 'Maintainer'. Note the GitLab API may not allow all values.

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

