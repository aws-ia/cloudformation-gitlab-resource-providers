package com.gitlab.aws.cfn.resources.shared;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingTestWatcher implements TestWatcher, BeforeAllCallback, BeforeEachCallback, AfterAllCallback {
    private static final Logger LOG = LoggerFactory.getLogger("JUnit");

    AtomicInteger countStarted = new AtomicInteger();
    AtomicInteger countPassed = new AtomicInteger();
    AtomicInteger countFailed = new AtomicInteger();

    AtomicInteger countStartedClass = new AtomicInteger();
    AtomicInteger countPassedClass = new AtomicInteger();
    AtomicInteger countFailedClass = new AtomicInteger();

    protected String nameForTestClass(ExtensionContext extensionContext) {
        return extensionContext.getTestClass().map(Class::getName).orElse("<unnamed>");
    }

    protected String nameForTest(ExtensionContext extensionContext) {
        Optional<String> methodName = extensionContext.getTestMethod().map(Method::getName);
        if (methodName.isPresent()) {
            if (extensionContext.getDisplayName().startsWith(methodName.get())) return nameForTestClass(extensionContext)+"."+extensionContext.getDisplayName();
            else return nameForTestClass(extensionContext)+"."+methodName.get()+" ("+extensionContext.getDisplayName()+")";
        }

        return extensionContext.getDisplayName();
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        LOG.info(nameForTestClass(extensionContext)+" - test class starting");
        countStartedClass.set(0);
        countPassedClass.set(0);
        countFailedClass.set(0);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        LOG.info(nameForTestClass(extensionContext)+" - test class finished, count passed: "+countPassedClass.get()+" / "+countStartedClass.get()+" here, "+countPassed.get()+" / "+countStarted.get()+" overall");
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        LOG.info(nameForTest(extensionContext)+" - test starting");
        countStarted.incrementAndGet();
        countStartedClass.incrementAndGet();
    }

    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> optional) {
        LOG.info(nameForTest(extensionContext)+" - test skipped (disabled)");
    }

    @Override
    public void testSuccessful(ExtensionContext extensionContext) {
        LOG.info(nameForTest(extensionContext)+" - test PASSED");
        countPassed.incrementAndGet();
        countPassedClass.incrementAndGet();
    }

    @Override
    public void testAborted(ExtensionContext extensionContext, Throwable throwable) {
        LOG.info(nameForTest(extensionContext)+" - test FAILED with error - "+throwable);
        countFailed.incrementAndGet();
        countFailedClass.incrementAndGet();
    }

    @Override
    public void testFailed(ExtensionContext extensionContext, Throwable throwable) {
        LOG.info(nameForTest(extensionContext)+" - test FAILED assertions - "+throwable);
        countFailed.incrementAndGet();
        countFailedClass.incrementAndGet();
    }
}
