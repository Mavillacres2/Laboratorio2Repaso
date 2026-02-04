package edu.espe.lab2.dto;

public class WalletResponse {

    private final String walletId;
    private final double balance;

    public WalletResponse(String walletId, double balance) {
        this.walletId = walletId;
        this.balance = balance;
    }

    public String getWalletId() {return walletId;}
    public double getBalance() {return balance;}
}
