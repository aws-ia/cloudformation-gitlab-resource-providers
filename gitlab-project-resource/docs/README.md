# CloudFormation::GitLab::Project

Gitlab Project Management

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "CloudFormation::GitLab::Project",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#id" title="ID">ID</a>" : <i>Integer</i>,
        "<a href="#token" title="Token">Token</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: CloudFormation::GitLab::Project
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#id" title="ID">ID</a>: <i>Integer</i>
    <a href="#token" title="Token">Token</a>: <i>String</i>
</pre>

## Properties

#### Name

The name of the project

_Required_: Yes

_Type_: String

_Maximum_: <code>64</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ID

The ID of the project

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Token

Authentication Token

_Required_: No

_Type_: String

_Maximum_: <code>64</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ID.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Server

GitLab Server address

