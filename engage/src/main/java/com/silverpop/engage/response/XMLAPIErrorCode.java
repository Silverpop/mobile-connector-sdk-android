package com.silverpop.engage.response;

/**
 * Created by Lindsay Thurmond on 1/7/15.
 */
public enum XMLAPIErrorCode {

    /**
     * Not defined by the Silverpop API, but is returned when a matching error isn't found.
     */
    UNKNOWN(-1, "Unknown Error"),

    FTF_NON_NUMERIC_MAILING_KEY(1, "F_NON_NUMERIC_MAILING_KEY"),
    FTF_NON_NUMERIC_SENDER_KEY(2, "FTF_NON_NUMERIC_SENDER_KEY"),
    FTF_BAD_MAILING(3, "FTF_BAD_MAILING"),
    FTF_INVALID_EMAIL_ADDRESS(4, "FTF_INVALID_EMAIL_ADDRESS"),
    FTF_INVALID_ENCRYPTED_SENDER_KEY(5, "FTF_INVALID_ENCRYPTED_SENDER_KEY"),
    FTF_INVALID_COMMENT_SIZE(6, "FTF_INVALID_COMMENT_SIZE"),
    SERVER_ERROR(50, "Server Error (typically returned if the API is invoked incorrectly such as no XML passed in the request)"),
    INVALID_XML_REQUEST(51, "Invalid XML Request"),
    MISSING_XML_PARAMETER(52, "Missing XML parameter"),
    PARAM_NOT_PROVIDED(100, "Parameter \"x\" was not provided in API call"),
    NAME_ALREADY_IN_USE(101, "Name already in use. Engage cannot rename the template directory."),
    DIRECTORY_ALREADY_EXISTS(102, "Directory already exists."),
    PARENT_DIRECTORY_DOES_NOT_EXIST(103, "Parent directory does not exist."),
    VISIBILITY_NOT_VALID(104, "Visibility is not valid."),
    LIST_TYPE_NOT_VALID(105, "List type is not valid."),
    LIST_ID_NOT_VALID(106, "List ID is not valid."),
    MAILING_ID_NOT_VALID(107, "Mailing ID is not valid."),
    TRACKING_LEVEL_NOT_VALID(108, "Tracking Level is not valid."),
    ERROR_SAVING_MAILING(109, "Error saving mailing to the database."),
    RETAIN_FLAG_NOT_VALID(110, "Retain flag is not valid."),
    MAILING_TYPE_NOT_VALID(111, "Mailing Type is not valid."),
    CLICK_THROUGH_TYPE_NOT_VALID(112, "Click Through Type is not valid."),
    TEXT_SIZE_NOT_INT(113, "TextSize is not an integer."),
    /**
     * Appears to be a duplicate of {@link #PARAM_NOT_PROVIDED}
     */
    PARAM_NOT_PROVIDED_2(114, "Parameter \"x\" was not provided in API call"),
    /**
     * Appears to be a duplicate of {@link #NAME_ALREADY_IN_USE}
     */
    NAME_ALREADY_IN_USE_2(115, "Name already in use. Engage cannot rename template directory."),
    ERR_INVALID_CREATED_FROM(116, "ERR_INVALID_CREATED_FROM"),
    ERR_INVALID_ALLOW_HTML(117, "ERR_INVALID_ALLOW_HTML"),
    ERR_INVALID_SEND_AUTOREPLY(118, "ERR_INVALID_SEND_AUTOREPLY"),
    ERR_INVALID_UPDATE_IF_FOUND(119, "ERR_INVALID_UPDATE_IF_FOUND"),
    ERROR_SAVING_RECIPIENT(120, "Error saving recipient to the database."),
    ADD_RECIPIENT_EMAIL_REQUIRED(121, "Unable to add recipient. No EMAIL provided."),
    ADD_RECIPIENT_ALREADY_EXISTS(122, "Unable to add recipient. Recipient already exists."),
    UPDATE_RECIPIENT_DOES_NOT_EXIST(123, "Unable to update recipient / recipient does not exist."),
    RECIPIENT_ID_NOT_VALID(124, "Recipient ID is not valid."),
    LIST_ID_OR_MAILING_ID_REQUIRED(125, "No List ID or Mailing ID provided with the Recipient ID."),
    MAILING_DOES_NOT_EXIST(126, "Mailing does not exist."),
    MAILING_DELETED(127, "Mailing deleted."),
    RECIPIENT_NOT_LIST_MEMBER(128, "Recipient is not a member of the list."),
    RECIPIENT_OPTED_OUT(129, "Recipient has opted out of the list."),
    MAILING_INTERNAL_ERROR(130, "Unable to send mailing; Internal error."),
    ERR_INVALID_IMPORT_TYPE(131, "ERR_INVALID_IMPORT_TYPE"),
    IMPORT_JOB_CREATE_ERROR(132, "Unable to create import job."),
    FILE_TYPE_NOT_VALID(133, "File type is not valid."),
    /**
     * Appears to be a duplicate of {@link #FILE_TYPE_NOT_VALID}
     */
    FILE_TYPE_NOT_VALID_2(134, "File type is not valid."),
    JOB_ID_NOT_VALID(135, "Job ID is not valid."),
    DELETE_JOB_INTERNAL_ERROR(136, "Unable to create Delete job. Internal error."),
    DESTROY_MAILING_INTERNAL_ERROR(137, "Unable to destroy mailing. Internal error."),
    REMOVE_RECIPIENT_INTERNAL_ERROR(138, "Unable to remove recipient from list. Internal error."),
    CANNOT_CREATE_DC_RULESET_EXPORT(139, "Unable to create DC ruleset export job."),
    EDITOR_TYPE_NOT_VALID(140, "Editor type is not valid."),
    ENCODING_NOT_VALID(141, "Encoding is not valid."),
    CANNOT_DELETE_LIST_QUERY_RECIPIENTS(143, "List is a query, cannot delete list query recipients."),
    INVALID_SESSION(145, "Session has expired or is invalid."),
    INVALID_LIST_COLUMN_TYPE(146, "Invalid default value for List Column type"),
    INCLUDE_ALL_LISTS_NOT_VALID(147, "Include All Lists is not valid."),
    INVALID_PERMISSIONS(150, "Organization permissions prohibit using this API."),
    ERR_LIST_META_DENIED(151, "ERR_LIST_META_DENIED"),
    CREATE_COLUMNS_INTERNAL_ERROR(152, "Unable to create set column values job. Internal error."),
    ERR_EXPORT_NOT_LIST_COLUMN(153, "ERR_EXPORT_NOT_LIST_COLUMN"),
    ACTION_CODE_NOT_VALID(154, "Action code is not valid."),
    RULESET_DOES_NOT_EXIST(155, "Action code is not valid."),
    CREATE_EXPORT_JOB_INTERNAL_ERROR(156, "Unable to create Export job. Internal error."),
    CUSTOM_MAILING_ID_REQUIRED(160, "Can only send Custom Automated Mailings. Please provide the Mailing ID for a Custom Automated Mailing."),
    COLUMN_NAME_NOT_VALID(161, "COLUMN_NAME is not valid for this list."),
    MAILING_NOT_ACTIVE(162, "Mailing is not active."),
    DELETE_RULESET_SQL_ERROR(170, "SQLException deleting ruleset."),
    DELETE_RULESET_ERROR(171, "Error deleting rule."),
    USAGE_NOT_INTEGER(172, "Usage was not an integer."),
    DYNAMIC_CONTENT_RULESET_SQL_ERROR(173, "SQLException listing Dynamic Content ruleset."),
    DYNAMIC_CONTENT_RULESET_SQL_ERROR_2(174, "SQLException listing Dynamic Content rulesets for list."),
    USER_EXISTS_INTERNAL_ERROR(180, "Unable to check if user exists. Internal error."),
    CANNOT_SCHEDULE_MULTIMATCH_MAILINGS(181, "You cannot schedule Multimatch Mailings through the API."),
    MAILING_NAME_ALREADY_EXISTS(182, "A Mailing with the provided name already exists."),
    MAILING_VALIDATION_ERROR(183, "Errors found validating mailing."),
    DATE_ERROR(184, "Numerous errors related to dates."),
    BEHAVIOR_REPORT_ID_NOT_VALID(185, "Report ID for Behavior is invalid."),
    ERR_INVALID_SENT_MAILING_TYPE(186, "ERR_INVALID_SENT_MAILING_TYPE"),
    RECURSIVE_NOT_VALID(187, "RECURSIVE flag is not valid."),
    LIST_COLUMN_CANNOT_BE_SYSTEM_FIELD(188, "Cannot use a System field name for a List column."),
    CANNOT_LOCATE_ELEMENT(190, "Unable to locate element in the definition. Unable to continue."),
    CREATE_QUERY_NAME_ALREADY_EXISTS(256, "Unable to create query. New List name already exists."),
    RULESET_WITH_NAME_ALREADY_EXISTS(300, "A Ruleset with the provided name already exists."),
    RULESET_WITH_NAME_DOES_NOT_EXIST(301, "A Ruleset with the provided name does not exist."),
    LIST_ID_NOT_INTEGER(310, "Invalid value for Element: LIST_ID. Not an integer. Value: 'x'"),
    CREATE_RECIPIENT_DATA_JOB_INTERNAL_ERROR(311, "Unable to create Recipient Data Job. Internal error."),
    LIST_TYPE_NOT_VALID_2(312, "List is not the right type for this API."),
    COLUMN_TYPE_NOT_VALID(313, "Column is not the right type for this API."),
    COLUMN_NOT_FOUND(314, "Column 'x' not found in list."),
    RECIPIENT_OPT_OUT_INTERNAL_ERROR(315, "Unable to opt out recipient from list. Internal error."),
    COLUMN_NAME_REQUIRED(316, "Invalid XML in request: COLUMN Element found without a NAME."),
    MAILING_ID_AND_LIST_ID_SET(320, "Both MAILING_ID and LIST_ID provided. Please pick only one."),
    EXPORT_FORMAT_NOT_VALID(321, "Export Format is not valid."),
    MAILING_CONTENT_ARCHIVED(322, "Mailing content archived."),
    FOLDER_ID_NOT_NUMBER(323, "Specified folder ID must be a number."),
    PARENT_FOLDER_VISIBILITY_MISMATCH(314, "Visibility of the list and parent folder must match."),
    FOLDER_ID_NOT_FOUND(325, "Specified folder ID does not exist."),
    CANNOT_UPDATE_EMAIL(326, "Unable to update recipient's EMAIL. EMAIL is part of Unique Identifier"),
    SYNC_FIELD_NAME_REQUIRED(329, "SYNC_FIELD Element found without a NAME."),
    MAILING_REPORT_DATA_NOT_AVAILABLE(500, "Detailed report data for this mailing is not available at this time. Please try again later."),
    QUERY_SAVE_ERROR(600, "Error saving query to the database."),
    AUTO_RESPONDER_NOT_ACTIVE(806, "Autoresponder is not active"),
    RECIPIENT_ID_NOT_FOUND(1001, "Recipient Id Not Found in List"),
    SYNC_ID_NOT_FOUND(1002, "Sync Id Not Found in List"),
    RECIPIENT_SAVE_ERROR(1003, "Error Saving Recipient"),
    RECIPIENT_RETRIEVE_ERROR(1004, "Error Retrieving Recipient"),
    RECIPIENT_ADD_ERROR(1005, "Error Adding Recipient"),
    RECIPIENT_KEY_MISSING(1006, "Missing Recipient Key Info"),
    COLUMN_DOES_NOT_EXIST(1007, "Column Does Not Exist in List"),
    RECIPIENT_NOT_FOUND(1008, "Recipient Not Found in List"),
    RECIPIENT_ALREADY_DELETED(1009, "Recipient Already Deleted in Engage"),
    CANNOT_UPDATE_SYSTEM_COLUMN(1011, "Cannot Update System Column"),
    RECIPIENT_MERGE_ERROR(1015, "Error Merging Recipient"),
    REQUIRED_COLUMN_VALUE(1016, "Missing Required Column Value"),
    EMAIL_NOT_VALID(1017, "Email Address is Invalid"),
    CONTACT_ALREADY_EXISTS(1018, "Error Adding Contact; Contact Already Exists in Contact List");


    private int number;
    private String description;

    XMLAPIErrorCode(int number, String description) {
        this.number = number;
        this.description = description;
    }

    public int number() {
        return number;
    }

    public String description() {
        return description;
    }

    public static XMLAPIErrorCode findByNumber(int number) {
        for (XMLAPIErrorCode errorCode : values()) {
            if (errorCode.number == number) {
                return errorCode;
            }
        }
        return UNKNOWN;
    }


}
