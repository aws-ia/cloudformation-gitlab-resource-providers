package com.gitlab.aws.cfn.resources.shared;

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
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
    public final static String GITLAB_PROPS_FILE = ".gitlab_cfn_tests";
    public final static String GITLAB_LEGACY_FILE_PREFIX = GITLAB_PROPS_FILE + File.separator;

    public static String getEnvOrFile(String label, String description) {
        String envVar = GITLAB_ENV_PREFIX+label.toUpperCase();

        String token = System.getenv(envVar);
        if (!Strings.isNullOrEmpty(token)) return token;

        String path = "";
        do {
            File f = new File(path + GITLAB_PROPS_FILE);
            if (f.exists()) {
                String canonical = null;
                try {
                    canonical = f.getCanonicalPath();
                } catch (IOException e) {
                    throw new IllegalStateException("Test requires either env var "+envVar+" or properties file "+GITLAB_PROPS_FILE+" containing "+envVar+"; but path is illegal: "+e, e);
                }
                if (f.isDirectory()) {
                    // legacy syntax
                    LOG.warn("Detected legacy individual files in folder "+canonical+"; make that be a properties file rather than a folder.");

                    File f2 = new File(path + GITLAB_PROPS_FILE + File.separator + label.toLowerCase());
                    if (f2.exists()) {
                        try {
                            token = Files.readAllLines(f2.toPath()).stream().collect(Collectors.joining()).trim();
                            if (!Strings.isNullOrEmpty(token.toString())) {
                                return token;
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    // new preferred syntax, properties file
                    Properties props = new Properties();
                    try {
                        props.load(new FileReader(f));
                    } catch (IOException e) {
                        throw new IllegalStateException("Test requires either env var "+envVar+" or properties file "+GITLAB_PROPS_FILE+" containing "+envVar+"; but invalid properties in "+canonical+": "+e, e);
                    }
                    Object tokenO = props.get(envVar);
                    if (tokenO!=null && !Strings.isNullOrEmpty(tokenO.toString())) {
                        return tokenO.toString();
                    }
                }
            }

            if (path.length()>128) {
                throw new IllegalStateException("Test requires either env var "+envVar+" or properties file "+GITLAB_PROPS_FILE+" containing "+envVar+" ("+description+")");
            }
            path = ".." + File.separator + path;
        } while (true);
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
