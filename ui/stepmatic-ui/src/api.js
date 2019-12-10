// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

/**
 * JSON API Serializer
 * See: https://github.com/SeyZ/jsonapi-serializer
 */

/*

### Deserialization

    var JSONAPIDeserializer = require('jsonapi-serializer').Deserializer;
    new JSONAPIDeserializer(opts).deserialize(data);

The function `JSONAPIDeserializer` takes one argument:

- `opts`: The deserializer options.

Calling the `deserialize` method on the returned object will deserialize your `data` (JSONAPI document) to a plain javascript object.

#### Available deserialization option (`opts` argument)

- **keyForAttribute**: A function or string to customize attributes. Functions are passed the attribute as a single argument and expect a string to be returned. Strings are aliases for inbuilt functions for common case conversions. Options include: `dash-case` (default), `lisp-case`, `spinal-case`, `kebab-case`, `underscore_case`, `snake_case`, `camelCase`, `CamelCase`.
- **AN\_ATTRIBUTE\_TYPE**: this option name corresponds to the type of a relationship from your JSONAPI document.
	- **valueForRelationship**: A function that returns whatever you want for a relationship (see examples below) ***can return a Promise (see tests)***
   - **transform**: A function to transform each record after the deserialization.

**Examples**

- [Simple usage](#simple-usage-deserializer)
- [Relationship](#relationship-deserializer)
- [More examples in tests](https://github.com/SeyZ/jsonapi-serializer/blob/master/test/deserializer.js)

 --------------*/
let JSONAPIDeserializer = require('jsonapi-serializer').Deserializer,
  deserializedRelationship = {
    valueForRelationship: function (rel, inc) {
      return inc ? inc : {
        id: rel.id,
        type: rel.type
      }
    }
  },
  deserializerOptions = {
    "user": deserializedRelationship,
    "library": deserializedRelationship,
    "program-memes": deserializedRelationship,
    "sequences": deserializedRelationship,
    "sequence-chords": deserializedRelationship,
    "sequence-bindings": deserializedRelationship,
    "sequence-binding-memes": deserializedRelationship,
    "voices": deserializedRelationship,
    "patterns": deserializedRelationship,
    "tracks": deserializedRelationship,
    "events": deserializedRelationship,
  };

/*

### Serialization

    var JSONAPISerializer = require('jsonapi-serializer').Serializer;
    new JSONAPISerializer(type, opts).serialize(data);

The function `JSONAPISerializer` takes two arguments:

- `type`: The resource type.
- `opts`: The serialization options.

Calling the `serialize` method on the returned object will serialize your `data` (object or array) to a compliant JSONAPI document.


#### Available serialization option (`opts` argument)

    - **attributes**: An array of attributes to show. You can define an attribute as an option if you want to define some relationships (included or not).
    - **ref**: If present, it's considered as a relationships.
    - **included**: Consider the relationships as [compound document](http://jsonapi.org/format/#document-compound-documents). Default: true.
    - **id**: Configurable identifier field for the resource. Default: `id`.
    - **attributes**: An array of attributes to show.
    - **topLevelLinks**: An object that describes the top-level links. Values can be *string* or a *function*
    - **dataLinks**: An object that describes the links inside data. Values can be *string* or a *function* (see examples below)
    - **dataMeta**: An object that describes the meta inside data. Values can be a plain value or a *function* (see examples below)
    - **relationshipLinks**: An object that describes the links inside relationships. Values can be *string* or a *function*
    - **relationshipMeta**: An object that describes the meta inside relationships. Values can be a plain value or a *function*
    - **ignoreRelationshipData**: Do not include the `data` key inside the relationship. Default: false.
    - **keyForAttribute**: A function or string to customize attributes. Functions are passed the attribute as a single argument and expect a string to be returned. Strings are aliases for inbuilt functions for common case conversions. Options include: `dash-case` (default), `lisp-case`, `spinal-case`, `kebab-case`, `underscore_case`, `snake_case`, `camelCase`, `CamelCase`.
    - **nullIfMissing**: Set the attribute to null if missing from your data input. Default: false.
    - **pluralizeType**: A boolean to indicate if the type must be pluralized or not. Default: true.
    - **typeForAttribute**: A function that maps the attribute (passed as an argument) to the type you want to override. If it returns `undefined`, ignores the flag for that attribute. Option *pluralizeType* ignored if set.
    - **meta**: An object to include non-standard meta-information. Values can be a plain value or a *function*
    - **transform**: A function to transform each record before the serialization.

**Examples**

- [Express example](https://github.com/SeyZ/jsonapi-serializer/tree/master/examples/express)
- [Simple usage](#simple-usage-serializer)
- [More examples in tests](https://github.com/SeyZ/jsonapi-serializer/blob/master/test/serializer.js)

 ------------*/
let JSONAPISerializer = require('jsonapi-serializer').Serializer,
  ref = function (parent, relation) {
    if (relation && relation.hasOwnProperty('id'))
      return relation.id;

    // console.warn("unknown relationship", this, parent, relation);
    return null;
  },
  serializerOptions = {
    attributes: [
      'density',
      'key',
      'name',
      'state',
      'tempo',
      // relationships
      'events',
      'patterns',
      'program-memes',
      'sequence-binding-memes',
      'sequence-bindings',
      'sequence-chords',
      'sequences',
      'tracks',
      'voices',
    ],
    keyForAttribute: 'camelCase',
    'events': {
      ref: ref,
      attributes: [
        'duration',
        'note',
        'position',
        'velocity',
        // relationships
        'pattern',
        'track',
      ],
      'track': {
        ref: ref,
      },
      'pattern': {
        ref: ref,
      },
    },
    'patterns': {
      ref: ref,
      attributes: [
        'type',
        'total',
        'name',
        // relationships
        'program',
        'sequence',
        'voice',
        'events',
      ],
      'program': {
        ref: ref,
      },
      'sequence': {
        ref: ref,
      },
      'voice': {
        ref: ref,
      },
      'events': {
        ref: ref,
      },
    },
    'program-memes': {
      ref: ref,
      attributes: [
        'name',
      ],
    },
    'sequence-binding-memes': {
      ref: ref,
      attributes: [
        'sequence-binding',
        'name',
      ],
      'sequence-binding': {
        ref: ref,
      },
    },
    'sequence-bindings': {
      ref: ref,
      attributes: [
        'sequence',
        'offset',
      ],
      'sequence': {
        ref: ref,
      },
    },
    'sequence-chords': {
      ref: ref,
      attributes: [
        'sequence',
        'name',
        'position',
      ],
      'sequence': {
        ref: ref,
      },
    },
    'sequences': {
      ref: ref,
      attributes: [
        'name',
        'key',
        'density',
        'total',
        'tempo',
      ],
    },
    'tracks': {
      ref: ref,
      attributes: [
        'voice',
        'name',
      ],
      'voice': {
        ref: ref,
      },
    },
    'voices': {
      ref: ref,
      attributes: [
        'type',
        'name',
      ],
    }
  };

export default {
  deserializer: new JSONAPIDeserializer(deserializerOptions),
  serializer: new JSONAPISerializer('programs', serializerOptions),
}
