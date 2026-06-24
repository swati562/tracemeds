package customer.tracemeds.handler;

import cds.gen.trackingservice.RecallBatchContext;
import cds.gen.trackingservice.GetBatchHistoryContext;
import cds.gen.trackingservice.RecordScanEventContext;
import cds.gen.trackingservice.TrackingService_;
import cds.gen.trackingservice.Batches;
import cds.gen.trackingservice.TrackEvents;
import cds.gen.trackingservice.TrackEvents_;
import cds.gen.tracemeds.db.Batch;
import cds.gen.tracemeds.db.Batch_;
import cds.gen.tracemeds.db.TrackEvent;
import cds.gen.tracemeds.db.TrackEvent_;

import com.sap.cds.Struct;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ServiceName(TrackingService_.CDS_NAME)
public class TrackingServiceHandler implements EventHandler {

    @Autowired
    PersistenceService db;

    @On
    public void onRecordScanEvent(RecordScanEventContext context) {
        String batchId = context.getBatchID();
        String eventType = context.getEventType();
        String location = context.getLocation();
        String scannedBy = context.getScannedBy();

        // Log the scan event
        TrackEvent event = TrackEvent.create();
        event.setBatchId(batchId);
        event.setEventType(eventType);
        event.setLocation(location);
        event.setScannedBy(scannedBy);
        db.run(Insert.into(TrackEvent_.class).entry(event));

        // 2. Fetch the current batch
        Batch batch = db.run(Select.from(Batch_.class).where(b -> b.ID().eq(batchId)))
                .single(Batch.class);

        // 3. Compute new status and apply it in-memory
        String newStatus = mapEventToStatus(eventType);
        batch.setStatus(newStatus);
        batch.setCurrentHolder(location);

        // 4. Persist the change
        db.run(Update.entity(Batch_.class)
                .data("status", newStatus)
                .data("currentHolder", location)
                .where(b -> b.ID().eq(batchId)));

        // 5. Return the in-memory updated object (no re-query needed)
        Batches result = Struct.create(Batches.class);
        result.putAll(batch);
        context.setResult(result);
    }

    private String mapEventToStatus(String eventType) {
        switch (eventType) {
            case "shipped":  return "in_transit";
            case "received": return "delivered";
            case "recalled": return "recalled";
            default:         return "manufactured";
        }
    }

    @On
    public void onRecallBatch(RecallBatchContext context) {
        String batchId = context.getBatchID();
        String reason = context.getReason();

        TrackEvent event = TrackEvent.create();
        event.setBatchId(batchId);
        event.setEventType("recalled");
        event.setRemarks(reason);
        db.run(Insert.into(TrackEvent_.class).entry(event));

        Batch batch = db.run(Select.from(Batch_.class).where(b -> b.ID().eq(batchId)))
                .single(Batch.class);
        batch.setStatus("recalled");

        db.run(Update.entity(Batch_.class)
                .data("status", "recalled")
                .where(b -> b.ID().eq(batchId)));

        Batches result = Struct.create(Batches.class);
        result.putAll(batch);
        context.setResult(result);
    }

    @On
    public void onGetBatchHistory(GetBatchHistoryContext context) {
        String batchId = context.getBatchID();
        List<TrackEvents> history = db.run(Select.from(TrackEvents_.class)
                .where(e -> e.batch_ID().eq(batchId)))
                .listOf(TrackEvents.class);
        context.setResult(history);
    }

  
    private long countRecalledBatches() {
        List<Batch> recalledBatches = db.run(
                Select.from(Batch_.class).where(b -> b.status().eq("recalled")))
                .listOf(Batch.class);
        return recalledBatches.size();
    }

}