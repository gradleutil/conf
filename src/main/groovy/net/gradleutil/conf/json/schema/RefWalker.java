package net.gradleutil.conf.json.schema;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

public class RefWalker implements JsonSchemaWalkListener {
    ArrayList<JsonNode> visited = new ArrayList<>();
    String basePath;

    public RefWalker(String basePath) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
    }
    @Override
    public WalkFlow onWalkStart(WalkEvent we) {
        JsonNode node = we.getSchemaNode();
        String ref = we.getSchemaNode().get("$ref").asText();
        if (!ref.startsWith("#")) {
            JsonNode rootNode = we.getParentSchema().findAncestor().getSchemaNode();
            ObjectNode defs = (ObjectNode) rootNode.get("definitions");
            SchemaLocation target = new SchemaLocation(new AbsoluteIri(ref));
            if (target.getAbsoluteIri().getScheme().isEmpty()) {
                File file = new File(ref);
                System.out.println(file.getAbsolutePath());
                target = new SchemaLocation(new AbsoluteIri("file://" + basePath + ref));
            }
            JsonSchema schema = we.getRefSchema(target);
            String name = schema.getSchemaNode().findValuesAsText("title").iterator().next();
            defs.set(name, schema.getSchemaNode());
            ObjectNode walkSchema = (ObjectNode) we.getSchemaNode();
            walkSchema.put("$ref", "#/definitions/" + name);
            return WalkFlow.SKIP;
        }
        if (visited.contains(node)) {
            return WalkFlow.SKIP;
        }
        visited.add(node);
        return WalkFlow.CONTINUE;

    }

    @Override
    public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {

    }
}
