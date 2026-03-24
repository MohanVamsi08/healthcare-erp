-- V13: Add performed_by to stock_transactions for user attribution
ALTER TABLE stock_transactions ADD COLUMN performed_by VARCHAR(255);
