package com.gitlab.aws.cfn.resources.shared;

import java.io.PrintWriter;
import java.io.StringWriter;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;

public interface HandlerMixins<ResourceModel, CallbackContext> {

    ResourceModel getModel();

    default ProgressEvent<ResourceModel, CallbackContext> success() {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(getModel())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    default ProgressEvent<ResourceModel, CallbackContext> success(String message) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(getModel())
                .status(OperationStatus.SUCCESS)
                .message(message)
                .build();
    }

    default ProgressEvent<ResourceModel, CallbackContext> failure(HandlerErrorCode code) {
        return failure(code, null);
    }

    default ProgressEvent<ResourceModel, CallbackContext> failure(String message) {
        return failure(HandlerErrorCode.GeneralServiceException, message);
    }

    default ProgressEvent<ResourceModel, CallbackContext> failure(Throwable exception) {
        // include stack traces
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return failure(sw.getBuffer().toString());

        //return failure(""+exception);
    }

    default ProgressEvent<ResourceModel, CallbackContext> failure(HandlerErrorCode code, String message) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(getModel())
                .status(OperationStatus.FAILED)
                .errorCode(code)
                .message(message)
                .build();
    }

    class FailureToSetInResult extends RuntimeException {
        private static final long serialVersionUID = 1L;

        final ProgressEvent<?,?> result;
        private FailureToSetInResult(ProgressEvent<?,?> result) {
            this.result = result;
        }
        public ProgressEvent<?,?> getResult() {
            return result;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()+"["+result.getErrorCode()+"]: "+result.getMessage();
        }
    }

    default FailureToSetInResult fail(ProgressEvent<ResourceModel, CallbackContext> failure) {
        throw new FailureToSetInResult(failure);
    }

    default FailureToSetInResult fail(Throwable exception) {
        return fail(failure(exception));
    }
    default FailureToSetInResult failNotFound() { return fail(failure(HandlerErrorCode.NotFound, "The requested resource cannot be found.")); }
    default FailureToSetInResult fail(HandlerErrorCode code, String message) { return fail(failure(code, message)); }

}
