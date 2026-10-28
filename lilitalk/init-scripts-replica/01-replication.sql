-- PostgreSQL Physical Streaming Replication is initialized via pg_basebackup with -R (generating standby.signal and primary_conninfo).
-- Verify that the replica is actively running in recovery mode and connected to the primary:
SELECT pg_is_in_recovery();
SELECT *
FROM pg_stat_wal_receiver;