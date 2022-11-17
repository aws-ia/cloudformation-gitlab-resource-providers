# GitLab::Code::Tag

Creates a tag against a code ref in GitLab

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "GitLab::Code::Tag",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#projectid" title="ProjectId">ProjectId</a>" : <i>Integer</i>,
        "<a href="#ref" title="Ref">Ref</a>" : <i>String</i>,
        "<a href="#message" title="Message">Message</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: GitLab::Code::Tag
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#projectid" title="ProjectId">ProjectId</a>: <i>Integer</i>
    <a href="#ref" title="Ref">Ref</a>: <i>String</i>
    <a href="#message" title="Message">Message</a>: <i>String</i>
</pre>

## Properties

#### Name

The name of the tag to create

_Required_: Yes

_Type_: String

_Maximum Length_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ProjectId

The ID of the project which will be tagged

_Required_: Yes

_Type_: Integer

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Ref

The reference to the code commit to be tagged, either a commit SHA ID or a branch name (to use the commit which is head of that branch at time of tag creation)

_Required_: Yes

_Type_: String

_Maximum Length_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Message

A message to attach to the tag

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### TagId

A CloudFormation ID to identify this tag

#### CommitId

The actual commit SHA ID referenced by this tag, set by the resource provider. This will be equal to Ref if Ref is a commit SHA ID, or set by the provider to point at the commit if Ref is a branch name.

