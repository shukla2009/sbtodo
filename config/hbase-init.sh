#!/usr/bin/env sh

set -eu

TABLE_NAME="${HBASE_TABLE_NAME:-todos}"
COLUMN_FAMILY="${HBASE_COLUMN_FAMILY:-cf}"
HBASE_BIN="${HBASE_BIN:-/hbase/bin/hbase}"

echo "Waiting for HBase shell to become available..."
until echo "status 'simple'" | "${HBASE_BIN}" shell -n >/dev/null 2>&1; do
  sleep 2
done

echo "Checking if table '${TABLE_NAME}' exists..."
if echo "exists '${TABLE_NAME}'" | "${HBASE_BIN}" shell -n 2>/dev/null | rg -q "does exist"; then
  echo "Table '${TABLE_NAME}' already exists. Skipping creation."
else
  echo "Creating table '${TABLE_NAME}' with column family '${COLUMN_FAMILY}'..."
  echo "create '${TABLE_NAME}', '${COLUMN_FAMILY}'" | "${HBASE_BIN}" shell -n >/dev/null
  echo "Table '${TABLE_NAME}' created."
fi

echo "HBase init complete."
