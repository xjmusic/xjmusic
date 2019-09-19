let JSONAPIDeserializer = require('jsonapi-serializer').Deserializer;
let DeserializedRelationship = {
  valueForRelationship: function (rel, inc) {
    return inc ? inc : {
      id: rel.id,
      type: rel.type
    }
  }
};

let ProgramDeserializer = new JSONAPIDeserializer({
  "user": DeserializedRelationship,
  "library": DeserializedRelationship,
  "program-memes": DeserializedRelationship,
  "sequences": DeserializedRelationship,
  "sequence-chords": DeserializedRelationship,
  "sequence-bindings": DeserializedRelationship,
  "sequence-binding-memes": DeserializedRelationship,
  "voices": DeserializedRelationship,
  "patterns": DeserializedRelationship,
  "pattern-events": DeserializedRelationship,
});

export default {
  deserialize: ProgramDeserializer.deserialize
}
