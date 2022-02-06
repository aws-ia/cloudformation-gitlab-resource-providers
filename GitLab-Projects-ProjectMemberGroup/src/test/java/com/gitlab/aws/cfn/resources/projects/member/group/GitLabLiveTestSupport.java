package com.gitlab.aws.cfn.resources.projects.member.group;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

public class GitLabLiveTestSupport {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GitLabLiveTestSupport.class);

    public final static String TEST_PREFIX = "cfn-test";

    public final static String GITLAB_ACCESS_TOKEN_ENV = "GITLAB_ACCESS_TOKEN_CFN_TESTS";
    public final static String GITLAB_ACCESS_TOKEN_FILE = ".gitlab_access_token_cfn_tests";

    public static String getAccessTokenForTests() {
        String token = System.getenv(GITLAB_ACCESS_TOKEN_ENV);
        if (!Strings.isNullOrEmpty(token)) return token;
        File f = new File(System.getProperty("user.home")+File.separator+GITLAB_ACCESS_TOKEN_FILE);
        if (f.exists()) {
            try {
                token = Files.readAllLines(f.toPath()).stream().collect(Collectors.joining()).trim();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!Strings.isNullOrEmpty(token)) return token;
        }
        throw new IllegalStateException("Test requires either env var "+GITLAB_ACCESS_TOKEN_ENV+" or file "+GITLAB_ACCESS_TOKEN_FILE+" containing personal access token");
    }

}
