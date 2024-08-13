package net.gradleutil.conf.json.schema;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.*;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

public class RefWalker implements JsonSchemaWalkListener {
    ArrayList<JsonNode> visited = new ArrayList<>();
    String basePath;

    public RefWalker(String basePath) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
        if(this.basePath.equals("/")) {
            this.basePath = "";
        }
    }

    @Override
    public WalkFlow onWalkStart(WalkEvent we) {
        JsonNode node = we.getSchemaNode();
        String ref = we.getSchemaNode().get("$ref").asText();
        if (!ref.startsWith("#")) {
/*
            JsonNode rootNode = we.getParentSchema().findAncestor().getSchemaNode();
            ObjectNode defs = (ObjectNode) rootNode.get("definitions");
            SchemaLocation target = getTargetSchemaLocation(ref);
            JsonSchema schema = we.getRefSchema(target);
            String name;
            if(ref.split("#").length > 1) {
                name = ref.split("#")[1].replace("/definitions/", "");
            } else if(!schema.getSchemaNode().findValuesAsText("title").isEmpty()) {
                name = schema.getSchemaNode().findValuesAsText("title").iterator().next();
            } else {
                throw new RuntimeException("Could not determine name for schema: " + ref);
            }
            defs.set(name, schema.getSchemaNode());
*/
            //ObjectNode walkSchema = (ObjectNode) we.getSchemaNode();
            //walkSchema.put("$ref", "#/definitions/" + name);
            return WalkFlow.SKIP;
        }
        if (visited.contains(node)) {
            return WalkFlow.SKIP;
        }
        visited.add(node);
        return WalkFlow.CONTINUE;

    }

    SchemaLocation getTargetSchemaLocation(String ref) {
        AbsoluteIri iri = new AbsoluteIri(ref);
        if (!iri.getScheme().isEmpty()) {
            return new SchemaLocation(new AbsoluteIri(ref));
        } else {
            if(ref.contains("#")){
                String filePath = ref.split("#")[0];
                String refPath = ref.split("#")[1];
                File file = new File(basePath + filePath).getAbsoluteFile();
                if (!file.exists()) {
                    throw new RuntimeException("Schema file not found: " + file.getAbsolutePath());
                }
                return SchemaLocation.of("file://" + file.getAbsolutePath() + '#' + refPath);
            } else {
                File file = new File(basePath + ref).getAbsoluteFile();
                if (!file.exists()) {
                    throw new RuntimeException("Schema file not found: " + file.getAbsolutePath());
                }
                return new SchemaLocation(new AbsoluteIri("file://" + file.getAbsolutePath()));
            }
        }
    }

    @Override
    public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {

    }
}
