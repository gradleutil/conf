package net.gradleutil.conf.util;

import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ReservedWordChecker {
    public static boolean isReservedWord(String word) {
        return reservedWords.contains(word.toLowerCase());
    }

    public static boolean isValidJavaIdent(String identifier) {
        final Pattern ID_PATTERN = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");
        return !isReservedWord(identifier) && ID_PATTERN.matcher(identifier).matches();
    }

    public static String javaIdent(String s) {
        StringBuilder sb = new StringBuilder();
        if (!Character.isJavaIdentifierStart(s.charAt(0)) || isReservedWord(s)) {
            sb.append("_");
        }

        char lastChar = ' ';
        for (char c : s.toCharArray()) {
            if (!Character.isJavaIdentifierPart(c)) {
                if (lastChar != StringGroovyMethods.asType("_", Character.class)) {
                    sb.append("_");
                }

            } else {
                sb.append(c);
            }

            lastChar = c;
        }

        return sb.toString();
    }

    private static final Set<String> reservedWords = new HashSet<String>(Arrays.asList("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"));
}
