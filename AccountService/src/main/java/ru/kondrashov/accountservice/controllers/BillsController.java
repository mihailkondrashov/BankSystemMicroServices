package ru.kondrashov.accountservice.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.kondrashov.accountservice.controllers.dto.*;
import ru.kondrashov.accountservice.services.interfacies.BillsService;
import ru.kondrashov.accountservice.utils.interfacies.AdjustmentMapping;
import ru.kondrashov.accountservice.utils.interfacies.BillsMapping;
import org.springframework.transaction.annotation.Transactional;
import ru.kondrashov.accountservice.utils.interfacies.PaymentsMapping;
import ru.kondrashov.accountservice.utils.interfacies.TransferMapping;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/v1/people/{personId}/accounts/{accountId}/bills",produces = MediaType.APPLICATION_JSON_VALUE)
@Api(description= "Controller which provides methods for operations with bills", tags = {"BILLS"})
@RequiredArgsConstructor
public class BillsController {

    private final BillsService billsService;
    private final BillsMapping billsMapping;
    private final PaymentsMapping paymentsMapping;
    private final AdjustmentMapping adjustmentMapping;
    private final TransferMapping transferMapping;

    @GetMapping("")
    @ApiOperation(value = "Get all bills by account ID", tags = { "BILLS" })
    @ResponseStatus(HttpStatus.OK)
    public Collection<BillResponseDTO> getBillsByAccountId(@ApiParam(value = "Id of the person. Cannot be empty.") @PathVariable("personId") UUID personId, @ApiParam(value = "Id of the account to be obtained. Cannot be empty.") @PathVariable("accountId") UUID accountId){
        return billsService.getBillsByAccountId(accountId)
                .stream()
                .map(billsMapping::mapToBillResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("")
    @ApiOperation(value = "Save bill", tags = { "BILLS" })
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@ApiParam(value = "Id of the person. Cannot be empty.") @PathVariable("personId") UUID personId, @ApiParam(value = "Id of the account to be obtained. Cannot be empty.") @PathVariable("accountId") UUID accountId, @ApiParam("Bill to add. Cannot null or empty.") @RequestBody @Valid BillRequestDTO billRequestDTO){
        billsService.save(billsMapping.mapToBill(billRequestDTO));
    }

    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/{id}")
    @ApiOperation(value = "Update bill", tags = { "BILLS" })
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void update(@ApiParam(value = "Id of the person. Cannot be empty.") @PathVariable("personId") UUID personId, @ApiParam(value = "Id of the account to be obtained. Cannot be empty.") @PathVariable("accountId") UUID accountId, @ApiParam(value = "Id of the bill to be update. Cannot be empty.") @PathVariable("id") UUID id, @ApiParam("Bill to update. Cannot null or empty.") @RequestBody @Valid BillRequestDTO bill){
        billsService.update(id,billsMapping.mapToBill(bill));
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete bill", tags = { "BILLS" })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@ApiParam(value = "Id of the person. Cannot be empty.") @PathVariable("personId") UUID personId,
                       @ApiParam(value = "Id of the account to be obtained. Cannot be empty.") @PathVariable("accountId") UUID accountId,
                       @ApiParam(value = "Id of the bill to be delete. Cannot be empty.") @PathVariable("id") UUID id){
        billsService.delete(id);
    }


    @PutMapping("/{billId}/payments")
    @ApiOperation(value = "Commit payment for bill", tags = { "BILLS" })
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void commitPayment(@ApiParam(value = "Id of the person. Cannot be empty.")  @PathVariable("personId") UUID personId,
                              @ApiParam(value = "Id of the account. Cannot be empty.") @PathVariable("accountId") UUID accountId,
                              @ApiParam(value = "Id of the bill. Cannot be empty.") @PathVariable("billId") UUID billId,
                              @ApiParam("Payment to commit. Cannot null or empty.") @RequestBody @Valid PaymentRequestDTO paymentRequestDTO){
        billsService.commitPayment(billId,paymentsMapping.mapToPayment(paymentRequestDTO));
    }

    @PutMapping("/{id}/adjustments")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiOperation(value = "Commit adjustment for bill", tags = { "BILLS" })
    public void commitAdjustment(@ApiParam(value = "Id of the person. Cannot be empty.")  @PathVariable("personId") UUID personId,
                                 @ApiParam(value = "Id of the account. Cannot be empty.") @PathVariable("accountId") UUID accountId,
                                 @ApiParam(value = "Id of the bill. Cannot be empty.") @PathVariable("id") UUID billId,
                                 @ApiParam("Adjustment to commit. Cannot null or empty.") @RequestBody @Valid AdjustmentRequestDTO adjustmentRequestDTO){
        billsService.commitAdjustment(billId,adjustmentMapping.mapToAdjustment(adjustmentRequestDTO));
    }

    @PutMapping("/{sourceId}/transfer/{beneficiaryBillId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiOperation(value = "Commit transfer for bills", tags = { "BILLS" })
    public void commitTransfer(@ApiParam(value = "Id of the person. Cannot be empty.") @PathVariable("personId") UUID personId,
                               @ApiParam(value = "Id of the account. Cannot be empty.") @PathVariable("accountId") UUID accountId,
                               @ApiParam(value = "Id of the source bill. Cannot be empty.") @PathVariable("sourceId") UUID sourceId,
                               @ApiParam(value = "Id of the beneficiary bill. Cannot be empty.") @PathVariable("beneficiaryBillId") UUID beneficiaryBillId,
                               @ApiParam("Transfer to commit. Cannot null or empty.") @RequestBody @Valid TransferRequestDTO transferRequestDTO){
        billsService.commitTransfer(sourceId,beneficiaryBillId,transferMapping.mapToTransfer(transferRequestDTO));
    }

    @GetMapping("/{id}/transactions")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get all financial transactions of bill", tags = { "BILLS" })
    public Collection<FinancialTransactionResponseDTO> getFinancialTransactionsByBillId(@ApiParam(value = "Id of the person. Cannot be empty.") @PathVariable("personId") UUID personId,
                                                                                        @ApiParam(value = "Id of the account. Cannot be empty.") @PathVariable("accountId") UUID accountId,
                                                                                        @ApiParam(value = "Id of the bill. Cannot be empty.") @PathVariable("id") UUID billId){

        Collection<FinancialTransactionResponseDTO> financialTransactionResponseDTOS = new ArrayList<>();

        financialTransactionResponseDTOS.addAll(billsService.
                getAdjustmentsByBillId(billId).
                stream().
                map(adjustmentMapping::mapToFinancialTransactionResponseDTO).
                collect(Collectors.toList()));

        financialTransactionResponseDTOS.
                addAll(
                        setAmountNegative(
                                billsService.
                                        getPaymentsByBillId(billId).
                                        stream().
                                        map(paymentsMapping::mapToFinancialTransactionResponseDTO).
                                        collect(Collectors.toList())
                        )
                );

        financialTransactionResponseDTOS.addAll(billsService.
                getTransfersByBeneficiaryBillId(billId).
                stream().
                map(transferMapping::mapToFinancialTransactionResponseDTO).
                collect(Collectors.toList())
        );
        financialTransactionResponseDTOS.addAll(setAmountNegative(billsService.getTransfersBySourceBillId(billId).stream().map(transferMapping::mapToFinancialTransactionResponseDTO).collect(Collectors.toList())));

        return financialTransactionResponseDTOS;
    }

    private Collection<FinancialTransactionResponseDTO> setAmountNegative(Collection<FinancialTransactionResponseDTO> financialTransactionResponseDTOS){

        financialTransactionResponseDTOS.forEach(financialTransactionResponseDTO -> financialTransactionResponseDTO.setAmount(financialTransactionResponseDTO.getAmount().negate()));

        return financialTransactionResponseDTOS;
    }
}
