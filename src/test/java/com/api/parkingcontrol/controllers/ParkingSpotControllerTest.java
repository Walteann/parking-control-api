package com.api.parkingcontrol.controllers;

import com.api.parkingcontrol.dtos.ParkingSpotDTO;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.implementation.bind.annotation.Default;
import org.apiguardian.api.API;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ParkingSpotController.class)
@AutoConfigureMockMvc
public class ParkingSpotControllerTest {

    final String API_URL = "/parking-spot";

    @Autowired
    MockMvc mvc;

    @MockBean
    ParkingSpotService parkingSpotService;

    @Test
    @DisplayName("Deve criar um Parking Stop")
    void createParkingStop() throws Exception {
//        CENARIO
        UUID id = UUID.randomUUID();
        ParkingSpotDTO parkingSpotDTO = createParkingStopDTO();

        ParkingSpotModel parkingSpotModel = new ParkingSpotModel();

        BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);

        String json = new ObjectMapper().writeValueAsString(parkingSpotDTO);

        parkingSpotModel.setId(id);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        BDDMockito.given(parkingSpotService.save(Mockito.any(ParkingSpotModel.class))).willReturn(parkingSpotModel);

        MockHttpServletRequestBuilder request = buildPostMockMvcRequestBuilders(json);

//        EXCECUCAO
        mvc.perform(request).andExpect(status().isCreated());

//        VERIFICACAO

        Mockito.verify(parkingSpotService).save(Mockito.any(ParkingSpotModel.class));
    }

    @Test
    @DisplayName("Deve retornar erro quando tentar salvar um parking spot com licensePlateCar ja em uso")
    void shouldReturnErrorWhenLicenseAlreadyInUse() throws Exception {

//        CENARIO
        ParkingSpotDTO parkingSpotDTO = createParkingStopDTO();
        String json = new ObjectMapper().writeValueAsString(parkingSpotDTO);

        BDDMockito.given(parkingSpotService.existsByLicensePlateCar(Mockito.any())).willReturn(true);

        MockHttpServletRequestBuilder request = buildPostMockMvcRequestBuilders(json);

//        EXECUCAO

        mvc.perform(request)
                .andExpect(status().isConflict())
                .andExpect(content().string("Conflict: License Plate Car is already in use!"));

        Mockito.verify(parkingSpotService, Mockito.never()).save(Mockito.any());

    }

    @Test
    @DisplayName("Deve retornar erro quando tentar salvar um parking spot com parkingSpotNumber ja em uso")
    void shouldReturnErrorWhenParkingSpotAlreadyInUse() throws Exception {

//        CENARIO
        ParkingSpotDTO parkingSpotDTO = createParkingStopDTO();
        String json = new ObjectMapper().writeValueAsString(parkingSpotDTO);

        BDDMockito.given(parkingSpotService.existsByParkingSpotNumber(Mockito.any())).willReturn(true);

        MockHttpServletRequestBuilder request = buildPostMockMvcRequestBuilders(json);

//        EXECUCAO

        mvc.perform(request)
                .andExpect(status().isConflict())
                .andExpect(content().string("Conflict: Parking Spot is already in use!"));

        Mockito.verify(parkingSpotService, Mockito.never()).save(Mockito.any());

    }

    @Test
    @DisplayName("Deve retornar erro quando tentar salvar um parking spot com apartment e bloco ja em uso")
    void shouldReturnErrorWhenApartmentAlreadyInUse() throws Exception {

//        CENARIO
        ParkingSpotDTO parkingSpotDTO = createParkingStopDTO();
        String json = new ObjectMapper().writeValueAsString(parkingSpotDTO);

        BDDMockito.given(parkingSpotService.existsByApartmentAndBlock(parkingSpotDTO.getApartment(), parkingSpotDTO.getBlock())).willReturn(true);

        MockHttpServletRequestBuilder request = buildPostMockMvcRequestBuilders(json);

//        EXECUCAO

        mvc.perform(request)
                .andExpect(status().isConflict())
                .andExpect(content().string("Conflict: Parking Spot already registered for this apartment/block!"));

        Mockito.verify(parkingSpotService, Mockito.never()).save(Mockito.any());

    }


    @Test
    @DisplayName("Deve carregar todos os Parking spots")
    void shouldGetAllParkingSpots() throws Exception {
//        Cenario
    ParkingSpotModel parkingSpotModel = createParkingSpotModel(UUID.randomUUID());

    BDDMockito.given(parkingSpotService.findAll(Mockito.any(Pageable.class)))
            .willReturn(new PageImpl<ParkingSpotModel>(List.of(parkingSpotModel), PageRequest.of(0, 10), 1));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
//        Execucao
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));

