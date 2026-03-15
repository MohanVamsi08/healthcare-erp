# Healthcare ERP — Complete Guided Walkthrough

> *Written for someone who doesn't know how to code. Every concept is explained from scratch.*

---

## 📁 What Are All These Files?

When you open the project, you see a lot of folders and files. Here's what they are and why they exist:

```
healthcare-erp/                     ← Your project (the whole thing)
├── docs/                           ← Documentation (the docs we created)
├── erp/                            ← The actual code lives here
│   ├── pom.xml                     ← "Shopping list" of tools/libraries we use
│   ├── src/main/java/...           ← All the Java code
│   └── src/main/resources/         ← Configuration & database scripts
└── .gitignore                      ← Tells Git which files to ignore
```

Think of it like a restaurant:
- `pom.xml` = the ingredient list (what we need from the store)
- `src/main/java/` = the kitchen (where the food is made)
- `src/main/resources/` = the recipe book & settings

---

## 🧩 The Code — Layer by Layer

Every feature in the project follows the **same pattern**. Once you understand it for ONE entity (like Hospital), you understand it for ALL of them.

### The Pattern: 6 Files Per Feature

```
For "Hospital" we have:
1. Hospital.java          (Model)       → "What does a hospital look like in the database?"
2. HospitalRepository.java (Repository) → "How do I talk to the database?"
3. HospitalService.java   (Service)     → "What are the rules when creating/updating?"
4. HospitalController.java (Controller) → "What URL triggers this action?"
5. HospitalDTO.java       (DTO)         → "What data do I send back to the user?"
6. V1__create_hospital.sql (Migration)  → "Create the table in the database"
```

The same pattern repeats for Department, Patient, and User. **If you understand these 6 files, you understand 80% of the project.**

---

## 📝 File-by-File Explanation

### 1. The Model — `Hospital.java`

> *"This is what a hospital looks like in the database."*

```java
@Entity                              // ← "This Java class = a database table"
@Table(name = "hospitals")           // ← "The table is called 'hospitals'"
public class Hospital {

    @Id                              // ← "This field is the primary key (unique ID)"
    @GeneratedValue                  // ← "The database generates this automatically"
    private UUID id;                 // ← A unique identifier like "a1b2c3d4-..."

    @Column(nullable = false)        // ← "This field is required (can't be empty)"
    private String name;             // ← The hospital's name

    @Column(unique = true)           // ← "No two hospitals can have the same GSTIN"
    private String gstin;            // ← Tax identification number

    private String address;          // ← Address (optional, no special rules)

    private boolean isActive = true; // ← Is this hospital still active? Default: yes
    private LocalDateTime createdAt; // ← When was this record created?
}
```

**What this does:** When Spring Boot starts, it looks at this file and says: *"Oh, I need a table called 'hospitals' with columns: id, name, gstin, address, is_active, created_at."* That's it. You never write SQL manually — Java creates the table for you.

**The @ symbols** are called **annotations**. They're instructions to Spring Boot:
- `@Entity` = "this is a database table"
- `@Column(nullable = false)` = "this column can't be empty"
- `@Id` = "this is the primary key"

---

### 2. The Repository — `HospitalRepository.java`

> *"How do I talk to the database?"*

```java
@Repository
public interface HospitalRepository extends JpaRepository<Hospital, UUID> {
    // That's it. Literally just this.
}
```

**Wait, where's the code?** There isn't any! This is the magic of Spring Data JPA. By writing just this ONE line (`extends JpaRepository<Hospital, UUID>`), you automatically get:

| Method you get for FREE | What SQL it runs |
|------------------------|-----------------|
| `findAll()` | `SELECT * FROM hospitals` |
| `findById(id)` | `SELECT * FROM hospitals WHERE id = ?` |
| `save(hospital)` | `INSERT INTO hospitals ...` or `UPDATE hospitals ...` |
| `deleteById(id)` | `DELETE FROM hospitals WHERE id = ?` |
| `existsById(id)` | `SELECT COUNT(*) FROM hospitals WHERE id = ?` |

You can also create custom queries just by naming a method. For example, in `UserRepository`:
```java
findByEmail(String email)  → Spring generates: SELECT * FROM users WHERE email = ?
```

Spring reads the method name and figures out the SQL. No SQL writing needed.

---

### 3. The Service — `HospitalService.java`

> *"What are the business rules?"*

```java
@Service                                    // ← "This is a service (business logic)"
@RequiredArgsConstructor                    // ← "Auto-inject the repository"
public class HospitalService {

    private final HospitalRepository hospitalRepository;  // ← Injected automatically

    public HospitalDTO create(HospitalDTO dto) {
        Hospital hospital = dto.toEntity();               // ← Convert DTO → Entity
        Hospital saved = hospitalRepository.save(hospital); // ← Save to database
        return HospitalDTO.fromEntity(saved);              // ← Convert Entity → DTO
    }

    public HospitalDTO getById(UUID id) {
        Hospital hospital = hospitalRepository.findById(id)
            .orElseThrow(() →                              // ← If not found...
                new ResourceNotFoundException("Hospital", id)); // ← ...throw error
        return HospitalDTO.fromEntity(hospital);
    }
}
```

