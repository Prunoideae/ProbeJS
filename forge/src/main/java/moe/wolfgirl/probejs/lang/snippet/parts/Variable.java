package moe.wolfgirl.probejs.lang.snippet.parts;

public enum Variable implements SnippetPart {

    // Stuffs related to comments
    LINE_COMMENT,
    BLOCK_COMMENT_END,
    BLOCK_COMMENT_START,

    // Stuffs related to random
    UUID,
    RANDOM_HEX,
    RANDOM,

    // Stuffs related to time
    CURRENT_TIMEZONE_OFFSET,
    CURRENT_SECONDS_UNIX,
    CURRENT_SECOND,
    CURRENT_MINUTE,
    CURRENT_HOUR,
    CURRENT_DAY_NAME_SHORT,
    CURRENT_DAY_NAME,
    CURRENT_DATE,
    CURRENT_MONTH_NAME_SHORT,
    CURRENT_MONTH_NAME,
    CURRENT_MONTH,
    CURRENT_YEAR_SHORT,
    CURRENT_YEAR,

    // Stuffs related to editor and documents
    CURSOR_NUMBER,
    CURSOR_INDEX,
    WORKSPACE_FOLDER,
    WORKSPACE_NAME,
    CLIPBOARD,
    RELATIVE_FILEPATH,
    TM_FILEPATH,
    TM_DIRECTORY,
    TM_FILENAME_BASE,
    TM_FILENAME,
    TM_LINE_NUMBER,
    TM_LINE_INDEX,
    TM_CURRENT_WORD,
    TM_CURRENT_LINE,
    TM_SELECTED_TEXT;


    @Override
    public String format() {
        return "$%s".formatted(name());
    }
}
