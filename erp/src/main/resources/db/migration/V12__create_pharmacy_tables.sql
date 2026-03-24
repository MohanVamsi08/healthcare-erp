-- =============================================
-- Module 6: Pharmacy & Inventory
-- =============================================

-- 1. Suppliers (pharmaceutical distributors/vendors)
CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    gst_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Medicines (drug catalog with full traceability)
CREATE TABLE medicines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    supplier_id UUID REFERENCES suppliers(id),
    name VARCHAR(255) NOT NULL,
    generic_name VARCHAR(255),
    manufacturer VARCHAR(255),
    batch_number VARCHAR(100),
    expiry_date DATE,
    dosage_form VARCHAR(50) NOT NULL,
    strength VARCHAR(100),
    unit_price NUMERIC(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER NOT NULL DEFAULT 10,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Hospital Supplies (non-pharmaceutical items)
CREATE TABLE hospital_supplies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    supplier_id UUID REFERENCES suppliers(id),
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(255),
    batch_number VARCHAR(100),
    expiry_date DATE,
    unit VARCHAR(50),
    unit_price NUMERIC(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER NOT NULL DEFAULT 10,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Purchase Orders (with delivery traceability)
CREATE TABLE purchase_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) NOT NULL UNIQUE,
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ORDERED',
    total_amount NUMERIC(12,2),
    notes TEXT,
    ordered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_by VARCHAR(255),
    delivery_location VARCHAR(255),
    delivered_at TIMESTAMP,
    received_by VARCHAR(255),
    received_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Purchase Order Items (supports both medicines and supplies)
CREATE TABLE purchase_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id),
    medicine_id UUID REFERENCES medicines(id),
    supply_id UUID REFERENCES hospital_supplies(id),
    item_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_cost NUMERIC(10,2) NOT NULL
);

-- 6. Prescriptions
CREATE TABLE prescriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_number VARCHAR(50) NOT NULL UNIQUE,
    patient_id UUID NOT NULL REFERENCES patients(id),
    doctor_id UUID NOT NULL REFERENCES doctors(id),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    prescribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dispensed_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Prescription Items
CREATE TABLE prescription_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_id UUID NOT NULL REFERENCES prescriptions(id),
    medicine_id UUID NOT NULL REFERENCES medicines(id),
    quantity INTEGER NOT NULL,
    dosage_instructions VARCHAR(500)
);

-- 8. Supplier Returns
CREATE TABLE supplier_returns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_number VARCHAR(50) NOT NULL UNIQUE,
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    reason TEXT NOT NULL,
    initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. Supplier Return Items
CREATE TABLE supplier_return_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_id UUID NOT NULL REFERENCES supplier_returns(id),
    medicine_id UUID REFERENCES medicines(id),
    supply_id UUID REFERENCES hospital_supplies(id),
    item_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL
);

-- 10. Stock Transactions (immutable audit trail)
CREATE TABLE stock_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medicine_id UUID REFERENCES medicines(id),
    supply_id UUID REFERENCES hospital_supplies(id),
    item_type VARCHAR(20) NOT NULL,
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    transaction_type VARCHAR(20) NOT NULL,
    quantity_change INTEGER NOT NULL,
    reference_id UUID,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_suppliers_hospital ON suppliers(hospital_id);
CREATE INDEX idx_medicines_hospital ON medicines(hospital_id);
CREATE INDEX idx_medicines_supplier ON medicines(supplier_id);
CREATE INDEX idx_hospital_supplies_hospital ON hospital_supplies(hospital_id);
CREATE INDEX idx_purchase_orders_hospital ON purchase_orders(hospital_id);
CREATE INDEX idx_purchase_orders_supplier ON purchase_orders(supplier_id);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);
CREATE INDEX idx_prescriptions_hospital ON prescriptions(hospital_id);
CREATE INDEX idx_prescriptions_patient ON prescriptions(patient_id);
CREATE INDEX idx_prescriptions_doctor ON prescriptions(doctor_id);
CREATE INDEX idx_prescriptions_status ON prescriptions(status);
CREATE INDEX idx_supplier_returns_hospital ON supplier_returns(hospital_id);
CREATE INDEX idx_stock_transactions_hospital ON stock_transactions(hospital_id);
CREATE INDEX idx_stock_transactions_medicine ON stock_transactions(medicine_id);
CREATE INDEX idx_stock_transactions_supply ON stock_transactions(supply_id);
CREATE INDEX idx_stock_transactions_type ON stock_transactions(transaction_type);
