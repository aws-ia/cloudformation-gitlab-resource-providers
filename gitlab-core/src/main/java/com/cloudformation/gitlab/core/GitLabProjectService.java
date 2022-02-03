package com.cloudformation.gitlab.core;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GitLabProjectService implements GitLabService<Project, Map<String,Object>> {

    GitLabApi gitLabApi;

    public GitLabProjectService(String url, String token) {
        gitLabApi = new GitLabApi(url, token);
    }

    @Override
    public Project create(Map<String,Object> data){
        if (!data.containsKey("name")){
            throw new GitLabServiceException("Name is needed to create a project!");
        }
        Project projectSpec = new Project()
                .withName((String) data.get("name"))
                .withIssuesEnabled(true)
                .withMergeRequestsEnabled(true)
                .withWikiEnabled(true)
                .withSnippetsEnabled(true)
                .withPublic(true);
        try {
            return gitLabApi.getProjectApi().createProject(projectSpec);
        } catch (Exception e){
            throw new GitLabServiceException("Error creating project: " + data.get("name"), e);
        }
    }

    @Override
    public void deleteById(Integer id){
        try {
            gitLabApi.getProjectApi().deleteProject(id);
        } catch (Exception e){
            throw new GitLabServiceException("Error deleting project: " + id, e);
        }
    }

    @Override
    public void update(Map<String,Object> data){

        if(!data.containsKey("id")) {
            throw new GitLabServiceException("No Project id was specified!");
        }
        Optional<Project> projectFromApi = getById((Integer)data.get("id"));
        Project item  = projectFromApi.orElseThrow( () -> new GitLabServiceException("Project to update does not exist!"));

        if (!data.containsKey("name")){
            throw new GitLabServiceException("A project cannot be updated to not have a name!");
        }
        String newName = (String) data.get("name");
        if(newName != null && !newName.isEmpty() &&  !newName.equals(item.getName())) {
            item.setName(newName);
            item.setPath(newName);
            item.setPathWithNamespace(item.getNamespace().getName() + '/' + newName);
            String httpRepo = item.getHttpUrlToRepo();
            item.setHttpUrlToRepo(httpRepo.substring(0, httpRepo.lastIndexOf('/') + 1) + newName + ".git");
            String sshRepo = item.getSshUrlToRepo();
            item.setSshUrlToRepo(sshRepo.substring(0, sshRepo.lastIndexOf('/') + 1) + newName + ".git");
            String webUrl = item.getWebUrl();
            item.setWebUrl(webUrl.substring(0, webUrl.lastIndexOf('/') + 1) + newName);
        }
        try{
            gitLabApi.getProjectApi().updateProject(item);
        } catch (Exception e){
            throw new GitLabServiceException("Error updating project: " + item.getName(), e);
        }
    }

    @Override
    public Optional<Project> getById(Integer id){
        try {
            return Optional.of(gitLabApi.getProjectApi().getProject(id));
        } catch (GitLabApiException e){
             if(e.getHttpStatus() == 404) {
                 return Optional.empty();
             }
            throw new GitLabServiceException("Error reading project: " + id, e);
        }
    }

    @Override
    public List<Project> list() {
        try {
            return  gitLabApi.getProjectApi().getOwnedProjects();
        } catch (GitLabApiException e) {
            throw new GitLabServiceException("Error listing projects.", e);
        }
    }

    @Override
    public boolean verifyConnection(){
        try{
            gitLabApi.getUserApi().getCurrentUser();
        } catch(Exception e){
            return false;
        }
        return true;
    }

}
