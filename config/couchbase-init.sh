#!/usr/bin/env sh

set -eu

HOST="${COUCHBASE_HOST:-couchbase}"
ADMIN_USER="${COUCHBASE_USERNAME:-Administrator}"
ADMIN_PASS="${COUCHBASE_PASSWORD:-password}"
BUCKET="${COUCHBASE_BUCKET:-todos}"
SCOPE="${COUCHBASE_SCOPE:-app}"
COLLECTION="${COUCHBASE_COLLECTION:-todos}"

echo "Waiting for Couchbase web endpoint..."
until curl -fsS "http://${HOST}:8091/ui/index.html" >/dev/null 2>&1; do
  sleep 2
done

echo "Initializing cluster (idempotent)..."
couchbase-cli cluster-init \
  -c "${HOST}" \
  --cluster-username "${ADMIN_USER}" \
  --cluster-password "${ADMIN_PASS}" \
  --services data,index,query \
  --cluster-ramsize 512 \
  --cluster-index-ramsize 256 \
  >/dev/null 2>&1 || true

echo "Waiting for admin API readiness..."
until couchbase-cli bucket-list \
  -c "${HOST}" \
  -u "${ADMIN_USER}" \
  -p "${ADMIN_PASS}" \
  >/dev/null 2>&1; do
  sleep 2
done

echo "Creating bucket '${BUCKET}' (idempotent)..."
couchbase-cli bucket-create \
  -c "${HOST}" \
  -u "${ADMIN_USER}" \
  -p "${ADMIN_PASS}" \
  --bucket "${BUCKET}" \
  --bucket-type couchbase \
  --bucket-ramsize 256 \
  --bucket-replica 0 \
  --storage-backend couchstore \
  --wait \
  >/dev/null 2>&1 || true

BUCKETS_JSON="$(curl -fsS -u "${ADMIN_USER}:${ADMIN_PASS}" "http://${HOST}:8091/pools/default/buckets")"
case "${BUCKETS_JSON}" in
  *"\"name\":\"${BUCKET}\""*) ;;
  *)
    echo "Bucket '${BUCKET}' was not created successfully."
    exit 1
    ;;
esac

echo "Creating scope '${SCOPE}' and collection '${COLLECTION}' (idempotent)..."
couchbase-cli collection-manage \
  -c "${HOST}" \
  -u "${ADMIN_USER}" \
  -p "${ADMIN_PASS}" \
  --bucket "${BUCKET}" \
  --create-scope "${SCOPE}" \
  >/dev/null 2>&1 || true

couchbase-cli collection-manage \
  -c "${HOST}" \
  -u "${ADMIN_USER}" \
  -p "${ADMIN_PASS}" \
  --bucket "${BUCKET}" \
  --create-collection "${SCOPE}.${COLLECTION}" \
  >/dev/null 2>&1 || true

echo "Couchbase init complete."
