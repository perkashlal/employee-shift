package it.unifi.attsw.employee_shift_scheduler;

import it.unifi.attsw.employee_shift_scheduler.repository.EmployeeRepository;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;

import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB implementation of EmployeeRepository.
 * Supports:
 *  - Constructor(uri, dbName, collName)
 *  - Constructor(host, port, user, pass, authDb, dbName, collName)
 *  - Constructor(MongoDatabase)  <-- required for TestContainers
 *
 * Persists Employee + Shifts as:
 * {
 *   employeeId: "...",
 *   name: "...",
 *   role: "...",
 *   shifts: [
 *      { id: "...", start: "...", end: "..." }
 *   ]
 * }
 */
public class MongoEmployeeRepository implements EmployeeRepository {

    private final MongoClient client;   // may be null when DB injected externally
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    // ------------------------------------------------------------
    //        1. Constructor for URI (mongodb://user:pass@host:port)
    // ------------------------------------------------------------
    public MongoEmployeeRepository(String uri, String dbName, String collName) {
        this.client = MongoClients.create(uri);
        this.database = client.getDatabase(dbName);
        this.collection = database.getCollection(collName);

        System.out.println("MongoEmployeeRepository (URI) connected to: "
                + database.getName() + "." + collName);
    }

    // ------------------------------------------------------------
    //  2. Credentials Constructor (host, port, username, password)
    // ------------------------------------------------------------
    public MongoEmployeeRepository(String host, int port,
                                   String username, String password,
                                   String authDb, String dbName, String collName) {

        MongoCredential credential =
                MongoCredential.createCredential(username, authDb, password.toCharArray());

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(b -> b.hosts(Arrays.asList(new ServerAddress(host, port))))
                .credential(credential)
                .build();

        this.client = MongoClients.create(settings);
        this.database = client.getDatabase(dbName);
        this.collection = database.getCollection(collName);

        System.out.println("MongoEmployeeRepository (Credentials) connected to: "
                + database.getName() + "." + collName);
    }

    // ------------------------------------------------------------
    //            3. REQUIRED BY TESTCONTAINERS (IMPORTANT)
    // ------------------------------------------------------------
    // MongoQuickTest uses:
    //      new MongoEmployeeRepository(MongoDatabase)
    // ------------------------------------------------------------
    public MongoEmployeeRepository(MongoDatabase injectedDb) {
        this.client = null; // not owned by this repo (TestContainers manages it)
        this.database = injectedDb;
        this.collection = database.getCollection("employees");

        System.out.println("MongoEmployeeRepository (Injected DB) connected to: "
                + database.getName() + ".employees");
    }

    // ------------------------------------------------------------
    //                    Helpers for persistence
    // ------------------------------------------------------------

    private Document toDoc(Employee e) {
        Document d = new Document();
        d.append("employeeId", e.getEmployeeId());
        d.append("name", e.getName());
        d.append("role", e.getRole());

        List<Shift> shifts = e.getScheduledShifts();
        List<Document> shiftDocs = new ArrayList<>();

        for (Shift s : shifts) {
            Document sd = new Document();
            sd.append("id", s.getId());

            Instant st = s.getStart();
            Instant en = s.getEnd();

            if (st != null) sd.append("start", st.toString());
            if (en != null) sd.append("end", en.toString());

            shiftDocs.add(sd);
        }
        d.append("shifts", shiftDocs);

        return d;
    }

    private Employee fromDoc(Document d) {
        String employeeId = d.getString("employeeId");
        String name = d.getString("name");
        String role = d.getString("role");

        List<Shift> shifts = new ArrayList<>();

        List<Document> shiftDocs = d.getList("shifts", Document.class, new ArrayList<>());

        for (Document sd : shiftDocs) {
            String id = sd.getString("id");
            Instant st = null;
            Instant en = null;
            try {
                String s1 = sd.getString("start");
                if (s1 != null) st = Instant.parse(s1);
            } catch (Exception ignore) {}

            try {
                String s2 = sd.getString("end");
                if (s2 != null) en = Instant.parse(s2);
            } catch (Exception ignore) {}

            // Create Shift(id,start,end)
            Shift sh;
            if (id != null)
                sh = new Shift(id, st, en);
            else
                sh = new Shift(st, en);

            shifts.add(sh);
        }

        return new Employee(name, employeeId, role, shifts);
    }

    // ------------------------------------------------------------
    //                 REPOSITORY INTERFACE METHODS
    // ------------------------------------------------------------

    @Override
    public Employee save(Employee employee) {
        Document doc = toDoc(employee);

        Document existing =
                collection.find(Filters.eq("employeeId", employee.getEmployeeId())).first();

        if (existing != null) {
            collection.replaceOne(Filters.eq("employeeId", employee.getEmployeeId()), doc);
        } else {
            InsertOneResult res = collection.insertOne(doc);
            if (!res.wasAcknowledged())
                throw new RuntimeException("Insert not acknowledged");
        }
        return employee;
    }

    @Override
    public Optional<Employee> findById(String id) {
        Document d = collection.find(Filters.eq("employeeId", id)).first();
        return d == null ? Optional.empty() : Optional.of(fromDoc(d));
    }

    @Override
    public List<Employee> findAll() {
        List<Employee> out = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                out.add(fromDoc(cursor.next()));
            }
        }
        return out;
    }

    @Override
    public void deleteById(String id) {
        collection.deleteOne(Filters.eq("employeeId", id));
    }

    /** If owned client exists, close it. (Injected DB case: client == null.) */
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