**What this does:**
- `create()` → Takes data from the user, saves it to the database, returns the result
- `getById()` → Looks up a hospital by ID. If it doesn't exist, throws a "not found" error
- `update()` → Finds the hospital, changes its fields, saves it back
- `delete()` → Checks if it exists, then deletes it

**Why not just do this in the Controller?** Separation of concerns. The Controller only knows about HTTP (requests/responses). The Service knows about business rules. The Repository knows about the database. If you change your database, only the Repository changes. If you change your API, only the Controller changes. Nothing else breaks.

---

### 4. The Controller — `HospitalController.java`

> *"What URL triggers this action?"*

```java
@RestController                           // ← "I handle HTTP requests and return JSON"
@RequestMapping("/api/hospitals")         // ← "All my URLs start with /api/hospitals"
public class HospitalController {

    private final HospitalService service;

    @GetMapping                           // ← GET /api/hospitals
    public ResponseEntity<List<HospitalDTO>> getAll() {
        return ResponseEntity.ok(service.getAllHospitals());
    }

    @GetMapping("/{id}")                  // ← GET /api/hospitals/abc-123
    public ResponseEntity<HospitalDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping                          // ← POST /api/hospitals (create)
    public ResponseEntity<HospitalDTO> create(@RequestBody HospitalDTO dto) {
        HospitalDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")               // ← DELETE /api/hospitals/abc-123
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**The annotations tell Spring which HTTP method + URL maps to which function:**
| Annotation | HTTP Method | Example URL |
|-----------|-------------|-------------|
| `@GetMapping` | GET | `GET /api/hospitals` |
| `@PostMapping` | POST | `POST /api/hospitals` |
| `@PutMapping("/{id}")` | PUT | `PUT /api/hospitals/abc-123` |
| `@DeleteMapping("/{id}")` | DELETE | `DELETE /api/hospitals/abc-123` |

**`@PathVariable`** grabs values from the URL: `/api/hospitals/{id}` → `id = "abc-123"`
**`@RequestBody`** grabs the JSON body from the request

---

### 5. The DTO — `HospitalDTO.java`

> *"What data do I send back to the user?"*

```java
public record HospitalDTO(           // ← "record" = a simple data container
    UUID id,
    String name,
    String gstin,
    String stateCode,
    String address,
    Boolean isActive,
    LocalDateTime createdAt
) {
    // Convert Entity → DTO (for sending to client)
    public static HospitalDTO fromEntity(Hospital hospital) { ... }

    // Convert DTO → Entity (for saving to database)
    public Hospital toEntity() { ... }
}
```

**Why not just send the Entity directly?**
- The Entity might have sensitive fields (like passwords in User)
- The Entity might have circular references (Department has Hospital, Hospital has Departments...)
- The DTO is a clean, flat object designed for the API

---

### 6. The Migration — `V1__create_hospital_table.sql`

```sql
CREATE TABLE hospitals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    gstin VARCHAR(15) UNIQUE,
    state_code VARCHAR(2) NOT NULL,
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**What this does:** This is raw SQL that Flyway runs once to create the table. The `V1` in the filename means "version 1". Flyway tracks which versions have run and never runs them twice.

---

## 🔐 Module 2: Security — How It All Fits

### The Login Flow (step by step)

```
Step 1: You send → POST /api/auth/login { "email": "admin@...", "password": "admin123" }

Step 2: AuthController receives the request
            ↓
Step 3: AuthService checks the password
        - Loads the user from the database (via UserRepository)
        - Compares the password using BCrypt (hashing)
        - If wrong → throws "Invalid email or password"
            ↓
Step 4: JwtService creates a token
        - Puts inside: userId, email, role, hospitalId
        - Signs it with a secret key
        - Sets expiration: 24 hours
            ↓
Step 5: Returns → { "token": "eyJhbG...", "email": "...", "role": "SUPER_ADMIN" }
```

### What Happens On Every Request After Login

```
Step 1: You send → GET /api/hospitals
        Headers: Authorization: Bearer eyJhbG...
            ↓
Step 2: JwtAuthenticationFilter intercepts (runs before the Controller)
        - Extracts token from "Authorization: Bearer <token>"
        - Asks JwtService: "Is this token valid?"
        - If expired or fake → 403 Forbidden, stop here
            ↓
Step 3: If valid → loads the user's details from database
        - Sets in SecurityContext: "This request is from admin@healthcare-erp.com, role: SUPER_ADMIN"
            ↓
Step 4: SecurityConfig checks: "Is /api/hospitals accessible to authenticated users?"
        - Yes → let through
            ↓
Step 5: HospitalController.getAll() runs normally
            ↓
Step 6: Returns the list of hospitals as JSON
```

