package edu.espe.lab2.service;

import edu.espe.lab2.dto.WalletResponse;
import edu.espe.lab2.model.Wallet;
import edu.espe.lab2.repository.WalletRepository;

import java.util.Optional;

public class WalletService {
    private final WalletRepository walletRepository;
    private final RiskClient riskClient;

    public WalletService(WalletRepository walletRepository, RiskClient riskClient) {
        this.walletRepository = walletRepository;
        this.riskClient = riskClient;
    }

    //Crear una cuenta si cumple con las reglas del negocio
    public WalletResponse createWallet(String ownerEmail, double initialBalance) {
        if (ownerEmail == null || ownerEmail.isEmpty() || !ownerEmail.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        //Regla de negocio: usuario bloqueado
        if(riskClient.isBlocked(ownerEmail)){
            throw new IllegalArgumentException("User blocked");
        }

        //Regla de negocio: no duplicar cuenta por email
        if (walletRepository.existsByOwnerEmail(ownerEmail)) {
            throw new IllegalArgumentException("Wallet already exists");
        }

        Wallet wallet = new Wallet(ownerEmail, initialBalance);
        Wallet save = walletRepository.save(wallet);

        return new WalletResponse(save.getId(), save.getBalance());
    }

    public double deposit(String walletId, double amount) {
        //Validaciones
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        Optional<Wallet> found = walletRepository.findById(walletId);
        if(found.isEmpty()){
            throw new IllegalArgumentException("Wallet not found");
        }

        Wallet wallet = found.get();
        wallet.deposit(amount);

        //Persistimos el nuevo saldo
        walletRepository.save(wallet);

        return wallet.getBalance();
    }

    //Retiro de dinero
    public double withdraw(String walletId, double amount) {
        //validaciones
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new
                IllegalArgumentException("Wallet not found"));

        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("Insufficient funds");
        }

        wallet.withdraw(amount);
        walletRepository.save(wallet);
        return wallet.getBalance();
    }

}
