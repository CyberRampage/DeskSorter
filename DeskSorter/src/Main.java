import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String startMessage = "Sort Desk?";
        int response = JOptionPane.showConfirmDialog(null, startMessage, "Confirm", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            //Path base = Paths.get(System.getProperty("user.home"), "Desktop") <- Für Allgemeinen Desktop-PFad
            //Path base = Paths.get(deskpath);                               <-
            Path base = Paths.get(System.getProperty("user.home"), "Desktop");

            try {
                Files.createDirectories(base);
            } catch (IOException e) {
                System.err.println("Basisordner konnte nicht erstellt werden: " + base + " (" + e.getMessage() + ")");
                return;
            }

            // Zielordner-Namen
            List<String> folderNames = Arrays.asList("Images", "Music", "Programs", "Documents", "Videos", "Others");

            // Ordner anlegen
            for (String name : folderNames) {
                Path dir = base.resolve(name);
                try {
                    Files.createDirectories(dir);
                    System.out.println(name + " folder ready: " + dir);
                } catch (IOException e) {
                    System.err.println(name + " folder couldn't be created: " + dir + " (" + e.getMessage() + ")");
                }
            }

            // Dateien im Basisordner einsammeln und verschieben
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(base)) {
                for (Path entry : stream) {
                    if (!Files.isRegularFile(entry)) {
                        continue; // Nur Dateien, keine Ordner verschieben
                    }

                    String fileName = entry.getFileName().toString();
                    String ext = getExtension(fileName);
                    String targetFolderName = resolveFolderByExtension(ext);

                    Path targetDir = base.resolve(targetFolderName);
                    Path targetFile = resolveNonClobber(targetDir.resolve(fileName));

                    try {
                        Files.move(entry, targetFile);
                        System.out.println("Moved: " + fileName + "  ->  " + targetDir.getFileName());
                    } catch (IOException moveEx) {
                        System.err.println("Couldn't move " + fileName + ": " + moveEx.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Fehler beim Durchsuchen des Ordners: " + e.getMessage());
            }

            System.out.println("Desk sorted");
        } else {
            System.out.println("Desk sorting canceled");
        }
    }

    // Ermittelt die Dateiendung
    private static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot > 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        return "";
    }

    // Sortierung nach Endungen (könnten noch einige fehlen) Würde gerne noch was für "Verknüpfungen" machen
    private static String resolveFolderByExtension(String ext) {
        if (ext.isEmpty()) return "Others";

        Set<String> images = Set.of("jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "heic", "tiff");
        Set<String> music  = Set.of("mp3", "wav", "flac", "m4a", "aac", "ogg", "wma");
        Set<String> videos = Set.of("mp4", "mov", "avi", "mkv", "webm", "m4v");
        Set<String> docs   = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "rtf");
        Set<String> progs  = Set.of("exe", "msi", "bat", "cmd", "sh", "jar");

        if (images.contains(ext)) return "Images";
        if (music.contains(ext))  return "Music";
        if (videos.contains(ext)) return "Videos";
        if (docs.contains(ext))   return "Documents";
        if (progs.contains(ext))  return "Programs";
        return "Others";
    }

    // Falls die Zieldatei schon existiert, wird" (1)", " (2)", ... angehangen
    private static Path resolveNonClobber(Path target) {
        if (!Files.exists(target)) return target;

        String fileName = target.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String base = (dot > 0) ? fileName.substring(0, dot) : fileName;
        String ext  = (dot > 0) ? fileName.substring(dot) : "";

        int i = 1;
        Path parent = target.getParent();
        Path candidate;
        do {
            String newName = base + " (" + i++ + ")" + ext;
            candidate = parent.resolve(newName);
        } while (Files.exists(candidate));

        return candidate;
    }
}