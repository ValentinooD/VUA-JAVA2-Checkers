package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ClassData implements IDocumentable {
    private final String className;
    private final Class<?> clazz;

    private final List<ConstructorData> constructors;
    private final List<AnnotationData> annotations;
    private ImplementsData interfaces;
    private ExtendsData extension;
    private final List<FieldData> fields;
    private final List<MethodData> methods;

    public ClassData(Class<?> clazz) {
        this.className = clazz.getSimpleName();
        this.clazz = clazz;

        this.constructors = new ArrayList<>();
        this.annotations = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        load();
    }

    private void load() {
        interfaces = new ImplementsData(clazz);
        extension = new ExtendsData(clazz);

        for (Annotation annotation : clazz.getAnnotations()) {
            annotations.add(new AnnotationData(clazz, annotation));
        }

        for (Field field : clazz.getDeclaredFields()) {
            fields.add(new FieldData(clazz, field));
        }

        for (Method method : clazz.getDeclaredMethods()) {
            methods.add(new MethodData(clazz, method));
        }

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            constructors.add(new ConstructorData(clazz, constructor));
        }
    }

    @Override
    public TextFile createDocs() throws Exception {
        TextFile txtFile = new TextFile("base.html");

        String type = "class";
        if (clazz.isEnum()) type = "enum";
        else if (clazz.isInterface()) type = "interface";

        txtFile.replace("%PACKAGE%", clazz.getPackage().getName());
        txtFile.replace("%MOD%", Modifier.toString(clazz.getModifiers()));
        txtFile.replace("%TYPE%", type);
        txtFile.replace("%NAME%", className);

        txtFile.insert("%EXTENDS%", extension);
        txtFile.insert("%IMPLEMENTS%", interfaces);
        
        txtFile.insert("%ANNOTATIONS%", annotations.stream().map(a -> (IDocumentable) a).toList());
        txtFile.insert("%CONSTRUCTORS%", constructors.stream().map(a -> (IDocumentable) a).toList());
        txtFile.insert("%FIELDS%", fields.stream().map(a -> (IDocumentable) a).toList());
        txtFile.insert("%METHODS%", methods.stream().map(a -> (IDocumentable) a).toList());

        return txtFile;
    }

    public String getClassName() {
        return className;
    }

    public String getFullClassName() {
        return clazz.getName();
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<ConstructorData> getConstructors() {
        return new ArrayList<>(constructors);
    }

    public List<AnnotationData> getAnnotations() {
        return new ArrayList<>(annotations);
    }

    public ImplementsData getInterfaces() {
        return interfaces;
    }

    public ExtendsData getExtension() {
        return extension;
    }

    public List<FieldData> getFields() {
        return new ArrayList<>(fields);
    }

    public List<MethodData> getMethods() {
        return new ArrayList<>(methods);
    }
}
