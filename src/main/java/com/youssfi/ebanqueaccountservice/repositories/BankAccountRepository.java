package com.youssfi.ebanqueaccountservice.repositories;

import com.youssfi.ebanqueaccountservice.entities.BankAccount;
import com.youssfi.ebanqueaccountservice.enums.AccountType;
import com.youssfi.ebanqueaccountservice.projections.AccountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(excerptProjection = AccountProjection.class)
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    @RestResource(path = "byType")
    List<BankAccount> findByType(@Param("t") AccountType type);
}
