package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;

import java.lang.annotation.Annotation;

public class AnnotationData implements IDocumentable {
    private final Annotation annotation;

    private final String name;

    public AnnotationData(Annotation annotation) {
        this.annotation = annotation;
        this.name = annotation.toString().substring(annotation.annotationType().getPackage().getName().length() + 2);
    }

    @Override
    public TextFile createDocs() throws Exception {
        TextFile file = new TextFile("annotation.html");
        file.replace("%NAME%", name);
        return file;
    }

    public String getName() {
        return name;
    }

    public Annotation getAnnotation() {
        return annotation;
    }
}
