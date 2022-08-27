package com.probejs.document;

import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.document.comment.CommentUtil;
import com.probejs.document.comment.special.CommentAssign;
import com.probejs.document.comment.special.CommentTarget;
import com.probejs.document.parser.processor.Document;
import com.probejs.document.type.IType;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Manager {
    public static Map<String, List<DocumentClass>> classDocuments = new HashMap<>();
    public static Map<String, List<IType>> typesAssignable = new HashMap<>();
    public static Map<String, List<DocumentClass>> classAdditions = new HashMap<>();
    public static List<String> rawTSDoc = new ArrayList<>();
    public static List<DocumentType> typeDocuments = new ArrayList<>();


    public static void fromPath(Document document) throws IOException {
        File[] files = ProbePaths.DOCS.toFile().listFiles();
        List<File> filesSorted = files == null ? new ArrayList<>() : new ArrayList<>(Arrays.stream(files).toList());
        filesSorted.sort(Comparator.comparing(File::getName));
        for (File f : filesSorted) {
            if (!f.getName().endsWith(".d.ts"))
                return;
            if (f.isDirectory())
                return;
            BufferedReader reader = Files.newBufferedReader(f.toPath());
            if (!f.getName().startsWith("!"))
                reader.lines().forEach(document::step);
            else
                reader.lines().forEach(rawTSDoc::add);
        }
    }

    public static void fromFiles(Document document) throws IOException {
        for (Mod mod : Platform.getMods()) {
            Optional<Path> docsList = mod.findResource("probejs.documents.txt");
            if (docsList.isPresent()) {
                ProbeJS.LOGGER.info("Found documents list from %s".formatted(mod.getName()));
                for (String subEntry : Files.lines(docsList.get()).toList()) {
                    if (subEntry.startsWith("!")) {
                        subEntry = subEntry.substring(1);
                        int i = subEntry.indexOf(" ");
                        if (i != -1) {
                            if (!Platform.isModLoaded(subEntry.substring(0, i))) {
                                continue;
                            }
                            subEntry = subEntry.substring(i + 1);
                        }
                        Optional<Path> docEntry = mod.findResource(subEntry);
                        if (docEntry.isPresent()) {
                            ProbeJS.LOGGER.info("Loading raw document inside jar - %s".formatted(subEntry));
                            Files.lines(docEntry.get()).forEach(rawTSDoc::add);
                        } else {
                            ProbeJS.LOGGER.warn("Document from file is not found - %s".formatted(subEntry));
                        }
                    } else {
                        Optional<Path> docEntry = mod.findResource(subEntry);
                        if (docEntry.isPresent()) {
                            ProbeJS.LOGGER.info("Loading document inside jar - %s".formatted(subEntry));
                            Files.lines(docEntry.get()).forEach(document::step);
                        } else {
                            ProbeJS.LOGGER.warn("Document from file is not found - %s".formatted(subEntry));
                        }
                    }
                }
            }
        }
    }

    public static void init() {
        classDocuments.clear();
        classAdditions.clear();
        typeDocuments.clear();
        typesAssignable.clear();
        rawTSDoc.clear();

        Document documentState = new Document();
        try {
            fromFiles(documentState);
            fromPath(documentState);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (IDocument doc : documentState.getDocument().getDocuments()) {
            if (doc instanceof DocumentClass classDoc) {
                if (CommentUtil.isLoaded(classDoc.getComment())) {
                    DocumentComment comment = classDoc.getComment();
                    if (comment != null) {
                        CommentTarget target = comment.getSpecialComment(CommentTarget.class);
                        if (target != null) {
                            classDocuments.computeIfAbsent(target.getTargetName(), s -> new ArrayList<>()).add(classDoc);
                            List<CommentAssign> assignable = comment.getSpecialComments(CommentAssign.class);
                            typesAssignable.computeIfAbsent(target.getTargetName(), s -> new ArrayList<>()).addAll(assignable.stream().map(CommentAssign::getType).collect(Collectors.toList()));
                            continue;
                        }
                    }
                    classAdditions.computeIfAbsent(classDoc.getName(), s -> new ArrayList<>()).add(classDoc);
                }
            }

            if (doc instanceof DocumentType) {
                if (CommentUtil.isLoaded(((DocumentType) doc).getComment()))
                    typeDocuments.add((DocumentType) doc);
            }
        }
    }
}
