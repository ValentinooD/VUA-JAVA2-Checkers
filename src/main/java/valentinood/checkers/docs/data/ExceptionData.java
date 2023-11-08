package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;

public class ExceptionData implements IDocumentable {
    private final Class<?> parent;
    private final Executable executable;

    private final List<Class<?>> exceptions;

    public ExceptionData(Class<?> parent, Executable executable) {
        this.parent = parent;
        this.executable = executable;
        this.exceptions = new ArrayList<>();

        load();
    }

    private void load() {
        this.exceptions.addAll(List.of(executable.getExceptionTypes()));
    }

    @Override
    public TextFile createDocs() throws Exception {
        TextFile file = new TextFile("throws.html");

        List<String> list = exceptions.stream().map(Class::getSimpleName).toList();

        if (!exceptions.isEmpty()) {
            file.replace("%EXCEPTIONS%", String.join(", ", list));
        } else {
            file.clear();
        }

        return file;
    }

    public Class<?> getParent() {
        return parent;
    }

    public Executable getExecutable() {
        return executable;
    }

    public List<Class<?>> getExceptions() {
        return new ArrayList<>(exceptions);
    }
}
