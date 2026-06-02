package moe.wolfgirl.probejs.misc.javadoc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Code borrowed from https://github.com/delight-im/Javadoc-to-Markdown since vscode doesn't like <p> tags in javadocs
public class JavadocSanitizer {

    // ── Block-level patterns ───────────────────────────────────────────────

    /** Matches {@code <table>...</table>} including content across newlines */
    private static final Pattern TABLE = Pattern.compile(
            "<table>\\s*(.*?)\\s*</table>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches a single table row {@code <tr>...</tr>} */
    private static final Pattern TR = Pattern.compile(
            "<tr>\\s*(.*?)\\s*</tr>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches a header cell {@code <th>...</th>} */
    private static final Pattern TH = Pattern.compile(
            "<th>\\s*(.*?)\\s*</th>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches a data cell {@code <td>...</td>} */
    private static final Pattern TD = Pattern.compile(
            "<td>\\s*(.*?)\\s*</td>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <ul>...</ul>} */
    private static final Pattern UL = Pattern.compile(
            "<ul>\\s*(.*?)\\s*</ul>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <ol>...</ol>} */
    private static final Pattern OL = Pattern.compile(
            "<ol>\\s*(.*?)\\s*</ol>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches a list item {@code <li>...</li>} */
    private static final Pattern LI = Pattern.compile(
            "<li>\\s*(.*?)\\s*</li>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <h1>...</h1>} through {@code <h6>...</h6>} */
    private static final Pattern HEADING = Pattern.compile(
            "<h([1-6])>\\s*(.*?)\\s*</h\\1>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <br>} or {@code <br/>} */
    private static final Pattern BR = Pattern.compile(
            "<br\\s*/?>",
            Pattern.CASE_INSENSITIVE
    );

    /** Matches a {@code <p>} that sits alone on its own line */
    private static final Pattern P_STANDALONE = Pattern.compile(
            "(?m)^\\s*<p>\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    // ── Inline patterns ────────────────────────────────────────────────────

    /** Matches {@code <pre><code>...</code></pre>} */
    private static final Pattern PRE_CODE = Pattern.compile(
            "<pre>\\s*<code>\\s*(.*?)\\s*</code>\\s*</pre>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <pre><samp>...</samp></pre>} */
    private static final Pattern PRE_SAMP = Pattern.compile(
            "<pre>\\s*<samp>\\s*(.*?)\\s*</samp>\\s*</pre>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <code>...</code>} */
    private static final Pattern CODE_TAG = Pattern.compile(
            "<code>\\s*(.*?)\\s*</code>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <i>...</i>} */
    private static final Pattern I_TAG = Pattern.compile(
            "<i>\\s*(.*?)\\s*</i>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <em>...</em>} */
    private static final Pattern EM_TAG = Pattern.compile(
            "<em>\\s*(.*?)\\s*</em>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <b>...</b>} */
    private static final Pattern B_TAG = Pattern.compile(
            "<b>\\s*(.*?)\\s*</b>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    /** Matches {@code <strong>...</strong>} */
    private static final Pattern STRONG_TAG = Pattern.compile(
            "<strong>\\s*(.*?)\\s*</strong>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // ── Javadoc inline tag patterns ────────────────────────────────────────

    /** Matches {@code {@code text}} */
    private static final Pattern JAVADOC_CODE = Pattern.compile(
            "\\{@code\\s+(.*?)}"
    );

    /** Matches {@code {@literal text}} */
    private static final Pattern JAVADOC_LITERAL = Pattern.compile(
            "\\{@literal\\s+(.*?)}"
    );

    /**
     * Matches {@code {@link reference}} or {@code {@link reference label}}.
     * Uses {@code [^}]+} to capture everything up to the closing brace without
     * crossing into other Javadoc tags (which all start with {@code {}).
     */
    private static final Pattern JAVADOC_LINK = Pattern.compile(
            "\\{@link\\s+([^}]+)}"
    );

    /**
     * Matches {@code {@linkplain reference}} or {@code {@linkplain reference label}}.
     * Same safe capture as {@link #JAVADOC_LINK}.
     */
    private static final Pattern JAVADOC_LINKPLAIN = Pattern.compile(
            "\\{@linkplain\\s+([^}]+)}"
    );

    /** Matches {@code {@return description}} — inline return tag */
    private static final Pattern JAVADOC_RETURN = Pattern.compile(
            "\\{@return\\s+(.*?)}"
    );

    /** Safety-net: strips any remaining HTML tags */
    private static final Pattern ANY_HTML_TAG = Pattern.compile(
            "<[^>]+>"
    );

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Sanitizes a list of raw Javadoc lines by converting HTML tags to
     * Markdown that VS Code can render properly in hover tooltips.
     *
     * @param javaDoc the raw Javadoc lines (from parchment data)
     * @return sanitized lines with HTML replaced by Markdown equivalents
     */
    public static List<String> sanitize(List<String> javaDoc) {
        if (javaDoc == null || javaDoc.isEmpty()) {
            return List.of();
        }

        // Join all lines so cross-line HTML structures (tables, lists) can be
        // matched as a single block.
        String text = String.join("\n", javaDoc);

        // ── Phase 1: Block-level transformations ────────────────────────
        text = convertTables(text);
        text = convertLists(text);
        text = convertHeadings(text);
        text = convertLineBreaks(text);
        text = convertParagraphs(text);

        // ── Phase 2: Inline transformations ─────────────────────────────
        // These run after block-level so that HTML inside markdown table
        // cells / list items still gets cleaned up.
        text = convertPreBlocks(text);
        text = convertCodeTags(text);
        text = convertJavadocTags(text);
        text = convertFormatting(text);
        text = stripRemainingHtml(text);

        // Split back into individual lines and collapse redundant blank lines.
        String[] lines = text.split("\n", -1);
        return collapseEmptyLines(lines);
    }

    // ── Block-level converters ─────────────────────────────────────────────

    /**
     * Converts {@code <table>...</table>} blocks to GitHub-flavoured Markdown
     * tables.  Cell content is left as-is — inline tags inside cells are
     * cleaned up by the later inline passes.
     */
    private static String convertTables(String text) {
        Matcher m = TABLE.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String markdownTable = buildMarkdownTable(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(markdownTable));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Converts {@code <ul>...</ul>} and {@code <ol>...</ol>} blocks to
     * Markdown lists.
     */
    private static String convertLists(String text) {
        // Ordered lists first (more specific), then unordered.
        text = convertListType(text, OL, true);
        text = convertListType(text, UL, false);
        return text;
    }

    private static String convertListType(String text, Pattern listPattern, boolean ordered) {
        Matcher m = listPattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String markdownList = buildMarkdownList(m.group(1), ordered);
            m.appendReplacement(sb, Matcher.quoteReplacement(markdownList));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Converts {@code <h1>}–{@code <h6>} to Markdown ATX headings.
     */
    private static String convertHeadings(String text) {
        Matcher m = HEADING.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            int level = Integer.parseInt(m.group(1));
            String content = m.group(2).trim();
            String prefix = "#".repeat(level);
            // Ensure a blank line before the heading for readability.
            m.appendReplacement(sb,
                    Matcher.quoteReplacement("\n\n" + prefix + " " + content + "\n"));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Converts {@code <br>} / {@code <br/>} to a newline.
     */
    private static String convertLineBreaks(String text) {
        return BR.matcher(text).replaceAll("\n");
    }

    /**
     * Converts standalone {@code <p>} lines into paragraph breaks (two
     * newlines).
     */
    private static String convertParagraphs(String text) {
        return P_STANDALONE.matcher(text).replaceAll("\n");
    }

    // ── Inline converters ──────────────────────────────────────────────────

    /**
     * Converts {@code <pre><code>...</code></pre>} and
     * {@code <pre><samp>...</samp></pre>} to inline code with backticks.
     * We use inline code rather than fenced code blocks because these
     * frequently appear inside table cells where fenced blocks would break
     * the table layout.
     */
    private static String convertPreBlocks(String text) {
        text = PRE_CODE.matcher(text).replaceAll("`$1`");
        text = PRE_SAMP.matcher(text).replaceAll("`$1`");
        return text;
    }

    /**
     * Converts {@code <code>...</code>} to inline code with backticks.
     */
    private static String convertCodeTags(String text) {
        return CODE_TAG.matcher(text).replaceAll("`$1`");
    }

    /**
     * Converts standard Javadoc inline tags ({@code {@code}}, {@code {@link}},
     * {@code {@linkplain}}, {@code {@literal}}, {@code {@return}}) to their
     * Markdown / plain-text equivalents.
     *
     * <p><b>Order matters:</b> {@code {@code}} and {@code {@literal}} are
     * handled first so that {@code {@link}} doesn't accidentally match
     * content inside them.
     */
    private static String convertJavadocTags(String text) {
        // {@code text}  →  `text`
        text = JAVADOC_CODE.matcher(text).replaceAll("`$1`");

        // {@literal text}  →  text
        text = JAVADOC_LITERAL.matcher(text).replaceAll("$1");

        // {@link reference} or {@link reference label}  →  `shortName` or label
        // The captured group is everything between the whitespace after @link
        // and the closing }.  If it contains a space we treat the first
        // whitespace-separated token as the reference and the rest as a
        // display label; otherwise the whole thing is the reference.
        text = JAVADOC_LINK.matcher(text).replaceAll(matchResult -> {
            String captured = matchResult.group(1).trim();
            int firstSpace = indexOfFirstSpace(captured);
            if (firstSpace > 0) {
                // Has a label — return only the label text.
                return captured.substring(firstSpace + 1).trim();
            }
            // Bare reference — output as inline code with short name.
            return "`" + shortName(captured) + "`";
        });

        // {@linkplain reference} or {@linkplain reference label}
        // Same logic as @link but output is plain text (no backticks).
        text = JAVADOC_LINKPLAIN.matcher(text).replaceAll(matchResult -> {
            String captured = matchResult.group(1).trim();
            int firstSpace = indexOfFirstSpace(captured);
            if (firstSpace > 0) {
                return captured.substring(firstSpace + 1).trim();
            }
            return shortName(captured);
        });

        // {@return description}  →  @return description
        text = JAVADOC_RETURN.matcher(text).replaceAll("@return $1");

        return text;
    }

    /**
     * Converts formatting tags: {@code <i>}, {@code <em>} → asterisks,
     * {@code <b>}, {@code <strong>} → double asterisks.
     */
    private static String convertFormatting(String text) {
        text = I_TAG.matcher(text).replaceAll("*$1*");
        text = EM_TAG.matcher(text).replaceAll("*$1*");
        text = B_TAG.matcher(text).replaceAll("**$1**");
        text = STRONG_TAG.matcher(text).replaceAll("**$1**");
        return text;
    }

    /**
     * Safety-net: removes any HTML tags that weren't explicitly handled
     * above.  Also cleans up leftover whitespace around removed tags.
     */
    private static String stripRemainingHtml(String text) {
        return ANY_HTML_TAG.matcher(text).replaceAll("");
    }

    // ── Table builder ──────────────────────────────────────────────────────

    /**
     * Builds a GitHub-flavoured Markdown table from the inner content of a
     * {@code <table>} block (everything between {@code <table>} and
     * {@code </table>}).
     */
    private static String buildMarkdownTable(String tableContent) {
        List<List<String>> rows = new ArrayList<>();
        boolean hasHeader = false;

        Matcher trMatcher = TR.matcher(tableContent);
        while (trMatcher.find()) {
            String rowContent = trMatcher.group(1);
            List<String> cells = new ArrayList<>();

            // Check for <th> cells first (header row)
            Matcher thMatcher = TH.matcher(rowContent);
            while (thMatcher.find()) {
                cells.add(thMatcher.group(1).trim());
                hasHeader = true;
            }

            // Check for <td> cells
            Matcher tdMatcher = TD.matcher(rowContent);
            while (tdMatcher.find()) {
                cells.add(tdMatcher.group(1).trim());
            }

            if (!cells.isEmpty()) {
                rows.add(cells);
            }
        }

        if (rows.isEmpty()) {
            return "";
        }

        // Determine the maximum number of columns.
        int maxCols = 0;
        for (List<String> row : rows) {
            if (row.size() > maxCols) {
                maxCols = row.size();
            }
        }

        StringBuilder sb = new StringBuilder("\n");

        // Header row.
        List<String> headerRow = rows.getFirst();
        sb.append("| ");
        for (int col = 0; col < maxCols; col++) {
            sb.append(col < headerRow.size() ? headerRow.get(col) : "");
            sb.append(" | ");
        }
        sb.append("\n");

        // Separator row.
        sb.append("|");
        sb.append("---|".repeat(maxCols));
        sb.append("\n");

        // Data rows (skip the first row if it was a header).
        int startRow = hasHeader ? 1 : 0;
        for (int r = startRow; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            sb.append("| ");
            for (int col = 0; col < maxCols; col++) {
                sb.append(col < row.size() ? row.get(col) : "");
                sb.append(" | ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    // ── List builder ───────────────────────────────────────────────────────

    /**
     * Builds a Markdown list from the inner content of a {@code <ul>} or
     * {@code <ol>} block.
     */
    private static String buildMarkdownList(String listContent, boolean ordered) {
        Matcher liMatcher = LI.matcher(listContent);
        StringBuilder sb = new StringBuilder("\n");
        int index = 1;

        while (liMatcher.find()) {
            String item = liMatcher.group(1).trim();
            if (ordered) {
                sb.append(index).append(". ").append(item).append("\n");
                index++;
            } else {
                sb.append("- ").append(item).append("\n");
            }
        }

        return sb.toString();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /**
     * Extracts the simple class name from a fully-qualified class path.
     * <p>Examples:
     * <ul>
     *   <li>{@code java.lang.String} → {@code String}</li>
     *   <li>{@code org.lwjgl.opengl.GLDebugMessageCallback} →
     *       {@code GLDebugMessageCallback}</li>
     *   <li>{@code #expand(double,double,double)} → kept as-is (method ref)</li>
     * </ul>
     */
    private static String shortName(String qualifiedName) {
        if (qualifiedName == null || qualifiedName.isEmpty()) {
            return qualifiedName;
        }
        // If it starts with '#' it's a method/field reference — keep as-is.
        if (qualifiedName.startsWith("#")) {
            return qualifiedName;
        }
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < qualifiedName.length() - 1) {
            return qualifiedName.substring(lastDot + 1);
        }
        return qualifiedName;
    }

    /**
     * Finds the index of the first space character that separates a Javadoc
     * link reference from its display label.  We look for the first space
     * that is <em>not</em> inside parentheses (to avoid splitting method
     * signatures like {@code #method(int, int)}).
     */
    private static int indexOfFirstSpace(String text) {
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == ' ' && depth == 0) return i;
        }
        return -1;
    }

    /**
     * Collapses multiple consecutive empty lines into a single empty line
     * and trims leading/trailing blank lines.
     */
    private static List<String> collapseEmptyLines(String[] lines) {
        List<String> result = new ArrayList<>();
        boolean prevEmpty = false;

        for (String line : lines) {
            boolean isEmpty = line.trim().isEmpty();
            if (isEmpty) {
                if (!prevEmpty && !result.isEmpty()) {
                    result.add("");
                }
            } else {
                result.add(line);
            }
            prevEmpty = isEmpty;
        }

        // Trim trailing empty lines.
        while (!result.isEmpty() && result.getLast().isEmpty()) {
            result.removeLast();
        }

        return result;
    }
}
