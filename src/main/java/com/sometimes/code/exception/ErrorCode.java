package com.sometimes.code.exception;

public enum ErrorCode {
    EMPTY_FILE_EXCEPTION("Uploaded file is empty or missing a filename."),
    IO_EXCEPTION_ON_IMAGE_UPLOAD("I/O exception occurred during image upload."),
    NO_FILE_EXTENSION("The file has no extension."),
    INVALID_FILE_EXTENSION("Invalid file extension. Only jpg, jpeg, png, gif are allowed."),
    PUT_OBJECT_EXCEPTION("Exception occurred while putting object to S3."),
    IO_EXCEPTION_ON_IMAGE_DELETE("I/O exception occurred during image deletion."),
    INVALID_URL("Invalid URL provided.");  // 추가된 부분

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
