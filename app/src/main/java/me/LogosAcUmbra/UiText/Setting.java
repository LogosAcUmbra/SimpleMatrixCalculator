package me.LogosAcUmbra.UiText;

public class Setting {
    int indentSize;

    private Setting(int indentSize) {
        this.indentSize = indentSize;
    }

    public static Setting of(int indentSize) {
        if (indentSize < 0) {
            throw new IllegalArgumentException(String.format("invalid indentSize (%d)", indentSize));
        }
        return new Setting(indentSize);
    }

    public void setTo(Setting other) { this.indentSize = other.indentSize; }
    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }
}
