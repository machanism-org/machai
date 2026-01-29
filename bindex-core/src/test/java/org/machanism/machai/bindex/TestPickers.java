package org.machanism.machai.bindex;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.machanism.machai.ai.manager.GenAIProvider;

final class TestPickers {

    private TestPickers() {
    }

    static Picker newPickerWithoutMongo() throws Exception {
        Constructor<Picker> ctor = Picker.class.getDeclaredConstructor(GenAIProvider.class, String.class);
        ctor.setAccessible(true);

        // Create with a syntactically valid connection string; we will immediately swap the client
        // to prevent any network activity.
        Picker picker = ctor.newInstance(null, "mongodb://localhost:27017");

        Field mongoClientField = Picker.class.getDeclaredField("mongoClient");
        mongoClientField.setAccessible(true);
        mongoClientField.set(picker, new NoOpMongoClient());

        return picker;
    }
}
