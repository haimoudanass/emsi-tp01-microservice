package com.youssfi.ebanqueaccountservice.service;

import com.youssfi.ebanqueaccountservice.dtos.BankAccountRequestDTO;
import com.youssfi.ebanqueaccountservice.dtos.BankAccountResponseDTO;

import java.util.List;

public interface BankAccountService {
    BankAccountResponseDTO addAccount(BankAccountRequestDTO requestDTO);

    List<BankAccountResponseDTO> getAccounts();

    BankAccountResponseDTO getAccountById(String id);

    BankAccountResponseDTO updateAccount(String id, BankAccountRequestDTO requestDTO);

    void deleteAccount(String id);
}
