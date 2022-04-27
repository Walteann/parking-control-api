package com.api.parkingcontrol.services;

import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.repositories.ParkingSpotRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class ParkingSpotServiceTest {

    ParkingSpotService service;

    @MockBean
    ParkingSpotRepository repository;

    @BeforeEach
    void setUp() {
        this.service = new ParkingSpotService(repository);
    }

    @Test
    @DisplayName("Deve salvar um Parking spot")
    void shouldSaveAParkingSpot() {
//        CENARIO
        ParkingSpotModel parkingSpotModel = createParkingSpotModel();
        Mockito.when(repository.save(Mockito.any(ParkingSpotModel.class))).thenReturn(parkingSpotModel);

//        EXECUCAO
        ParkingSpotModel result = service.save(parkingSpotModel);

//        VALIDACAO

        assertThat(result.getParkingSpotNumber())
                .isEqualTo(parkingSpotModel.getParkingSpotNumber());
        assertThat(result.getBlock())
                .isEqualTo(parkingSpotModel.getBlock());
        assertThat(result.getLicensePlateCar())
                .isEqualTo(parkingSpotModel.getLicensePlateCar());
    }

    @Test
    @DisplayName("Deve retornar TRUE quando existir placa KND1234")
    void shouldReturnTrueWhenExistsByLicensePlateCar() {
        String plate = "KND1234";
        Mockito.when(repository.existsByLicensePlateCar(plate)).thenReturn(true);

        Boolean result = service.existsByLicensePlateCar(plate);

        assertThat(result).isTrue();

    }

    @Test
    @DisplayName("Deve retornar FALSE nao existir com placa KND1234")
    void shouldReturnFalseWhenNoExistsByLicensePlateCar() {
        String plate = "KND1234";
        Mockito.when(repository.existsByLicensePlateCar(plate)).thenReturn(false);

        Boolean result = service.existsByLicensePlateCar(plate);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar TRUE quando existir ParkingSpotNumber 200A")
    void shouldReturnTrueWhenExistsByParkingSpotNumber() {
        String parkingSpotNumber = "200A";
        Mockito.when(repository.existsByParkingSpotNumber(parkingSpotNumber)).thenReturn(true);

        Boolean result = service.existsByParkingSpotNumber(parkingSpotNumber);

        assertThat(result).isTrue();

    }

    @Test
    @DisplayName("Deve retornar FALSE nao existir com ParkingSpotNumber 200A")
    void shouldReturnFalseWhenNoExistsByParkingSpotNumber() {
        String parkingSpotNumber = "200A";
        Mockito.when(repository.existsByParkingSpotNumber(parkingSpotNumber)).thenReturn(false);

        Boolean result = service.existsByParkingSpotNumber(parkingSpotNumber);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar TRUE quando existir Aptarment 200 e Bloco B")
    void shouldReturnTrueWhenExistsByApartmentAndBlock() {
        String apartment = "200";
        String block = "B";
        Mockito.when(repository.existsByApartmentAndBlock(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Boolean result = service.existsByApartmentAndBlock(apartment, block);

        assertThat(result).isTrue();

    }

    @Test
    @DisplayName("Deve retornar FALSE nao existir com Aptarment 200 e Bloco B")
    void shouldReturnFalseWhenNoExistsByApartmentAndBlock() {
        String apartment = "200";
        String block = "B";
        Mockito.when(repository.existsByApartmentAndBlock(apartment, block)).thenReturn(false);

        Boolean result = service.existsByApartmentAndBlock(apartment, block);

        assertThat(result).isFalse();
    }



    private ParkingSpotModel createParkingSpotModel() {
        ParkingSpotModel model = new ParkingSpotModel();
        model.setParkingSpotNumber("300A");
        model.setBrandCar("Hyundai");
        model.setModelCar("HB20");
        model.setColorCar("Blue");
        model.setBlock("A");
        model.setResponsibleName("Szylzen Silva");
        model.setLicensePlateCar("KND0091");
        model.setApartment("101");
        model.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));

        return model;
    }

}
