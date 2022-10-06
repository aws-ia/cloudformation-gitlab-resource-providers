# GitLab::Groups::GroupAccessToGroup

Adds a group as a member of another GitLab group

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "GitLab::Groups::GroupAccessToGroup",
    "Properties" : {
        "<a href="#sharedgroupid" title="SharedGroupId">SharedGroupId</a>" : <i>Integer</i>,
        "<a href="#sharedwithgroupid" title="SharedWithGroupId">SharedWithGroupId</a>" : <i>Integer</i>,
        "<a href="#accesslevel" title="AccessLevel">AccessLevel</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: GitLab::Groups::GroupAccessToGroup
Properties:
    <a href="#sharedgroupid" title="SharedGroupId">SharedGroupId</a>: <i>Integer</i>
    <a href="#sharedwithgroupid" title="SharedWithGroupId">SharedWithGroupId</a>: <i>Integer</i>
    <a href="#accesslevel" title="AccessLevel">AccessLevel</a>: <i>String</i>
</pre>

## Properties

#### SharedGroupId

ID of the group which should be shared, i.e. the group to which access is being granted

_Required_: Yes

_Type_: Integer

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SharedWithGroupId

ID of the group to share with, i.e. the group being given access to another group

_Required_: Yes

_Type_: Integer

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AccessLevel

The access level to grant to the shared-with group for acessing the shared group, e.g. 'Guest', 'Developer', or 'Maintainer'. Note the GitLab API may not allow all values.

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

