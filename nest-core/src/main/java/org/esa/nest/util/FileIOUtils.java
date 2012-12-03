package org.esa.nest.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import java.util.*;

/**

 */
public class FileIOUtils {

    /**
     * Reads a text file and replaces all outText with newText
     * @param inFile input file
     * @param outFile output file
     * @param oldText text to replace
     * @param newText replacement text
     * @throws IOException on io error
     */
    public static void replaceText(final File inFile, final File outFile,
                                   final String oldText, final String newText) throws IOException {
        final List<String> lines;
        try (FileReader fileReader = new FileReader(inFile)) {
            lines = IOUtils.readLines(fileReader);

            for (int i = 0; i < lines.size(); ++i) {
                String line = lines.get(i);
                if (line.contains(oldText)) {
                    lines.set(i, line.replaceAll(oldText, newText));
                }
            }
        }

        if(!lines.isEmpty()) {
            try (FileWriter fileWriter = new FileWriter(outFile)) {
                IOUtils.writeLines(lines, "\n", fileWriter);
            }

        }
    }

    static class CopyDirVisitor extends SimpleFileVisitor<Path> {
        private final Path source;
        private final Path target;
        private final boolean isMove;

        CopyDirVisitor(final Path source, final Path target, boolean move) {
            this.source = source;
            this.target = target;
            this.isMove = move;
        }

        private boolean copyFile(final Path source, final Path target) {
            try {
                if(isMove)
                    Files.move(source, target, ATOMIC_MOVE, REPLACE_EXISTING);
                else
                    Files.copy(source, target, COPY_ATTRIBUTES, REPLACE_EXISTING);
            } catch (FileAlreadyExistsException x) {
                // ignore
            } catch (NoSuchFileException x) {
                // ignore
            } catch (IOException e) {
                ErrorHandler.reportError(e.toString());
                return false;
            }
            return true;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = target.resolve(source.relativize(dir));
            if(!Files.exists(targetPath)){
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            copyFile(file, target.resolve(source.relativize(file)));
            return FileVisitResult.CONTINUE;
        }
    }

    public static void copyFolder(final Path source, final Path target) throws IOException {
        // follow links when copying files
        final EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        final CopyDirVisitor tc = new CopyDirVisitor(source, target, false);
        Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
    }

    public static void moveFolder(final Path source, final Path target) throws IOException {
        // follow links when copying files
        final EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        final CopyDirVisitor tc = new CopyDirVisitor(source, target, true);
        Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
    }

    public static void deleteFolder(final Path source) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                    return CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
    }
}
