package com.gitlab.aws.cfn.resources.shared;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.FailableRunnable;
import org.gitlab4j.api.GitLabApi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestWatcher;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(LoggingTestWatcher.class)
public class GitLabLiveTestSupport {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GitLabLiveTestSupport.class);

    public final static String TEST_PREFIX = "cfn-test";

    public final static String GITLAB_ENV_PREFIX = "GITLAB_CFN_TESTS_";
    public final static String GITLAB_FILE_PREFIX = ".gitlab_cfn_tests" + File.separator;

    public static String getEnvOrFile(String label, String description) {
        String envVar = GITLAB_ENV_PREFIX+label.toUpperCase();

        String token = System.getenv(envVar);
        if (!Strings.isNullOrEmpty(token)) return token;

        String fileName = GITLAB_FILE_PREFIX+label.toLowerCase();

        // use files, from CWD then HOME
        File f = new File(fileName);
        if (f.exists()) {
            try {
                token = Files.readAllLines(f.toPath()).stream().collect(Collectors.joining()).trim();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!Strings.isNullOrEmpty(token)) return token;
        }

        f = new File(System.getProperty("user.home")+File.separator+fileName);
        if (f.exists()) {
            try {
                token = Files.readAllLines(f.toPath()).stream().collect(Collectors.joining()).trim();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!Strings.isNullOrEmpty(token)) return token;
        }

        throw new IllegalStateException("Test requires either env var "+envVar+" or file "+fileName+" containing "+description);
    }

    public static String getAccessTokenForTests() {
        return getEnvOrFile("access_token", "GitLab personal access token");
    }

    protected GitLabApi gitlab;

    @BeforeAll
    public void initGitLabApi() {
        gitlab = new GitLabApi(AbstractGitlabCombinedResourceHandler.DEFAULT_URL, getAccessTokenForTests());
    }

    public void assertSoon(FailableRunnable body) {
        assertWithinSeconds(15, body);
    }

    public void assertWithinSeconds(int numSeconds, FailableRunnable body) {
        long end = System.currentTimeMillis() + numSeconds*1000;
        while (true) {
            try {
                body.run();
                return;
            } catch (Throwable e) {
                if (end < System.currentTimeMillis()) {
                    if (e instanceof RuntimeException) throw (RuntimeException)e;
                    if (e instanceof Error) throw (Error)e;
                    throw new RuntimeException(e);
                }
                try {
                    LOG.debug("Assertion failed, but will retry after delay ("+body+")");
                    Thread.sleep(50);
                } catch (InterruptedException e2) {
                    throw new RuntimeException(e2);
                }
            }
        }
    }
}
