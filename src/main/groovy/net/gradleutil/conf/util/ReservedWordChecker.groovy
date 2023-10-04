package net.gradleutil.conf.util

import java.util.regex.Pattern

class ReservedWordChecker {

    private static final Set<String> reservedWords = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
    ));

    static boolean isReservedWord(String word) {
        return reservedWords.contains(word.toLowerCase())
    }

    static boolean isValidJavaIdent(String identifier) {
        final Pattern ID_PATTERN = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*")
        return !isReservedWord(identifier) && ID_PATTERN.matcher(identifier).matches()
    }


    static String javaIdent(String s) {
        StringBuilder sb = new StringBuilder()
        if (!Character.isJavaIdentifierStart(s.charAt(0)) || isReservedWord(s)) {
            sb.append('_')
        }
        char lastChar = ' '
        for (char c : s.toCharArray()) {
            if (!Character.isJavaIdentifierPart(c)) {
                if (lastChar != '_' as char) {
                    sb.append('_')
                }
            } else {
                sb.append(c)
            }
            lastChar = c
        }
        return sb.toString()
    }


}
