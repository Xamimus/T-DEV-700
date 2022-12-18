package com.api.bank.manager;

import com.api.bank.model.BankConstants;
import com.api.bank.model.ObjectResponse;
import com.api.bank.model.entity.Account;
import com.api.bank.model.entity.Operation;
import com.api.bank.model.entity.QrCheck;
import com.api.bank.model.enums.OperationStatus;
import com.api.bank.model.enums.OperationType;
import com.api.bank.model.enums.PaymentMethod;
import com.api.bank.model.enums.TransactionStatus;
import com.api.bank.model.exception.BankTransactionException;
import com.api.bank.model.transaction.BankTransaction;
import com.api.bank.model.transaction.TransactionResult;
import com.api.bank.repository.AccountRepository;
import com.api.bank.repository.CheckRepository;
import com.api.bank.repository.ClientRepository;
import com.api.bank.repository.OperationRepository;
import com.api.bank.service.AccountService;
import com.api.bank.service.CheckService;
import com.api.bank.service.ClientService;
import com.api.bank.service.OperationService;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Queue;

@Component()
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BankManager implements IBankManager {
    private final AccountService accountService;
    private final OperationService operationService;
    private final ClientService clientService;
    private final CheckService checkService;

    private Queue<BankTransaction> transactionQueue;

    @Autowired
    public BankManager(OperationRepository operationRepository, AccountRepository accountRepository, ClientRepository clientRepository, CheckRepository checkRepository) {
        this.accountService = new AccountService(accountRepository);
        this.operationService = new OperationService(operationRepository);
        this.clientService = new ClientService(clientRepository);
        this.checkService = new CheckService(checkRepository);
    }

    /**
     * Handle a transaction between two accounts
     *
     * @param transaction Represents the transaction to be processed
     * @return The transaction status by the TransactionResult Object
     */
    @Override
    @Transactional(rollbackFor = {BankTransactionException.class, Exception.class}, propagation = Propagation.REQUIRES_NEW)
    public TransactionResult HandleTransaction(BankTransaction transaction) {

        try {
            var withdrawAccount = getWithdrawAccountBy(transaction);

            checkAccount(withdrawAccount, transaction, OperationType.WITHDRAW);
            checkMeansOfPayment(withdrawAccount, transaction);

            isAlreadyPendingOperation(withdrawAccount, transaction);
            var withdrawOperation = writeOperation(withdrawAccount, transaction, OperationStatus.PENDING, OperationType.WITHDRAW);  // Add the operation to the list of pending operations

            checkBalance(withdrawAccount, transaction);
            updateBalanceAndOperation(withdrawAccount, transaction, withdrawOperation);

            var depositAccount = getDepositAccount(transaction);
            checkAccount(depositAccount, transaction, OperationType.DEPOSIT);
            var depositOperation = writeOperation(depositAccount, transaction, OperationStatus.PENDING, OperationType.DEPOSIT);

            updateBalanceAndOperation(depositAccount, transaction, depositOperation);

            return new TransactionResult(TransactionStatus.SUCCESS, transaction.getOperationId(), "Payment has been validated");

        } catch (BankTransactionException e) {
            return new TransactionResult(e.getTransactionStatus(), transaction.getOperationId(), e.getMessage());

        } catch (Exception e) {
            return new TransactionResult(TransactionStatus.FAILED, transaction.getOperationId(), e.getMessage());
        }
    }



    private void updateBalanceAndOperation(Account account, BankTransaction transaction, Operation operation) throws BankTransactionException {

        if (operation.getOperationType() == OperationType.DEPOSIT)
            account.setSold(account.getSold() + transaction.getAmount());

        else if (operation.getOperationType() == OperationType.WITHDRAW)
            account.setSold(account.getSold() - transaction.getAmount());

        var result = accountService.update(account);
        if (result.isValid()) {
            if (!this.updateOperationStatus(OperationStatus.CLOSED, operation))
                throw new BankTransactionException(TransactionStatus.OPERATION_CLOSING_ERROR, transaction.getOperationId(), "Operation closing error");
        } else {
            this.updateOperationStatus(OperationStatus.CANCELED, operation);
            throw new BankTransactionException(TransactionStatus.PAYMENT_ERROR, transaction.getOperationId(), "Payment error was occurred");
        }
    }

    private void updateWithdrawBalance(Account withdrawAccount, BankTransaction transaction, Operation withdrawOperation) throws BankTransactionException {

        if (setSoldAccount(withdrawAccount, transaction).isValid()) {
            this.updateOperationStatus(OperationStatus.CLOSED, withdrawOperation);
            if (isCheckPayment(transaction)) {
                var qrCheck = getCheck(transaction);
                qrCheck.setSoldAmount(qrCheck.getSoldAmount() - transaction.getAmount());
                checkService.update(qrCheck);
            }
        } else {
            this.updateOperationStatus(OperationStatus.CANCELED, withdrawOperation);
            throw new BankTransactionException(TransactionStatus.OPERATION_PENDING_ERROR, transaction.getOperationId(), "Operation pending error");
        }
    }

    private void checkBalance(Account withdrawAccount, BankTransaction transaction) throws BankTransactionException {
        if (!withdrawAccount.isEnoughMoney(transaction.getAmount()))
            throw new BankTransactionException(TransactionStatus.INSUFFICIENT_FUNDS_ERROR, transaction.getOperationId(), "Account's insufficient funds");

        if (getCheck(transaction) != null && !getCheck(transaction).isEnoughMoney(transaction.getAmount()))
            throw new BankTransactionException(TransactionStatus.INSUFFICIENT_FUNDS_ERROR, transaction.getOperationId(), "Check amount invalid");
    }

    private Operation writeOperation(Account account, BankTransaction transaction, OperationStatus opStatus, OperationType opType) throws BankTransactionException {
        var operation = createOperation(transaction, account, getCheck(transaction), opStatus, opType, transaction.getPaymentMethod());
        if (!operationService.add(operation).isValid())
            throw new BankTransactionException(TransactionStatus.OPERATION_PENDING_ERROR, transaction.getOperationId(), "Operation pending error");
        return operation;
    }

    private void isAlreadyPendingOperation(Account withdrawAccount, BankTransaction transaction) throws BankTransactionException {
        // Is an operation is already in progress ?
        if (operationService.isOperationPendingFor(withdrawAccount.getId()))
            throw new BankTransactionException(TransactionStatus.OPERATION_PENDING_ERROR, transaction.getOperationId(), "Operation pending error");

    }

    private QrCheck getCheck(BankTransaction transaction) {
        return checkService.getCheckByCheckToken(transaction.getMeansOfPaymentId());
    }

    /**
     * Check if the payment method exist and is valid
     *
     * @param withdrawAccount the account to be debited
     * @param transaction     the transaction
     * @throws BankTransactionException if the payment method is not valid
     */
    private void checkMeansOfPayment(Account withdrawAccount, BankTransaction transaction) throws BankTransactionException {

        if (isCardPayment(transaction)) {
            // Is the card exist ?
            if (withdrawAccount.getCard() == null) {
                throw new BankTransactionException(TransactionStatus.CARD_ERROR, transaction.getOperationId(), "Card not found");
            }
            // Is the expiration date card's valid ?
            if (withdrawAccount.getCard().isExpired())
                throw new BankTransactionException(TransactionStatus.VALIDITY_DATE_ERROR, transaction.getOperationId(), "Card expired");
        } else if (isCheckPayment(transaction)) {
            var qrCheck = getCheck(transaction);
            // Is the check exist ?
            if (qrCheck == null)
                throw new BankTransactionException(TransactionStatus.CHECK_ERROR, transaction.getOperationId(), "Check not found");
            // Is the check expired ?
            if (qrCheck.isExpired())
                throw new BankTransactionException(TransactionStatus.VALIDITY_DATE_ERROR, transaction.getOperationId(), "Check expired");

        }
    }

    /**
     * Check if the account is valid and throw an exception if not
     *
     * @param transaction     Represents the transaction to be processed
     * @param account Represents the account to be debited
     * @throws BankTransactionException if the account is not valid
     */
    private void checkAccount(Account account, BankTransaction transaction, @Nullable OperationType opType) throws BankTransactionException {

        if (account == null) {
            throw new BankTransactionException(TransactionStatus.ACCOUNT_ERROR, transaction.getOperationId(), "Account not found");
        }
        if (opType == OperationType.WITHDRAW && isCheckPayment(transaction) && !account.getClient().getOrganisationName().equals(BankConstants.BANK_NAME)) {
            throw new BankTransactionException(TransactionStatus.BANK_ERROR, transaction.getOperationId(), "Bank not found");
        }
    }


    /**
     * Supply the account to withdraw
     *
     * @param transaction Represents the transaction to be processed
     * @throws BankTransactionException if the means of paiement is not valid
     * @return the account to withdraw
     */
    private Account getWithdrawAccountBy(BankTransaction transaction) throws BankTransactionException {

        if (isCardPayment(transaction)) {
            return accountService.getAccountByCardId(transaction.getMeansOfPaymentId());

        } else if (isCheckPayment(transaction)) {
            return clientService.getClientByOrganisationName(BankConstants.BANK_NAME).getAccount();
        } else {
            throw new BankTransactionException(TransactionStatus.MEANS_OF_PAYMENT_ERROR, transaction.getOperationId(), "Means of Payment error was occurred");
        }
    }

    /**
     * Supply the account to deposit
     * @param transaction   Represents the transaction to be processed
     * @return The account to deposit
     */
    private Account getDepositAccount(BankTransaction transaction) {
        return accountService.getAccountByClientId(transaction.getShopId());
    }

    private boolean isCardPayment(BankTransaction transaction) throws BankTransactionException {
        return transaction.getPaymentMethod() == PaymentMethod.CARD;
    }

    private boolean isCheckPayment(BankTransaction transaction) throws BankTransactionException {
        return transaction.getPaymentMethod() == PaymentMethod.CHECK;
    }

    private @NotNull Operation createOperation(BankTransaction transaction, Account account, QrCheck qrCheck, OperationStatus opeStatus, OperationType opeType, PaymentMethod payMethod) {
        return new Operation(transaction.getOperationId(), transaction.getLabel(), transaction.getAmount(),
                transaction.getDate(), account, qrCheck, opeStatus, opeType, payMethod);
    }

    private ObjectResponse setSoldAccount(Account clientAccount, BankTransaction transaction) {
        clientAccount.setSold(clientAccount.getSold() - transaction.getAmount());
        return accountService.update(clientAccount);
    }

    private boolean updateOperationStatus(OperationStatus status, Operation operation) {
        operation.setOperationStatus(status);
        return operationService.update(operation).isValid();
    }


}