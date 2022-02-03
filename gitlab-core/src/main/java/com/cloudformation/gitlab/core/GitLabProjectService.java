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
            e.printStackTrace();
            throw new GitLabServiceException("Error creating project: " + data.get("name"), e);
        }
        return project;
    }

    @Override
    public Optional<Project> delete(Integer id){
        //Optional<Project> project = Optional.of(item);
        Optional<Project> project;
        try {
            //if (!Objects.isNull(item.getId())) {
                gitLabApi.getProjectApi().deleteProject(id);
                project = Optional.empty();
            //}
        } catch (Exception e){
            //e.printStackTrace();
            throw new GitLabServiceException("Error deleting project: " + id, e);
        }
        return project;

    }

    @Override
    public Optional<Project> update(Project item, Map<String,Object> data){
        Optional<Project> project = Optional.empty();
        try{
            if (!(Objects.isNull(data.get("name")) ||
                    data.get("name").equals(item.getName()))){
                String newName = (String) data.get("name");

                // update name
                item.setName(newName);
                // set path
                item.setPath(newName);
                // set path with namespace
                item.setPathWithNamespace(item.getNamespace().getName() + '/' + newName);
                // update URL to repo
                String httpRepo = item.getHttpUrlToRepo();
                item.setHttpUrlToRepo(httpRepo.substring(0,httpRepo.lastIndexOf('/')+1) + newName + ".git");
                // update SSH URL to repo
                String sshRepo = item.getSshUrlToRepo();
                item.setSshUrlToRepo(sshRepo.substring(0,sshRepo.lastIndexOf('/')+1) + newName + ".git");
                // update web url
                String webUrl = item.getWebUrl();
                item.setWebUrl(webUrl.substring(0,webUrl.lastIndexOf('/')+1) + newName);

                // update the project live
                gitLabApi.getProjectApi().updateProject(item);
                project = Optional.of(item);
            }

        } catch (Exception e){
            //e.printStackTrace();
            throw new GitLabServiceException("Error updating project: " + item.getName(), e);
        }
        return project;
    }

    @Override
    public Optional<Project> read(Project item){
        Optional<Project> project = Optional.empty();
        try{
            item = gitLabApi.getProjectApi().getProject(item.getId());
            project = Optional.of(item);
        } catch (Exception e){
            e.printStackTrace();
            throw new GitLabServiceException("Error reading project: " + item.getName(), e);
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
            e.printStackTrace();
            throw new GitLabServiceException("Error listing projects.", e);
        }
        return projects;
    }

    @Override
    public Optional<Project> getById(Integer id) {
        try {
            //Optional<Project> optProject = gitLabApi.getProjectApi().getProject(id);
            return  Optional.of(gitLabApi.getProjectApi().getProject(id));
        } catch (GitLabApiException e) {
            e.printStackTrace();
            // Throws  404 -> Optional.empty
            // anythign else throw GitLabServiceException("Error reteiving Group with id" + id, e);
        }
        return Optional.empty();
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
