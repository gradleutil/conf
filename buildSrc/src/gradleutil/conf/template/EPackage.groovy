package gradleutil.conf.template

import org.codehaus.groovy.syntax.Types

class EPackage {
    public String name
    public String rootClassName
    public List<EClass> classes = []
    static final Collection<String> KEYWORDS = Types.getKeywords()

    static class EClass {
        public String etype
        public String name
        public List<EStructuralFeature> features = []
        public List<EAnnotation> annotations = []

        def setEtype(String rawname) {
            etype = rawname.capitalize()
        }

        def setName(String rawname) {
            if(!etype){
                setEtype(rawname)
            }
            if (KEYWORDS.contains(rawname) || rawname == 'enum') {
                name = 'e' + rawname
            } else {
                name = rawname
            }
        }
    }

    static String toEnumValue(String string) {
        string.replaceAll("[^A-Za-z0-9]+", '_').toUpperCase().with {
            if (Character.isDigit(string.charAt(0))) {
                'V' + it
            } else {
                it
            }
        }
    }

    static class EStructuralFeature extends EClass {
        public String format
        public Object defaultValue
        public List<Object> valueList
        public Boolean isContainment
        public int upperBound = -1
        public int lowerBound = 0
        public List<EAnnotation> annotations = []
        def enums = valueList.collect { "${toEnumValue(it as String)}(\"${it}\")" }.join(',')

        String asEnum() {
            return '{' + enums + """
        private final String name;
    
        private ${name}(String s) {
            name = s;
        }
    
        public boolean equalsName(String otherName) {
            return name.equals(otherName);
        }
    
        public String toString() {
           return this.name;
        }
    }
"""
        }
    }

    static class EAnnotation {
        public String source
        public List<EAnnotationKeyValue> details = []
    }

    static class EAnnotationKeyValue {
        public String key
        public String value
    }
}

