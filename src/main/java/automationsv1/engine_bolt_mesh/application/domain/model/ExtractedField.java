package automationsv1.engine_bolt_mesh.application.domain.model;

public record ExtractedField(
        String alias,
        String path,
        FieldType type
) {
    public enum FieldType { STRING, INTEGER, DECIMAL, BOOLEAN, OBJECT }
}
