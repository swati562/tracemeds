using tracemeds.db as db from '../db/schema';

service TrackingService @(path: '/tracking') {

  entity Manufacturers as projection on db.Manufacturer;
  entity Distributors  as projection on db.Distributor;
  entity Pharmacies    as projection on db.Pharmacy;
  entity Medicines     as projection on db.Medicine;
  entity Batches       as projection on db.Batch;
  entity TrackEvents   as projection on db.TrackEvent;

action recordScanEvent(batchID: UUID, eventType: String, location: String, scannedBy: String) returns Batches;
action recallBatch(batchID: UUID, reason: String) returns Batches;
function getBatchHistory(batchID: UUID) returns array of TrackEvents;
}