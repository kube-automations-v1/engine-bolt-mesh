package automationsv1.engine_bolt_mesh.adapter.out.spel;

import java.time.Instant;
import java.util.UUID;

public final class WorkflowFunctions {

    private WorkflowFunctions() {}

    public static String upper(String s) {
        return s == null ? null : s.toUpperCase();
    }

    public static String lower(String s) {
        return s == null ? null : s.toLowerCase();
    }

    public static String nowIso() {
        return Instant.now().toString();
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static Object coalesce(Object a, Object b) {
        return a != null ? a : b;
    }

}
