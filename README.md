# QBit: WMS

[![Version](https://img.shields.io/badge/version-0.1.0-blue.svg)](https://github.com/QRun-IO/qbit-wms)
[![License](https://img.shields.io/badge/license-GNU%20Affero%20GPL%20v3-green.svg)](https://www.gnu.org/licenses/agpl-3.0.en.html)
[![Java](https://img.shields.io/badge/java-21+-blue.svg)](https://adoptium.net/)

> **Warehouse Management System for QQQ Applications - Task-Centric, Perpetual Inventory**

This QBit provides a complete WMS for QQQ applications, covering receiving, putaway, inventory management, order fulfillment (pick/pack/ship), returns processing, kitting, cycle counting, replenishment, and 3PL billing. The system is metadata-driven, multi-warehouse capable, and supports both brand-direct and 3PL operating models.

## Core Capabilities

- **Task-Centric Work Engine**: A unified `wms_task` table is the heart of the system. Every warehouse operation (putaway, picking, packing, counting, moving, replenishment, loading, QC inspection, kit assembly) creates directed work tasks. Workers interact through a single task queue. Supervisors monitor, reprioritize, and reassign tasks.
- **Perpetual Inventory Through Transactions**: The `wms_inventory_transaction` table is the authoritative ledger. Every quantity change is recorded as an immutable transaction first. Inventory can be fully reconstructed from the transaction log at any point in time.
- **Receiving & Putaway**: PO-based, blind, and ASN receiving with directed or manual putaway. QC inspection gates putaway when required.
- **Order Fulfillment**: Configurable allocation (FEFO/FIFO/LIFO), wave-based pick release, cartonization, and short-pick resolution.
- **Shipping**: Carrier label generation (parcel and LTL/FTL), manifesting, dock appointment scheduling, and ship confirmation.
- **Returns Processing**: Full RMA lifecycle with inspection grading, disposition rules, and restock/scrap handling.
- **Kitting**: Bill-of-materials-driven kit assembly flowing through the task engine with component deduction and finished kit creation.
- **Cycle Counting**: ABC-based, location-based, SKU-based, and random counting with blind count support and supervisor variance approval.
- **Replenishment**: Rule-based pick-face replenishment with configurable min/max thresholds.
- **3PL Billing**: Activity-based billing automatically captured from task completion hooks. Rate cards, invoicing, and storage snapshots.
- **Multi-Warehouse**: Warehouse-scoped data isolation from day one via `warehouse_id` FK and RecordSecurityLock.
- **License Plate Tracking**: First-class LPN entity for pallet-level operations and container lifecycle tracking.
- **11 Dashboard Widgets**: Task queue summary, worker productivity, fulfillment pipeline, SLA risk, inventory accuracy, billing dashboard, and more.

## Open Source & Full Control

QBit WMS is 100% open source under AGPL v3. All data stays in your systems. No external WMS services required.

## Architecture

### Design Principles

1. **Task-centric work engine**: A unified `wms_task` table sits at the heart of the system. Every warehouse operation, from putaway to picking to cycle counting, creates directed work tasks. Workers interact with the system through a single task queue. Supervisors monitor, reprioritize, and reassign tasks. This gives the warehouse a single source of truth for "what work needs to happen" and "who is doing it."

2. **Perpetual inventory through transactions**: The `wms_inventory_transaction` table is the authoritative ledger. Inventory quantities on `wms_inventory` are never modified directly. Every quantity change is recorded as an immutable transaction first, and `wms_inventory.quantity_on_hand` is updated as a consequence. Inventory can be fully reconstructed from the transaction ledger at any point in time, and every unit movement is traceable to the task, user, and timestamp that caused it.

3. **Completion hook dispatcher**: When a task is completed, a `TaskCompletionDispatcher` switches on `task_type` and calls the appropriate type-specific handler. Each handler contains the full chain of side effects (deduct inventory, update order lines, log transactions, create downstream tasks, capture billing activities). This explicit dispatcher pattern keeps completion logic testable in isolation and avoids unintended triggering from non-completion task updates.

### Technology Stack

- **Java 21+** with QQQ backend modules
- **QQQ Framework**: Entities, processes, widgets, permissions, API layer
- **Database**: RDBMS through QQQ's backend abstraction
- **MemoryRecordStore**: Full test suite runs in-memory (no database required)

### Module Organization

```
qbit-wms/
  src/main/java/com/kingsrook/qbits/wms/
    WmsQBitConfig.java              -- Configuration (backend, strategies, companion QBits)
    WmsQBitProducer.java            -- Gen 2 QBitMetaDataProducer (auto-discovers all entities)
    core/                           -- Warehouse, Zone, Location, Item, ItemCategory, UOM, Client, Vendor
    tasks/                          -- Unified task engine (wms_task, task type config, task processes)
    receiving/                      -- POs, Receipts, ASNs, Putaway rules
    inventory/                      -- Tracking, Counting, Adjustments, Holds, License Plates
    fulfillment/                    -- Orders, Waves, Cartons, Allocation, Kitting
    shipping/                       -- Shipments, Manifests, Dock Appointments, Labels
    returns/                        -- RMA, Inspection, Disposition
    billing/                        -- 3PL activity capture, Rate Cards, Invoicing
    widgets/                        -- 11 dashboard widgets (producers + renderers)
```

## Getting Started

### Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **QQQ Application** (this is a QBit, not a standalone application)

### Usage

#### Maven dependency

```xml
<dependency>
    <groupId>com.kingsrook.qbits</groupId>
    <artifactId>qbit-wms</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

#### Minimal setup

```java
WmsQBitConfig config = new WmsQBitConfig()
   .withBackendName("yourBackendName");

WmsQBitProducer producer = new WmsQBitProducer()
   .withQBitConfig(config);

MetaDataProducerMultiOutput output = producer.produce(qInstance);
output.addSelfToInstance(qInstance);
```

Default security locks (warehouseId DENY + clientId ALLOW) and permission rules are applied automatically in `postProduceActions`.

#### With custom security locks

```java
WmsQBitConfig config = new WmsQBitConfig()
   .withBackendName("yourBackendName")
   .withRecordSecurityLocks(List.of(
      new RecordSecurityLock()
         .withFieldName("warehouseId")
         .withSecurityKeyType("warehouseAccess")
         .withNullValueBehavior(RecordSecurityLock.NullValueBehavior.DENY),
      new RecordSecurityLock()
         .withFieldName("clientId")
         .withSecurityKeyType("clientAccess")
         .withNullValueBehavior(RecordSecurityLock.NullValueBehavior.ALLOW)
   ));
```

## Data Model

### Tables (46 total)

#### Core

| Table | Description |
|-------|-------------|
| `wmsWarehouse` | Top-level organizational unit with timezone and address |
| `wmsZone` | Logical grouping of locations (Bulk, Pick Face, Cold, Hazmat, Staging, etc.) |
| `wmsLocation` | Individual storage positions (bins, shelves, floor spots) with capacity tracking |
| `wmsItem` | Product/SKU master with lot, serial, expiration tracking and velocity classification |
| `wmsItemCategory` | Hierarchical item categories with default storage requirements |
| `wmsUnitOfMeasure` | Item-specific UOM conversions (Each, Case, Pallet, Custom) |
| `wmsClient` | 3PL client records (empty in brand-direct mode) |
| `wmsVendor` | Supplier/vendor master with lead time and contact info |

#### Task Engine

| Table | Description |
|-------|-------------|
| `wmsTask` | Central work table for all warehouse operations (10 task types, 8 statuses) |
| `wmsTaskTypeConfig` | Configurable defaults per task type per warehouse (priority, equipment, auto-assign, scan requirements) |

#### Receiving

| Table | Description |
|-------|-------------|
| `wmsPurchaseOrder` | Inbound purchase orders with vendor reference and dock door assignment |
| `wmsPurchaseOrderLine` | PO line items with over/under receiving tolerance percentages |
| `wmsReceipt` | Receiving events (PO-based, blind, ASN, return, cross-dock) |
| `wmsReceiptLine` | Receipt line items with QC status tracking through putaway |
| `wmsAsn` | Advance ship notices with carrier and tracking info |
| `wmsAsnLine` | ASN line-level detail for pre-validation and variance detection |

#### Inventory

| Table | Description |
|-------|-------------|
| `wmsInventory` | Core inventory record at item+location+lot+status intersection |
| `wmsInventoryTransaction` | Immutable append-only ledger of every inventory movement |
| `wmsCycleCount` | Count plans (full, ABC-based, location-based, SKU-based, random) |
| `wmsCycleCountLine` | Individual count lines with variance tracking and supervisor approval |
| `wmsInventoryHold` | Hold records for QC, damage, recall, regulatory, and custom holds |
| `wmsLicensePlate` | First-class LPN entity for pallets, cases, and totes |

#### Fulfillment

| Table | Description |
|-------|-------------|
| `wmsOrder` | Customer orders with carrier, service level, and ship-to address |
| `wmsOrderLine` | Order line items with allocation, pick, pack, ship quantity tracking |
| `wmsWave` | Wave groupings for coordinated pick release (carrier cutoff, priority, zone) |
| `wmsCarton` | Physical shipping containers with dimensions, weight, and tracking |
| `wmsCartonLine` | Items packed into cartons with lot and serial tracking |
| `wmsCartonType` | Available box/container sizes for cartonization |
| `wmsAllocationRule` | Configurable allocation strategy rules (FEFO, FIFO, LIFO) per category |
| `wmsKitBom` | Kit bill of materials linking finished items to components |

#### Shipping

| Table | Description |
|-------|-------------|
| `wmsShipment` | Shipment records with carrier, tracking, and parcel/LTL/FTL mode |
| `wmsShipmentOrder` | Many-to-many junction for split shipments and order consolidation |
| `wmsManifest` | End-of-day carrier manifests |
| `wmsDockAppointment` | Inbound/outbound dock door scheduling |

#### Returns

| Table | Description |
|-------|-------------|
| `wmsReturnAuthorization` | RMA records with reason codes and lifecycle status |
| `wmsReturnAuthorizationLine` | Return line items with authorized quantities and expected condition |
| `wmsReturnReceipt` | Physical receipt of returned goods |
| `wmsReturnReceiptLine` | Inspected return items with grade, disposition, and restock location |

#### Billing

| Table | Description |
|-------|-------------|
| `wmsBillingRateCard` | Client-specific rate card definitions with effective dates |
| `wmsBillingRate` | Individual rates per activity type (receiving, storage, pick, pack, ship, etc.) |
| `wmsBillingActivity` | Automatically captured billable events from task completion hooks |
| `wmsInvoice` | Generated invoices with billing period, totals, and accounting sync |
| `wmsInvoiceLine` | Invoice line items aggregated by activity type |

#### Configuration

| Table | Description |
|-------|-------------|
| `wmsPutawayRule` | Configurable directed putaway rules (zone type, velocity, category, storage) |
| `wmsReplenishmentRule` | Min/max thresholds for pick-face replenishment by item and location |

## Processes (47)

### Receiving

| Process | Description |
|---------|-------------|
| `receiveAgainstPO` | Receive goods against a PO with tolerance validation, QC check, and PUTAWAY task creation |
| `blindReceive` | Receive unexpected goods without a PO |
| `receiveASN` | Receive against an advance ship notice with per-line variance detection |
| `directedPutaway` | Evaluate configurable rules to recommend directed putaway locations |
| `qualityInspection` | QC inspection gate for received goods |

### Inventory

| Process | Description |
|---------|-------------|
| `wmsCycleCount` | Generate count plans (ABC, random, location-based) and create COUNT tasks |
| `approveCountVariance` | Supervisor reviews count variances, approves adjustments or triggers recounts |
| `inventoryAdjustment` | Manual adjustment with reason codes and optional supervisor approval |
| `inventoryMove` | Request an inventory relocation, creates MOVE task |
| `inventoryHold` | Place inventory on hold (QC, damage, recall, regulatory) |
| `inventoryRelease` | Release held inventory back to available status |

### Fulfillment

| Process | Description |
|---------|-------------|
| `allocateOrders` | Reserve inventory using FEFO/FIFO/LIFO allocation rules |
| `createWave` | Create a wave grouping for coordinated pick release |
| `releaseWave` | Release a wave and create optimized PICK tasks |
| `packOrder` | Pack picked items into cartons with scan verification |
| `kitAssembly` | Assemble kits from BOM components via KIT_ASSEMBLE tasks |
| `shortPickResolution` | Handle short picks with reallocation from alternate locations or backorder |
| `voidCarton` | Void a packed carton and return items to the pick queue |
| `cancelWave` | Cancel a wave with allocation reversal and order status rollback |
| `holdOrder` | Place an order on hold with associated task hold management |
| `releaseOrder` | Release a held order and resume fulfillment |

### Shipping

| Process | Description |
|---------|-------------|
| `generateShippingLabel` | Create carrier labels for parcel; generate BOL for LTL/FTL |
| `manifestShipments` | Create and transmit end-of-day carrier manifests |
| `shipConfirm` | Confirm shipment departure and push tracking to order source |

### Returns

| Process | Description |
|---------|-------------|
| `createRMA` | Generate return authorization from original order |
| `receiveReturn` | Receive returned items and create QC_INSPECT tasks |
| `inspectReturn` | Inspect and grade returned items per quality criteria |
| `dispositionReturn` | Apply disposition rules (restock, scrap, return to vendor) |

### Task Management

| Process | Description |
|---------|-------------|
| `getNextTask` | Mobile: assign and return the next available task for a worker |
| `completeTask` | Mobile: complete a task with scan verification and fire completion dispatcher |
| `reassignTask` | Supervisor: move a task to a different worker |
| `reprioritizeTask` | Supervisor: change task priority |
| `holdTask` | Supervisor: place a task on hold |
| `releaseTask` | Supervisor: release a held task back to the queue |
| `cancelTask` | Supervisor: cancel a task with type-specific reversal logic |
| `pauseTask` | Pause a task in progress |
| `resumeTask` | Resume a paused task |
| `staleTaskCheck` | Scheduled: find and unassign stuck or abandoned tasks |

### Billing

| Process | Description |
|---------|-------------|
| `generateInvoice` | Create invoice for a billing period with rate card application |
| `storageSnapshot` | Scheduled nightly: calculate daily storage billing per client |
| `syncInvoiceToAccounting` | Push invoices to accounting systems (QuickBooks, Xero) |

### Advanced / Scheduled

| Process | Description |
|---------|-------------|
| `abcAnalysis` | Reclassify SKU velocity (A/B/C) from order history |
| `autoAllocateAndRelease` | Scheduled: allocate new orders and release picks automatically |
| `autoAssignTasks` | Scheduled: auto-assign pending tasks to available workers by zone and equipment |
| `expirationAlertCheck` | Scheduled: flag inventory approaching expiration |
| `lowStockAlertCheck` | Scheduled: flag items below reorder point |
| `replenishCheck` | Scheduled: detect low pick-face inventory and create REPLENISH tasks |

## Dashboard Widgets (11)

### Task Dashboard

| Widget | Type | Description |
|--------|------|-------------|
| `wmsTaskQueueSummary` | Multi-Statistics | Task counts by type and status |
| `wmsTaskAging` | Bar Chart | Pending/assigned tasks by age bucket |
| `wmsActiveWorkers` | Table | Active workers with current task and time on task |
| `wmsWorkerProductivity` | Table | Per-worker task completion metrics |

### Operations Dashboard

| Widget | Type | Description |
|--------|------|-------------|
| `wmsOrdersToday` | Multi-Statistics | Orders received / picked / packed / shipped today |
| `wmsFulfillmentPipeline` | Stacked Bar | Orders by status from pending to shipped |
| `wmsSlaRisk` | Table | Orders at risk of missing ship-by date |

### Inventory Dashboard

| Widget | Type | Description |
|--------|------|-------------|
| `wmsInventorySummary` | Statistics | Total SKUs, units, locations, and utilization percentage |
| `wmsLowStockAlerts` | Table | Items below reorder point |
| `wmsInventoryAccuracy` | Statistics | Accuracy percentage from recent cycle counts |

### Billing Dashboard

| Widget | Type | Description |
|--------|------|-------------|
| `wmsBillingDashboard` | Multi-Statistics | Revenue, unbilled activities, and invoice status |

## Companion QBit Integration

### qbit-quick-search (ElasticSearch)

Index `wmsItem`, `wmsOrder`, `wmsInventory`, and `wmsTask` for full-text global search.

### qbit-webhooks

Supported event types:

| Event | Fires When |
|-------|------------|
| `wms.task.assigned` | Task assigned to a worker |
| `wms.task.completed` | Task completed |
| `wms.task.short` | Task completed with short quantity |
| `wms.task.cancelled` | Task cancelled |
| `wms.order.created` / `wms.order.shipped` | Order created or shipped |
| `wms.receipt.completed` | Receipt fully received and put away |
| `wms.inventory.adjusted` | Inventory adjustment recorded |
| `wms.inventory.low` | Item fell below reorder point |
| `wms.return.dispositioned` | Return disposition complete (enables refund processing) |
| `wms.shipment.confirmed` | Shipment confirmed with tracking |

### qbit-easypost-tracking (Carrier Integration)

Carrier label generation, rate shopping, and tracking number retrieval for parcel shipments. The WMS defines a `CarrierAdapter` interface so multiple carrier QBits can be plugged in.

## Testing

```bash
mvn test                    # Run all tests
mvn test -Dtest=BaseTest    # Verify QBit produces metadata
```

### Coverage

- All tests run in-memory via MemoryRecordStore (no database required)
- Test classes cover all entities, processes, widgets, completion hooks, and task dispatching

```bash
mvn org.jacoco:jacoco-maven-plugin:0.8.11:prepare-agent test \
    org.jacoco:jacoco-maven-plugin:0.8.11:report
# Report at: target/site/jacoco/index.html
```

## Phased Delivery

| Phase | Tables | Processes | Widgets | Focus |
|-------|--------|-----------|---------|-------|
| 1 | 12 | 14 | 10 | Core + Task Engine + Inventory |
| 2 | 8 | 5 | 1 | Receiving + Putaway |
| 3 | 8 | 9 | 2 | Fulfillment (Pick/Pack/Kit) |
| 4 | 4 | 3 | 0 | Shipping |
| 5 | 4 | 4 | 0 | Returns + Kitting |
| 6 | 5 | 4 | 5 | 3PL Billing |
| 7 | 4 | 6+ | 6 | Replenishment, Analytics, Optimization |
| **Total** | **46** | **47** | **11** | |

## Documentation

- **[QQQ Wiki](https://github.com/Kingsrook/qqq/wiki)** - Framework documentation
- **[QBit Development Guide](https://github.com/Kingsrook/qqq/wiki/QBit-Development)** - How QBits work
- **Design Spec** - Full data model and architecture in `QRun-IO/specs/2026-03-28-qbit-wms-design-v2.md`

## Contributing

QBit WMS is open source and welcomes contributions.

- **[Report Issues](https://github.com/QRun-IO/qqq/issues)** - Bug reports and feature requests
- **[QQQ Contribution Guide](https://github.com/Kingsrook/qqq/wiki/Contribution-Guidelines)** - How to contribute

## About Kingsrook

QBit WMS is built by **[Kingsrook](https://qrun.io)** - making engineers more productive through intelligent automation and developer tools.

- **Website**: [https://qrun.io](https://qrun.io)
- **Contact**: [contact@kingsrook.com](mailto:contact@kingsrook.com)
- **GitHub**: [https://github.com/QRun-IO](https://github.com/QRun-IO)

## License

This project is licensed under the **GNU Affero General Public License v3.0** - see the [LICENSE](LICENSE) file for details.
