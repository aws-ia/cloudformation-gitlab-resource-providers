package com.gitlab.aws.cfn.resources.projects.member.group;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.Pager;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
