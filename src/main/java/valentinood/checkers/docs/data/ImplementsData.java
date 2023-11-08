package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;

import java.util.ArrayList;
import java.util.List;

public class ImplementsData implements IDocumentable {
    private final Class<?> clazz;
    private final List<Class<?>> interfaces;

    public ImplementsData(Class<?> clazz) {
        this.clazz = clazz;
        this.interfaces = new ArrayList<>();

        load();
    }

    private void load() {
        this.interfaces.addAll(List.of(clazz.getInterfaces()));
    }

    @Override
    public TextFile createDocs() throws Exception {
        TextFile file = new TextFile("implements.html");

        if (interfaces.isEmpty()) {
            file.clear();
            return file;
        }

        List<String> info = interfaces.stream().map(Class::getSimpleName).toList();
        file.replace("%IMPLEMENTS%", String.join(", ", info));

        return file;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<Class<?>> getInterfaces() {
        return new ArrayList<>(interfaces);
    }
}
