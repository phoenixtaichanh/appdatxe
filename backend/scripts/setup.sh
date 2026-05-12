#!/bin/bash
# Setup script - chạy schema + seed data
# Usage: bash scripts/setup.sh
# Hoặc import thủ công qua MySQL Workbench / CLI:
#   mysql -u root -p < src/database/schema.sql
#   mysql -u root -p < src/database/seed.sql

echo "Setting up DoAn3 database..."
echo ""
echo "Step 1: Creating schema..."
mysql -u root -p1234 < src/database/schema.sql
echo "Step 2: Loading seed data..."
mysql -u root -p1234 < src/database/seed.sql
echo ""
echo "Done! Database 'doan3_db' is ready."
