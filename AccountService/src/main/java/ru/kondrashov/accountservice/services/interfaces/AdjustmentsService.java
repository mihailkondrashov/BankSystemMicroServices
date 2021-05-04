package ru.kondrashov.accountservice.services.interfaces;

import ru.kondrashov.accountservice.entities.Adjustment;

import java.util.Collection;
import java.util.UUID;

public interface AdjustmentsService {

    void save(Adjustment adjustment);

    Collection<Adjustment> getAdjustmentsByBillId(UUID billId);
}
