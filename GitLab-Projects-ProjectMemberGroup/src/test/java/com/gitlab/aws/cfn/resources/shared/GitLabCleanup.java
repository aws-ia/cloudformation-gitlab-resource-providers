package com.gitlab.aws.cfn.resources.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Tag("Live")
public class GitLabCleanup {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GitLabCleanup.class);


    public static void main(String[] args) throws GitLabApiException {
        int MAX = 10;

        // clean up
        GitLabApi gitlab = new GitLabApi("https://gitlab.com", GitLabLiveTestSupport.getAccessTokenForTests());
        for (Group x : gitlab.getGroupApi().getGroups(MAX).current()) {
            if (x.getName().startsWith(GitLabLiveTestSupport.TEST_PREFIX)) {
                LOG.info("Deleting leaked test item: "+x.getName()+" "+x);
                gitlab.getGroupApi().deleteGroup(x.getId());
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
