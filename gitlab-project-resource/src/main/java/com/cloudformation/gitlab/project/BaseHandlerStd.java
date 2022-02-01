package com.cloudformation.gitlab.project;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    @Override
    public abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            Logger logger);

    private GitLabApi gitlabApi;

    protected GitLabApi getGitLabApi(){
        return gitlabApi;
    }

    protected void setGitLabApi (ResourceModel model){
        try{
            if (!(Objects.isNull(model.getServer()) || Objects.isNull(model.getToken()))){
                gitlabApi = new GitLabApi(model.getServer(), model.getToken());
            }
        } catch (Exception e){
            //error
        }
    }

    private List<Project> allProjects = new ArrayList<>();

    protected List<Project> getAllProjects(){
        return allProjects;
    }

    protected Project getProject(ResourceModel model){
        Project project;
        try{
            project = gitlabApi.getProjectApi().getProject(model.getId());
        } catch (Exception e){
            return null;
        }
        return project;
    }

    protected ProgressEvent<ResourceModel, CallbackContext> checkApiConnection(ResourceModel model){
        ProgressEvent<ResourceModel, CallbackContext> failurePe = ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.NetworkFailure)
                .build();
        if (Objects.isNull(gitlabApi)) return failurePe;
        try {
            gitlabApi.getUserApi().getCurrentUser();
        } catch(Exception e){
            return failurePe;
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> checkNameSupplied(ResourceModel model){
        // check name var
        if (Objects.isNull(model.getName())) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.InvalidRequest)
                    .build();
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> fetchAllProjects(ResourceModel model){
        try {
            allProjects = gitlabApi.getProjectApi().getOwnedProjects();
        } catch (Exception e) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.InternalFailure)
                    .build();
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> checkProjectExists(ResourceModel model){
        final AtomicBoolean projectsAlreadyExists = new AtomicBoolean(false);
        allProjects.forEach(project -> {
            if (project.getId().equals(model.getId())) {
                projectsAlreadyExists.set(true);
            }
        });
        if (projectsAlreadyExists.get()){
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.SUCCESS)
                    .build();
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.NotFound)
                .build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> getProjectSummary(ResourceModel model){
        Project project;
        try {
            project = getProject(model);
        } catch (Exception e){
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotFound)
                    .build();
        }

        final ResourceModel actualModel = ResourceModel.builder()
                .id(project.getId())
                .name(project.getName())
                .server(gitlabApi.getGitLabServerUrl())
                .token(gitlabApi.getAuthToken())
                .build();

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(actualModel)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> createProject(ResourceModel model){
        try{
            Project projectSpec = new Project()
                    .withName(model.getName())
                    .withIssuesEnabled(true)
                    .withMergeRequestsEnabled(true)
                    .withWikiEnabled(true)
                    .withSnippetsEnabled(true)
                    .withPublic(true);
            Project newProject = gitlabApi.getProjectApi().createProject(projectSpec);
            model.setId(newProject.getId());
        } catch (Exception e){
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.InternalFailure)
                    .build();
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> deleteProject(ResourceModel model){
        try{
            gitlabApi.getProjectApi().deleteProject(model.getId());
        } catch (Exception e){
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.InternalFailure)
                    .build();
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> updateProject(ResourceModel model){
        try{
            Project project = gitlabApi.getProjectApi().getProject(model.getId());
            if (!(Objects.isNull(model.getName()) ||
                    project.getName().equals(model.getName()))){
                // update name
                project.setName(model.getName());
                // set path
                project.setPath(model.getName());
                // set path with namespace
                project.setPathWithNamespace(project.getNamespace().getName() + '/' + model.getName());
                // update URL to repo
                String httpRepo = project.getHttpUrlToRepo();
                project.setHttpUrlToRepo(httpRepo.substring(0,httpRepo.lastIndexOf('/')+1) + model.getName() + ".git");
                // update SSH URL to repo
                String sshRepo = project.getSshUrlToRepo();
                project.setSshUrlToRepo(sshRepo.substring(0,sshRepo.lastIndexOf('/')+1) + model.getName() + ".git");
                // update web url
                String webUrl = project.getWebUrl();
                project.setWebUrl(webUrl.substring(0,webUrl.lastIndexOf('/')+1) + model.getName());

                // update the project live
                gitlabApi.getProjectApi().updateProject(project);
            }
        } catch (Exception e){
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.InternalFailure)
                    .build();
        }

        // update api (if needed)
        try{
            Project project = gitlabApi.getProjectApi().getProject(model.getId());
            if (!(Objects.isNull(model.getServer()) || (gitlabApi.getGitLabServerUrl().equals(model.getServer()))) ||
                    !(Objects.isNull(model.getToken()) || (gitlabApi.getAuthToken().equals(model.getToken())))){
                // if either server or the token changed, reset the api connection
                setGitLabApi(model);
            }
        } catch (Exception e) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NetworkFailure)
                    .build();
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }


    protected ResourceModel translateProjectToResourceModel(Project project, GitLabApi gitlabApi){
        return ResourceModel.builder()
                .name(project.getName())
                .server(gitlabApi.getGitLabServerUrl())
                .token(gitlabApi.getAuthToken())
                .id(project.getId())
                .build();
    }

}
