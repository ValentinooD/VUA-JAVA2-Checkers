package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;

import java.util.LinkedList;
import java.util.List;

public class ExtendsData implements IDocumentable {
    private final Class<?> clazz;
    private final List<Class<?>> extensions;

    public ExtendsData(Class<?> clazz) {
        this.clazz = clazz;
        this.extensions = new LinkedList<>();

        load();
    }

    private void load() {
        Class<?> c = clazz.getSuperclass();
        while (c != Object.class && c != null) {
            if (c != Enum.class) {
                extensions.add(c);
            }

            c = c.getSuperclass();
        }
    }

    @Override
    public TextFile createDocs() throws Exception {
        TextFile file = new TextFile("extends.html");

        if (extensions.isEmpty()) {
            file.clear();
            return file;
        }

        List<String> info = extensions.stream().map(Class::getName).toList();
        file.replace("%INFO%", String.join("\n", info));

        file.replace("%EXTENDS%", extensions.get(0).getSimpleName());

        return file;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<Class<?>> getExtensions() {
        return new LinkedList<>(extensions);
    }
}
