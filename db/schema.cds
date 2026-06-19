namespace tracemeds.db;

using { cuid, managed } from '@sap/cds/common';

entity Manufacturer : cuid, managed {
  name        : String(100);
  licenseNo   : String(40);
  country     : String(60);
}

entity Distributor : cuid, managed {
  name        : String(100);
  licenseNo   : String(40);
  city        : String(60);
}

entity Pharmacy : cuid, managed {
  name        : String(100);
  licenseNo   : String(40);
  city        : String(60);
}

entity Medicine : cuid, managed {
  name          : String(100);
  composition   : String(200);
  manufacturer  : Association to Manufacturer;
  batches       : Composition of many Batch on batches.medicine = $self;
}

entity Batch : cuid, managed {
  batchNo       : String(40);
  medicine      : Association to Medicine;
  mfgDate       : Date;
  expiryDate    : Date;
  quantity      : Integer;
  currentHolder : String(100);
  status        : String(20) enum { manufactured; in_transit; delivered; recalled };
  events        : Composition of many TrackEvent on events.batch = $self;
}

entity TrackEvent : cuid, managed {
  batch       : Association to Batch;
  eventType   : String(30) enum { scanned; shipped; received; recalled };
  location    : String(120);
  scannedBy   : String(100);
  remarks     : String(200);
}