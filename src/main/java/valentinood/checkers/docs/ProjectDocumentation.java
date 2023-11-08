package valentinood.checkers.docs;

import valentinood.checkers.Constants;
import valentinood.checkers.docs.data.ClassData;
import valentinood.checkers.util.OSUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class ProjectDocumentation {
    private final String saveToDirectory;

    public ProjectDocumentation(String saveToDirectory) {
        this.saveToDirectory = saveToDirectory;
    }

    public void save() {
        try {
            Files.walkFileTree(Constants.CLASSDATA_PATH.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    createDocs(file);

                    return super.visitFile(file, attrs);
                }
            });

            if (OSUtils.getOS() == OSUtils.OSType.WINDOWS) {
                Runtime.getRuntime().exec(new String[]{"explorer.exe", "/select,", "\"" + saveToDirectory + "\""});
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDocs(Path file) {
        try {
        String className = StreamSupport.stream(file.spliterator(), false)
                .skip(2)
                .map(p -> p.toString().contains(".") ? p.toString().substring(0, p.toString().indexOf(".")) : p.toString())
                .collect(Collectors.joining("."));

        if (!file.toFile().getAbsolutePath().endsWith(".class") || className.equals("module-info"))
            return;

        Class<?> clazz = Class.forName(className);

        ClassData data = new ClassData(clazz);
        createPage(data);

        } catch (Exception e) {
            System.out.println("Did not generate documentation for " + file + "\t" + e.toString());
            e.printStackTrace();
        }
    }

    private void createPage(ClassData classData) throws Exception {
        File file = new File(saveToDirectory + "/" + classData.getFullClassName().replace(".", "/") + ".html");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        Files.writeString(file.toPath(), classData.createDocs().getContent());
    }
}