### The Role Hierarchy (Who Creates Who)

```
App starts → DataSeeder creates SUPER_ADMIN (admin@healthcare-erp.com)
    ↓
SUPER_ADMIN logs in → gets JWT token
    ↓
SUPER_ADMIN calls POST /api/admin/users → creates HOSPITAL_ADMIN for "Apollo"
    ↓
HOSPITAL_ADMIN logs in → gets JWT token (contains hospitalId)
    ↓
HOSPITAL_ADMIN calls POST /api/hospitals/{id}/users → creates DOCTOR
    ↓
DOCTOR logs in → can only see patients in their hospital
```

---

## 🗂️ Complete File Map

```
erp/src/main/java/com/healthcare/erp/
│
├── ErpApplication.java              ← App starts here (the "main" function)
│
├── model/                           ← DATABASE TABLES
│   ├── Hospital.java                ← hospitals table
│   ├── Department.java              ← departments table (belongs to hospital)
│   ├── Patient.java                 ← patients table (belongs to hospital)
│   ├── User.java                    ← users table (belongs to hospital)
│   └── Role.java                    ← enum: SUPER_ADMIN, DOCTOR, NURSE, etc.
│
├── repository/                      ← DATABASE QUERIES
│   ├── HospitalRepository.java      ← findAll, findById, save, delete
│   ├── DepartmentRepository.java    ← + findByHospitalId
│   ├── PatientRepository.java       ← + findByHospitalId
│   └── UserRepository.java          ← + findByEmail, existsByEmail
│
├── service/                         ← BUSINESS LOGIC
│   ├── HospitalService.java         ← CRUD for hospitals
│   ├── DepartmentService.java       ← CRUD for departments
│   ├── PatientService.java          ← CRUD for patients
│   ├── AuthService.java             ← Login logic (verify password → JWT)
│   └── UserService.java             ← Create users (with role enforcement)
│
├── controller/                      ← API ENDPOINTS
│   ├── RootController.java          ← GET / (welcome page)
│   ├── HospitalController.java      ← /api/hospitals
│   ├── DepartmentController.java    ← /api/hospitals/{id}/departments
│   ├── PatientController.java       ← /api/hospitals/{id}/patients
│   ├── AuthController.java          ← POST /api/auth/login
│   └── UserController.java          ← /api/admin/users, /api/hospitals/{id}/users
│
├── dto/                             ← API REQUEST/RESPONSE SHAPES
│   ├── HospitalDTO.java
│   ├── DepartmentDTO.java
│   ├── PatientDTO.java
│   ├── LoginRequest.java            ← { email, password }
│   ├── AuthResponse.java            ← { token, email, role, hospitalId }
│   ├── CreateUserRequest.java       ← { email, password, firstName, lastName, role }
│   └── UserDTO.java                 ← User info without password
│
├── security/                        ← AUTHENTICATION & AUTHORIZATION
│   ├── JwtService.java              ← Creates & validates JWT tokens
│   ├── JwtAuthenticationFilter.java ← Checks every request for a valid token
│   ├── CustomUserDetailsService.java← Loads user from DB for Spring Security
│   └── SecurityConfig.java          ← Which endpoints are public vs protected
│
├── config/
│   └── DataSeeder.java              ← Creates default SUPER_ADMIN on startup
│
└── exception/                       ← ERROR HANDLING
    ├── GlobalExceptionHandler.java  ← Catches errors → clean JSON responses
    ├── ResourceNotFoundException.java← "Hospital not found" type errors
    └── ErrorResponse.java           ← { status: 404, message: "...", timestamp }
```

---

## 🔑 Key Vocabulary Cheat Sheet

| Term | What It Means | Real-World Analogy |
|------|-------------|-------------------|
| **Entity / Model** | A Java class = a database table | A blank form template |
| **Repository** | Talks to the database | The filing cabinet clerk |
| **Service** | Business logic layer | The manager who enforces rules |
| **Controller** | Handles HTTP requests | The receptionist |
| **DTO** | Data shape for API responses | The summary report you hand to someone |
| **Migration** | SQL script to create/change tables | Building blueprints |
| **JWT** | Login token (like a wristband) | A concert wristband — proves you paid |
| **BCrypt** | Password hashing algorithm | Shredding a document — can't be un-shredded |
| **RBAC** | Role-Based Access Control | Keycards — different roles open different doors |
| **API** | Application Programming Interface | A restaurant menu — lists what you can order |
| **CRUD** | Create, Read, Update, Delete | The 4 things you can do with any data |
| **UUID** | Universally Unique Identifier | A random ID like "a1b2c3d4-e5f6-..." |
| **Annotation (@)** | Instructions to Spring Boot | Post-it notes on your code telling the framework what to do |
| **Endpoint** | A URL that does something | A door in a building — each leads somewhere |
