package edu.espe.lab2;

import edu.espe.lab2.dto.WalletResponse;
import edu.espe.lab2.model.Wallet;
import edu.espe.lab2.repository.WalletRepository;
import edu.espe.lab2.service.RiskClient;
import edu.espe.lab2.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WalletServiceTest {
    private WalletRepository walletRepository;
    private WalletService walletService;
    private RiskClient riskClient;

    @BeforeEach
    public void setup() {
        walletRepository = Mockito.mock(WalletRepository.class);
        riskClient = Mockito.mock(RiskClient.class);
        walletService = new WalletService(walletRepository, riskClient);
    }

    //Crear cuenta con datos válidos, guardar y retornar respuesta
    @Test
    void createWallet_validData_shouldSaveAndReturnResponse() {
        //Arrange
        String email = "michael@espe.edu.ec";
        double balance = 100.0;

        when(walletRepository.existsByOwnerEmail(email)).thenReturn(Boolean.FALSE);
        when(riskClient.isBlocked(email)).thenReturn(false);
        when(walletRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        //Act
        WalletResponse response = walletService.createWallet(email, balance);

        //Assert
        assertNotNull(response);
        assertEquals(100.0, response.getBalance());

        verify(riskClient).isBlocked(email);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletRepository).existsByOwnerEmail(email);
    }

    //Crear cuenta con correo no válido, lanzar excepción y no crear las dependencias
    @Test
    void createWallet_invalidEmail_shouldThrow_andNotCallDependencies(){
        //Arrange
        String invalidEmail = "michael-espe.edu.ec";

        //Act+Assert
        assertThrows(IllegalArgumentException.class, () -> walletService.createWallet(invalidEmail, 50.0));

        //No debe llamar a ninguna dependencia porque falla la validacion
        verifyNoInteractions(walletRepository, riskClient);
    }

    //Depositar a una cuenta que no se encuentra y lanzar excepción
    @Test
    void deposit_walletNotFound_shouldThrow(){
        //Arrange
        String walletId = "no-exist-wallet";

        when(walletRepository.existsByOwnerEmail(walletId)).thenReturn(Optional.empty().isEmpty());

        //Act + Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> walletService
                .deposit(walletId, 60.0));

        assertEquals("Wallet not found", exception.getMessage());
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any());
    }

    //Depositar a una cuenta, se debe actualizar el balance, se debe guardar y usar captor
    @Test
    void deposit_shouldUpdateBalance_andSave_usingCaptor(){
        //Arrange
        Wallet wallet = new Wallet("kacortez@espe.edu.ec",300.0);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);

        //Act
        double newBalance = walletService.deposit(walletId, 300.0);

        //Assert
        assertEquals(600.0, newBalance);

        verify(walletRepository).save(captor.capture());
        Wallet saved = captor.getValue();
        assertEquals(600.0, saved.getBalance());
    }

    @Test
    void withdraw_insufficientFunds_shouldThrow_andNotSave(){
        //Arrange
        Wallet wallet = new Wallet("michael@espe.edu.ec",300);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        //Act + Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> walletService
                .withdraw(walletId,500));

        assertEquals("Insufficient funds", exception.getMessage());
        verify(walletRepository, never()).save(any());
    }
}
