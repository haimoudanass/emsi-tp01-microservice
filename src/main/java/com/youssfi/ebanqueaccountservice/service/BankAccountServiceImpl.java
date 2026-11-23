package com.youssfi.ebanqueaccountservice.service;

import com.youssfi.ebanqueaccountservice.dtos.BankAccountRequestDTO;
import com.youssfi.ebanqueaccountservice.dtos.BankAccountResponseDTO;
import com.youssfi.ebanqueaccountservice.entities.BankAccount;
import com.youssfi.ebanqueaccountservice.mappers.BankAccountMapper;
import com.youssfi.ebanqueaccountservice.repositories.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountMapper bankAccountMapper;

    public BankAccountServiceImpl(BankAccountRepository bankAccountRepository, BankAccountMapper bankAccountMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountMapper = bankAccountMapper;
    }

    @Override
    public BankAccountResponseDTO addAccount(BankAccountRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("requestDTO must not be null");
        }
        BankAccount bankAccount = bankAccountMapper.toBankAccount(requestDTO);
        bankAccount.setId(UUID.randomUUID().toString());
        if (bankAccount.getCreatedAt() == null) {
            bankAccount.setCreatedAt(new Date());
        }
        BankAccount saved = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.fromBankAccount(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponseDTO> getAccounts() {
        return bankAccountRepository.findAll()
                .stream()
                .map(bankAccountMapper::fromBankAccount)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountResponseDTO getAccountById(String id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Account %s not found", id)));
        return bankAccountMapper.fromBankAccount(bankAccount);
    }

    @Override
    public BankAccountResponseDTO updateAccount(String id, BankAccountRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("requestDTO must not be null");
        }
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Account %s not found", id)));

        if (requestDTO.getBalance() != null) {
            account.setBalance(requestDTO.getBalance());
        }
        if (requestDTO.getCurrency() != null) {
            account.setCurrency(requestDTO.getCurrency());
        }
        if (requestDTO.getType() != null) {
            account.setType(requestDTO.getType());
        }
        if (requestDTO.getCreatedAt() != null) {
            account.setCreatedAt(requestDTO.getCreatedAt());
        }

        BankAccount saved = bankAccountRepository.save(account);
        return bankAccountMapper.fromBankAccount(saved);
    }

    @Override
    public void deleteAccount(String id) {
        bankAccountRepository.deleteById(id);
    }
}
