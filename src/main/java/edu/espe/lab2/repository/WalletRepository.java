package edu.espe.lab2.repository;

import edu.espe.lab2.model.Wallet;

import java.util.Optional;

public interface WalletRepository {
    Wallet save (Wallet wallet);
    Optional<Wallet> findById(String id);
    boolean existsByOwnerEmail(String ownerEmail);
}
