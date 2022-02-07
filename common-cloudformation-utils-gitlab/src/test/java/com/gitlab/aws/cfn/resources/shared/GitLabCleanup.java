package com.gitlab.aws.cfn.resources.shared;

import java.util.ArrayList;
import java.util.List;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Project;
import org.slf4j.LoggerFactory;

public class GitLabCleanup {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GitLabCleanup.class);

    public static void main(String[] args) throws GitLabApiException {
        LOG.info("Cleaning up GitLab test resources...");

        int MAX = 10;

        // clean up
        GitLabApi gitlab = new GitLabApi("https://gitlab.com", GitLabLiveTestSupport.getAccessTokenForTests());

        boolean firstGroup = true;
        List<Group> groups = new ArrayList<>(gitlab.getGroupApi().getGroups(MAX).current());
        while (!groups.isEmpty()) {
            Group x = groups.remove(0);
            if (x.getName().startsWith(GitLabLiveTestSupport.TEST_PREFIX)) {
                LOG.info("Deleting leaked test item: "+x.getName()+" "+x);
                gitlab.getGroupApi().deleteGroup(x.getId());
            }
            if (firstGroup) {
                // descend into the first group because that's usually where we create them
                groups.addAll(gitlab.getGroupApi().getSubGroups(x.getId()));
                firstGroup = false;
            }
        }

        for (Project x : gitlab.getProjectApi().getOwnedProjects(MAX).current()) {
            if (x.getName().startsWith(GitLabLiveTestSupport.TEST_PREFIX)) {
                LOG.info("Deleting leaked test item: "+x.getName()+" "+x);
                gitlab.getProjectApi().deleteProject(x.getId());
            }
        }
    }
}
