package com.pecacm.backend.constants;

public class ErrorConstants {
    public static final String USER_NOT_FOUND = "User with provided email does not exist";
    public static final String USER_UNAUTHORIZED = "User Unauthorized";
    public static final String QUESTION_NOT_FOUND = "Question does not exist with id : ";
    public static final String ANSWER_NOT_FOUND = "Answer does not exist with id : ";
    public static final String QUESTION_NOT_OWNED = "Question can only be modified by the user who asked it";
    public static final String ANSWER_NOT_OWNED = "Answer can only be modified by the user who wrote it";
    public static final String CONTENT_EMPTY = "Content cannot be blank or empty";
    public static final String USER_NOT_VERIFIED = "Only verified ACM members can use this feature, please verify your email first";
    public static final String QUESTION_ALREADY_UPVOTED = "Question has already been upvoted by this user";
    public static final String QUESTION_NOT_UPVOTED = "Question has not been upvoted by this user";
    public static final String QUESTION_ASKED_TOO_SOON = "Please wait before asking another question, only one question can be asked every ";
    public static final String ANSWER_POSTED_TOO_SOON = "Please wait before posting another answer, only one answer can be posted every ";
}
