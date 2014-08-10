package magic.utility;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import magic.MagicMain;
import magic.MagicUtility;

/**
 * Utility class for useful or common file-system related tasks.
 *
 */
public final class MagicFiles {
    private MagicFiles() {}

    /**
     * Deletes all directory contents and then directory itself.
     */
    public static void deleteDirectory(final Path root) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null){
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                    throw exc;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens specified directory in OS file explorer.
     */
    public static void openDirectory(final String path) throws IOException {
        final File imagesPath = new File(path);
        if (MagicUtility.IS_WINDOWS_OS) {
            // Specific fix for Windows.
            // If running Windows and path is the default "Magarena" directory
            // then Desktop.getDesktop() will start a new instance of Magarena
            // instead of opening the directory! This is because the "Magarena"
            // directory and "Magarena.exe" are both at the same level and
            // Windows incorrectly assumes you mean "Magarena.exe".
            new ProcessBuilder("explorer.exe", imagesPath.getPath()).start();
        } else {
            Desktop.getDesktop().open(imagesPath);
        }
    }

    public static void openFileInDefaultOsEditor(final File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                if (MagicUtility.IS_WINDOWS_OS) {
                    // There is an issue in Windows where the open() method of getDesktop()
                    // fails silently. The recommended solution is to use getRuntime().
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + file.toString());
                } else {
                    Desktop.getDesktop().open(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(MagicMain.rootFrame, "Unable to open the following file using default application :\n" + file.getAbsolutePath());
            }
        } else {
            JOptionPane.showMessageDialog(MagicMain.rootFrame, "Sorry, opening this file with the default application is not supported on this OS.");
        }
    }

    public static void serializeStringList(final List<String> list, final File targetFile) {
        try (final FileOutputStream fos = new FileOutputStream(targetFile);
             final ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(list);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> deserializeStringList(final File sourceFile) {
        try (final FileInputStream fis = new FileInputStream(sourceFile);
             final ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<String>)ois.readObject();
        } catch (IOException|ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

}
