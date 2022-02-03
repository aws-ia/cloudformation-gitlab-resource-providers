package com.cloudformation.gitlab.core;

public class GitLabServiceException extends RuntimeException{
    private static final long serialVersionUID = 122345678;

    public GitLabServiceException(String message) {
        super(message);
    }

    public GitLabServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
