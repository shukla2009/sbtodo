#!/usr/bin/env sh

set -eu

HOST="${TIGERGRAPH_HOST:-tigergraph}"
RESTPP_PORT="${TIGERGRAPH_RESTPP_PORT:-14240}"

echo "Waiting for TigerGraph RESTPP endpoint..."
until curl -fsS "http://${HOST}:${RESTPP_PORT}/version" >/dev/null 2>&1; do
  sleep 2
done

echo "Applying TigerGraph schema (idempotent)..."
gsql -ip "${HOST}" "CREATE VERTEX Todo (PRIMARY_ID id STRING, title STRING, description STRING, completed BOOL, createdAt STRING, updatedAt STRING) WITH primary_id_as_attribute=\"true\"" >/dev/null 2>&1 || true
gsql -ip "${HOST}" "CREATE GRAPH todos_graph(Todo)" >/dev/null 2>&1 || true

echo "TigerGraph init complete."
