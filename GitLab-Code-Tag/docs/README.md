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

_Maximum_: <code>64</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ProjectId

The ID of the project which will be tagged

_Required_: Yes

_Type_: Integer

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Ref

The code reference to tag, either a branch or a commit SHA

_Required_: Yes

_Type_: String

_Maximum_: <code>64</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Message

A message to attach to the tag

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the TagId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### TagId

A CloudFormation ID to identify this tag

