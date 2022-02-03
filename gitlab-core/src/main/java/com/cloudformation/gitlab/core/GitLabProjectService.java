package com.cloudformation.gitlab.core;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Project;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public class GitLabProjectService implements GitLabService<Project> {

    GitLabApi gitLabApi;

    public GitLabProjectService(String url, String token) {
        gitLabApi = new GitLabApi(url, token);
    }

    @Override
    public Optional<Project> create(Map<String,Object> data){
        Optional<Project> project = Optional.empty();
        try {
            if (!Objects.isNull(data.get("name"))){
                Project projectSpec = new Project()
                        .withName((String) data.get("name"))
                        .withIssuesEnabled(true)
                        .withMergeRequestsEnabled(true)
                        .withWikiEnabled(true)
                        .withSnippetsEnabled(true)
                        .withPublic(true);
                project = Optional.of(gitLabApi.getProjectApi().createProject(projectSpec));
            }
        } catch (Exception e){
            throw new GitLabServiceException("Error creating project: " + data.get("name"), e);
        }
        return project;
    }

    @Override
    public void delete(Integer id){
        try {
            gitLabApi.getProjectApi().deleteProject(id);
        } catch (Exception e){
            throw new GitLabServiceException("Error deleting project: " + id, e);
        }
    }

    @Override
    public void update(Map<String,Object> data){
        try{
            Project project = getById((int) data.get("id")).get();
            if (!((Objects.isNull(project)) ||
                    Objects.isNull(data.get("name")) || data.get("name").equals(project.getName()))){
                String newName = (String) data.get("name");

                // update name
                project.setName(newName);
                // set path
                project.setPath(newName);
                // set path with namespace
                project.setPathWithNamespace(project.getNamespace().getName() + '/' + newName);
                // update URL to repo
                String httpRepo = project.getHttpUrlToRepo();
                project.setHttpUrlToRepo(httpRepo.substring(0,httpRepo.lastIndexOf('/')+1) + newName + ".git");
                // update SSH URL to repo
                String sshRepo = project.getSshUrlToRepo();
                project.setSshUrlToRepo(sshRepo.substring(0,sshRepo.lastIndexOf('/')+1) + newName + ".git");
                // update web url
                String webUrl = project.getWebUrl();
                project.setWebUrl(webUrl.substring(0,webUrl.lastIndexOf('/')+1) + newName);

                // update the project live
                gitLabApi.getProjectApi().updateProject(project);
            }

        } catch (Exception e){
            throw new GitLabServiceException("Error updating project: " + data.get("id"), e);
        }
    }

    @Override
    public Optional<Project> read(Integer id){
        Optional<Project> project = Optional.empty();
        try{
            Project item = gitLabApi.getProjectApi().getProject(id);
            project = Optional.of(item);
        } catch (Exception e){
            throw new GitLabServiceException("Error reading project: " + id, e);
        }
        return project;
    }

    @Override
    public Optional<List<Project>> list(){
        Optional<List<Project>> projects = Optional.empty();
        try {
            List<Project> prjs = gitLabApi.getProjectApi().getOwnedProjects();
            projects = Optional.of(prjs);
        } catch (Exception e){
            throw new GitLabServiceException("Error listing projects:", e);
        }
        return projects;
    }

    @Override
    public Optional<Project> getById(Integer id) {
        try {
            return  Optional.of(gitLabApi.getProjectApi().getProject(id));
        } catch (GitLabApiException e) {
            throw new GitLabServiceException("Error retrieving Project with id" + id, e);
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
