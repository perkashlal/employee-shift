package it.unifi.attsw.employee_shift_scheduler;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.unifi.attsw.employee_shift_scheduler.db.MongoConnectionFactory;
import org.bson.Document;

public class MongoInsertCheck {
    public static void main(String[] args) {
        MongoDatabase db = MongoConnectionFactory.getDatabase();
        System.out.println("DB: " + db.getName());
        MongoCollection<Document> coll = db.getCollection("employees");

        Document doc = new Document("employeeId", "emp-cmd-1")
                .append("name", "Cmd Test")
                .append("role", "Tester");
        coll.insertOne(doc);
        System.out.println("Inserted id: " + doc.getObjectId("_id"));

        System.out.println("Now read:");
        System.out.println(coll.find().limit(10).into(new java.util.ArrayList<>()));
    }
}
