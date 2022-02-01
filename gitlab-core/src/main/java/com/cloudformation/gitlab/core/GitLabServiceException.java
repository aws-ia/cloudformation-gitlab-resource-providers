package com.cloudformation.gitlab.core;

public class GitLabServiceException extends RuntimeException{
    public GitLabServiceException(String message) {
        super(message);
    }

    public GitLabServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
