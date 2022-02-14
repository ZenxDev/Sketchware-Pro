package a.a.a;

import java.util.HashMap;

public class Mx {

    public Nx a = new Nx("resources");
    public HashMap<String, Nx> b = new HashMap<>();

    public String a() {
        return a.b();
    }

    public void a(String name, int value) {
        Nx integerBuilder = new Nx("integer", true);
        integerBuilder.a("", "name", name);
        integerBuilder.a(String.valueOf(value));
        a.a(integerBuilder);
    }

    public void a(String name, String value) {
        Nx colorBuilder = new Nx("color", true);
        colorBuilder.a("", "name", name);
        colorBuilder.a(value);
        a.a(colorBuilder);
    }

    public void a(String styleName, String name, String value) {
        Nx styleBuilder = b.get(styleName);
        if (styleBuilder != null) {
            Nx styleItemBuilder = new Nx("item", true);
            styleItemBuilder.a("", "name", name);
            styleItemBuilder.a(value);
            styleBuilder.a(styleItemBuilder);
        }
    }

    public void a(String name, String value, boolean translatable) {
        Nx stringBuilder = new Nx("string", true);
        stringBuilder.a("", "name", name);
        if (!translatable) {
            stringBuilder.a("", "translatable", "false");
        }

        String trimmedValue = value.trim();
        value = trimmedValue;
        if (trimmedValue.length() > 0) {
            name = trimmedValue;
            if (trimmedValue.charAt(0) == '"') {
                name = trimmedValue.substring(1);
            }

            value = name;
            if (name.charAt(name.length() - 1) == '"') {
                value = name.substring(0, name.length() - 1);
            }
        }

        stringBuilder.a(value);
        a.a(stringBuilder);
    }

    public void b(String name, String value) {
        a(name, value, false);
    }

    public void c(String name, String parent) {
        Nx styleBuilder = new Nx("style", true);
        styleBuilder.a("", "name", name);
        if (parent.length() > 0) {
            styleBuilder.a("", "parent", parent);
        }

        a.a(styleBuilder);
        b.put(name, styleBuilder);
    }
}
