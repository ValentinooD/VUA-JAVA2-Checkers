package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class ParameterData implements IDocumentable {
    private final Class<?> parent;
    private final Executable executable;
    private final Parameter parameter;

    private final String name;
    private final Class<?> type;

    public ParameterData(Class<?> parent, Executable executable, Parameter parameter) {
        this.parent = parent;
        this.executable = executable;
        this.parameter = parameter;

        this.name = parameter.getName();
        this.type = parameter.getType();
    }

    @Override
    public TextFile createDocs() throws Exception {
        TextFile file = new TextFile("param.html");

        file.replace("%NAME%", name);

        if (type.isPrimitive()) {
            file.replace("%TYPE%", "<span class=\"primitive\">" + type.getSimpleName() + "</span>");
        } else {
            file.replace("%TYPE%", type.getSimpleName());
        }

        return file;
    }

    public Class<?> getParent() {
        return parent;
    }

    public Executable getExecutable() {
        return executable;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }
}
