# qbit-wms

QBit plugin for the QQQ platform: a complete Warehouse Management System (46 tables, 47 processes, 11 widgets, task-centric work engine, perpetual inventory ledger).

## Knowledge base

Reviewed dossiers for this repo and the wider QQQ platform live in the second-brain vault:

- Platform hub: `R:/Git.Local/KofTwentyTwo/second-brain/knowledge/qqq/qqq-hub.md`
- QBit mechanics: `R:/Git.Local/KofTwentyTwo/second-brain/knowledge/qqq/architecture/metadata-model.md`
- This repo's dossier: `R:/Git.Local/KofTwentyTwo/second-brain/knowledge/qqq/repos/qbit-wms.md`

Dossier reviewed at commit `f8c255148974` (branch `develop`, 2026-07-04). Key facts captured there: qqq pin 0.27.9 via `qbit-build-parent:1.4.0`, AGPL-3.0 licensing (no source headers), Gen-2 `QBitMetaDataProducer` contract, dead-config findings (only `backendName` + `recordSecurityLocks` consumed), phase plan tracked in QRun-IO/qqq issues #420-431, and v4.0 upgrade exposure (test-only BREAK-04-11 hit; Java 17 vs 21 floor).
