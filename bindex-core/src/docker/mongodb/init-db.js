db = db.getSiblingDB('machanism');

// Define the JSON schema for the bindex collection
var bindexSchema = {
  $jsonSchema: {
    bsonType: "object",
    required: [
      "_id",
      "bindex",
      "classification_embedding",
	  "description",
      "domains",
      "id",
      "integrations",
      "languages",
      "layers",
      "name",
      "version"
    ],
    properties: {
      "_id": {
        bsonType: "objectId"
      },
      "bindex": {
        bsonType: "string"
      },
      "classification_embedding": {
        bsonType: "array",
        items: {
          bsonType: "double"
        }
      },
      "domains": {
        bsonType: "array",
        items: {
          bsonType: "string"
        }
      },
      "id": {
        bsonType: "string"
      },
      "integrations": {
        bsonType: "array"
      },
      "languages": {
        bsonType: "array",
        items: {
          bsonType: "string"
        }
      },
      "layers": {
        bsonType: "array",
        items: {
          bsonType: "string"
        }
      },
      "name": {
        bsonType: "string"
      },
      "version": {
        bsonType: "string"
      }
    }
  }
};

// Create the bindex collection with the schema validator, or update if it exists
try {
  db.createCollection('bindex', { validator: bindexSchema });
  print("Collection 'bindex' created with schema validator.");
} catch (e) {
  db.runCommand({
    collMod: "bindex",
    validator: bindexSchema
  });
  print("Collection 'bindex' already exists. Schema validator updated.");
}

// Create Atlas Search index using 'definition' (Atlas Local syntax)
db.bindex.createSearchIndex(
   "id",
   {
     "mappings": {
       "dynamic": true
     }
   }
);

db.bindex.createSearchIndex(
  "vector_index", 
  "vectorSearch", 
  {
    "fields": [
      {
        "numDimensions": 700,
        "path": "classification_embedding",
        "similarity": "cosine",
        "type": "vector"
      }
    ]
  }
);