//        Validacao
    }

    @Test
    @DisplayName("Deve retornar um parking spot por id")
    void shouldFindByIdParkingSpot() throws Exception {
        UUID id = UUID.randomUUID();
        ParkingSpotModel parkingSpotModel = createParkingSpotModel(id);

        BDDMockito.given(parkingSpotService.findById(id)).willReturn(Optional.of(parkingSpotModel));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(API_URL + '/' + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id.toString()));
    }

    @Test
    @DisplayName("Deve retornar error quando tentar encontrar um parking spot inexistente")
    void shouldErrorFindByIdNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        BDDMockito.given(parkingSpotService.findById(Mockito.any())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(API_URL + '/' + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um parking spot")
    void shouldDeleteParkingSpot() throws Exception {
        UUID id = UUID.randomUUID();

        ParkingSpotModel parkingSpotModel = createParkingSpotModel(id);
        Optional<ParkingSpotModel> optional = Optional.of(parkingSpotModel);

        BDDMockito.given(parkingSpotService.findById(id)).willReturn(optional);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(API_URL + '/' + id)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                        .andExpect(status().isOk());

        Mockito.verify(parkingSpotService, Mockito.times(1)).delete(optional.get());
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um parking spot inexistente")
    void shouldErroTryDeleteParkingSpotNonexistent() throws Exception {
        UUID id = UUID.randomUUID();

        BDDMockito.given(parkingSpotService.findById(id)).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(API_URL + "/" + id)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Parking Spot Not Found."));

        Mockito.verify(parkingSpotService, Mockito.never()).delete(Mockito.any());

    }

    @Test
    @DisplayName("Deve atualizar um parking spot")
    void shouldUpdateParkingSpot() throws Exception {

        UUID id = UUID.randomUUID();

        ParkingSpotModel parkingSpotModel = createParkingSpotModel(id);
        BDDMockito.given(parkingSpotService.findById(id)).willReturn(Optional.of(parkingSpotModel));

        parkingSpotModel.setParkingSpotNumber("400B");

        ParkingSpotDTO dto = createParkingStopDTO();
        dto.setParkingSpotNumber("400B");

        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(parkingSpotService.save(Mockito.any(ParkingSpotModel.class))).willReturn(parkingSpotModel);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(API_URL + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("parkingSpotNumber").value("400B"));

    }

    @Test
    @DisplayName("Deve retornar error quando tentar atualizar um parking spot não encontrado.")
    void shouldErrorWhenTryUpdatingParkingSpotNotFound() throws Exception {

//        CENARIO
        UUID id = UUID.randomUUID();
        BDDMockito.given(parkingSpotService.findById(id)).willReturn(Optional.empty());

        ParkingSpotDTO dto = createParkingStopDTO();
        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(API_URL + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

//        EXECUCAO
        mvc.perform(request)
                .andExpect(status().isNotFound());

//        VERIFICACAO

        Mockito.verify(parkingSpotService, Mockito.never()).save(Mockito.any(ParkingSpotModel.class));

    }

    private MockHttpServletRequestBuilder buildPostMockMvcRequestBuilders(String json) {
        return MockMvcRequestBuilders
                .post(API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
    }

    private ParkingSpotDTO createParkingStopDTO() {
        ParkingSpotDTO dto = new ParkingSpotDTO();

        dto.setParkingSpotNumber("101A");
        dto.setBrandCar("Hyundai");
        dto.setModelCar("HB20");
        dto.setColorCar("Blue");
        dto.setBlock("A");
        dto.setResponsibleName("Szylzen Silva");
        dto.setLicensePlateCar("KND0091");
        dto.setApartment("101");
        return dto;
    }

    private ParkingSpotModel createParkingSpotModel(UUID id) {
        ParkingSpotDTO dto = createParkingStopDTO();
        ParkingSpotModel parkingSpotModel = new ParkingSpotModel();

        BeanUtils.copyProperties(dto, parkingSpotModel);

        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        parkingSpotModel.setId(id);

        return parkingSpotModel;

    }
}
