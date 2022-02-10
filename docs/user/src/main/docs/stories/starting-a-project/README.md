# Setting up a new project with correct users and groups

The following CloudFormation GitLab resource types allow creating projects and control users and groups of users who should be able to access them.
This allows kicking off new projects and managing users responsible for the project simply and efficiently.

We will use the following types:
* `GitLab::Projects::Project` - to create and manage projects in GitLab
* `GitLab::Groups::Group` - to create and manage groups
* `GitLab::Groups::UserMemberOfGroup` - to specify particular members for created groups
* `GitLab::Projects::UserMemberOfProject` - to control which users can access particular projects
* `GitLab::Projects::GroupAccessToProject` - to control access of groups to a project

Typically GitLab requires that one group already exist, and new groups will be added to it. Find or create a parent group to use in the GitLab UI, and make a note of its numeric ID. This will be the parameter `ParentGroupId`:
```
Parameters:
  ParentGroupId:
    Type: Number
    Default: xxx
    Description: Enter the ID of an existing group, e.g. for a "sample-company", where new groups will be created
```

We can now define the set of projects required. Imagine our project consists of a separate front-end and back-end projects, which we can define as follows:
```
  FrontEnd:
    Type: GitLab::Projects::Project
    Properties:
      Name: FrontEndProject
  BackEnd:
    Type: GitLab::Projects::Project
    Properties:
      Name: BackEndProject
```

We can then create groups of users with particular roles, here we will define groups: `ProjectManagers`, `FrontEndDevelopers` and `BackEndDevelopers`.
We also create a group `ProjectX` as a parent group to the ones specified above:
```
  ProjectX:
    Type: GitLab::Groups::Group
    Properties:
      Name: ProjectX
      ParentId: { Ref: ParentGroupId }
      Path: path-to-project-x
  ProjectManagers:
    Type: GitLab::Groups::Group
    Properties:
      Name: ProjectManagers
      ParentId: { Ref: ProjectX }
      Path: path-to-project-managers
  FrontEndDevelopers:
    Type: GitLab::Groups::Group
    Properties:
      Name: FrontEndDevelopers
      ParentId: { Ref: ProjectX }
      Path: path-to-frontend-developers
  BackEndDevelopers:
    Type: GitLab::Groups::Group
    Properties:
      Name: BackEndDevelopers
      ParentId: { Ref: ProjectX }
      Path: path-to-backend-developers
```

We can now add users to the groups created.
Note that we have admins, front and back end developers, but we also have a full stack developer who belongs to both front and back end groups:
```
  ProjectManagerMemberOfGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: ProjectManagers }
      Username: cloudsoft_admin_geralt
      AccessLevel: Maintainer
  FrontEndDeveloperMemberOfGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: FrontEndDevelopers }
      Username: cloudsoft_developer_ciri
      AccessLevel: Developer
  BackEndDeveloperMemberOfGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: {Ref: BackEndDevelopers }
      Username: cloudsoft_developer_jaskier
      AccessLevel: Developer
  FullStackDeveloperMemberOfFrontEndGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: { Ref: FrontEndDevelopers }
      Username: cloudsoft_developer_yennefer
      AccessLevel: Developer
  FullStackDeveloperMemberOfBackEndGroup:
    Type: GitLab::Groups::UserMemberOfGroup
    Properties:
      GroupId: { Ref: BackEndDevelopers }
      Username: cloudsoft_developer_yennefer
      AccessLevel: Developer
```

With groups populated, we can associate relevant groups with the projects.
We will use the `ProjectManagers` group for both projects with AccessLevel `Maintainer`
and relevant developers for each of the projects (with AccessLevel `Developer`):
```
  ManagersInFrontEndProject:
    Type: GitLab::Projects::GroupAccessToProject
    Properties:
      ProjectId: { Ref: FrontEnd }
      GroupId: { Ref: ProjectManagers }
      AccessLevel: Maintainer
  ManagersInBackEndProject:
    Type: GitLab::Projects::GroupAccessToProject
    Properties:
      ProjectId: { Ref: BackEnd }
      GroupId: { Ref: ProjectManagers }
      AccessLevel: Maintainer
  DevelopersInFrontEndProject:
    Type: GitLab::Projects::GroupAccessToProject
    Properties:
      ProjectId: {Ref: FrontEnd }
      GroupId: {Ref: FrontEndDevelopers}
      AccessLevel: Developer
  DevelopersInBackEndProject:
    Type: GitLab::Projects::GroupAccessToProject
    Properties:
      ProjectId: { Ref: BackEnd }
      GroupId: { Ref: BackEndDevelopers }
      AccessLevel: Developer
```

This is a tough project that needs an extra pair of hands, we want to pull a user from another team but don't necessarily need to add them to any group.
We can add users to a project directly as follows:
```
  DeveloperVesemirInFrontEndProject:
    Type: GitLab::Projects::UserMemberOfProject
    Properties:
      ProjectId: { Ref: FrontEnd }
      Username: cloudsoft_developer_vesemir
      AccessLevel: Developer
```

As a result, we have a structure of our `ProjectX`, with 2 projects created:
![Projects](projects.png)

Our `FrontEnd` project has the developer added directly to project:
![Front End Project Members](project_members.png)

Also each of the projects has 2 groups added: `ProjectManagers` and `Front/BackEndDevelopers` respectively.
![Front End Project Groups](project_groups.png)

In terms of groups, we have our parent `ProjectX` group wigit th the subgroups as specified above:
![Main Group with Subgroups](subgroups.png)

Each subgroup will have the relevant users (`Developers` and `Maintainer`) present as members.