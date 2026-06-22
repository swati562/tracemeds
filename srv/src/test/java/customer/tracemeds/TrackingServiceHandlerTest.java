package customer.tracemeds;

import com.sap.cds.ql.Insert;
import com.sap.cds.services.persistence.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cds.gen.trackingservice.Batches;
import cds.gen.trackingservice.TrackEvents;
import cds.gen.trackingservice.TrackingService;
import cds.gen.tracemeds.db.Batch;
import cds.gen.tracemeds.db.Batch_;

import java.time.LocalDate;
import java.util.Collection;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TrackingServiceHandlerTest {

    @Autowired
    TrackingService trackingService;

    @Autowired
    PersistenceService db;

    String batchId;

    @BeforeEach
void setup() {
    Batch batch = Batch.create();
    batch.setBatchNo("BATCH-001");
    batch.setStatus("manufactured");
    batch.setCurrentHolder("Manufacturer A");

    var result = db.run(Insert.into(Batch_.class).entry(batch));
    Batch insertedBatch = result.single(Batch.class);
    batchId = insertedBatch.getId();
}

    @Test
    void recordScanEvent_updatesBatchStatus() {
        Batches result = trackingService.recordScanEvent(
        batchId, "shipped", "Warehouse 1", "QA Tester");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("in_transit");
        assertThat(result.getCurrentHolder()).isEqualTo("Warehouse 1");
 
    }
@Test
void recallBatch_marksBatchAsRecalled() {
    Batches result = trackingService.recallBatch(batchId, "Contamination detected in QA check");
    assertThat(result.getStatus()).isEqualTo("recalled");
}
@Test
void getBatchHistory_returnsLoggedEvents() {
    trackingService.recordScanEvent(batchId, "shipped", "Warehouse 1", "QA Tester");
  Collection<TrackEvents> history = trackingService.getBatchHistory(batchId);
    assertThat(history).isNotEmpty();
}
// @Test
// void isBatchExpired_returnsTrueForExpiredBatch() {
//     Batch expiredBatch = Batch.create();
//     expiredBatch.setBatchNo("BATCH-EXPIRED");
//     expiredBatch.setStatus("manufactured");
//     expiredBatch.setExpiryDate(LocalDate.now().minusDays(10));
//     var result = db.run(Insert.into(Batch_.class).entry(expiredBatch));
//     String expiredBatchId = result.single(Batch.class).getId();

//     boolean expired = trackingService.isBatchExpired(expiredBatchId);
//     assertThat(expired).isTrue();
// }
}