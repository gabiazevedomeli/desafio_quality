package com.dh.mercadolivre.desafioquality.service;

import com.dh.mercadolivre.desafioquality.dto.DefaultServerResponseDto;
import com.dh.mercadolivre.desafioquality.dto.PropertyDto;
import com.dh.mercadolivre.desafioquality.dto.RoomAreaDto;
import com.dh.mercadolivre.desafioquality.exceptions.DistrictNotFoundException;
import com.dh.mercadolivre.desafioquality.exceptions.PropertyNotFoundException;
import com.dh.mercadolivre.desafioquality.model.District;
import com.dh.mercadolivre.desafioquality.model.Property;
import com.dh.mercadolivre.desafioquality.repository.DistrictRepository;
import com.dh.mercadolivre.desafioquality.repository.PropertyRepository;
import com.dh.mercadolivre.desafioquality.util.TestUtilsGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyServiceTest {

    @InjectMocks
    PropertyService service;

    @Mock
    PropertyRepository propertyRepository;

    @Mock
    DistrictRepository districtRepository;

    @BeforeEach
    public void setup(){
        BDDMockito.when(propertyRepository.getProperty(ArgumentMatchers.anyLong()))
                .thenReturn(TestUtilsGenerator.generateNewProperty());

        BDDMockito.when(propertyRepository.getAllProperty())
                .thenReturn(TestUtilsGenerator.generateListProperty());

        BDDMockito.when(propertyRepository.deleteProperty(ArgumentMatchers.anyInt()))
                .thenReturn(true);

        BDDMockito.when(districtRepository.getAllDistrict())
                .thenReturn(TestUtilsGenerator.generateNewDistrictList());

        BDDMockito.doNothing().when(districtRepository).saveDistrict(ArgumentMatchers.any(District.class));

        BDDMockito.when(propertyRepository.saveProperty(ArgumentMatchers.any(Property.class)))
                .thenAnswer(i -> i.getArguments()[0]);

    }

    @Test
    @DisplayName("Test calculation of the property's square meter total value")
    void calculateTotalPropertyPrice() {
        Property newProperty = TestUtilsGenerator.generateNewProperty();

       Double result = service.calculateTotalPropertyPrice(newProperty.getId());

       assertThat(result).isEqualTo(5850.0);
       verify(propertyRepository, atLeastOnce()).getProperty(newProperty.getId()); 
    }

    @Test
    @DisplayName("Tests function when District does not exist and throws an Error")
    void calculateTotalPropertyPriceWhitoutDistrict() {
        BDDMockito.when(districtRepository.getAllDistrict())
                .thenReturn(new ArrayList<District>());

        Property newProperty = TestUtilsGenerator.generateNewProperty();


        DistrictNotFoundException exception = Assertions.assertThrows(DistrictNotFoundException.class, () -> {
            Double result = service.calculateTotalPropertyPrice(newProperty.getId());
        });

        assertThat(exception.getMessage()).isEqualTo(String.format("District Not Found"));
        verify(propertyRepository, atLeastOnce()).getProperty(newProperty.getId());
        verify(districtRepository, atLeastOnce()).getAllDistrict();
    }

    @Test
    @DisplayName("Tests a method that calculates the property total area")
    void getAreaTotal() {
        Property newProperty = TestUtilsGenerator.generateNewProperty();

        Double result = service.getAreaTotal(newProperty.getId());

        assertThat(result).isEqualTo(6.5);
        verify(propertyRepository, atLeastOnce()).getProperty(newProperty.getId());
    }

    @Test
    @DisplayName("Test the largest room on the property")
    void getLargestRoomFromId() {
        Property newProperty = TestUtilsGenerator.generateNewProperty();
        RoomAreaDto largestRoom = TestUtilsGenerator.generateLargestRoom();
        RoomAreaDto result = service.getLargestRoomFromId(newProperty.getId());

        assertThat(result).isEqualTo(largestRoom);
        verify(propertyRepository, atLeastOnce()).getProperty(newProperty.getId());
    }

    @Test
    @DisplayName("Tests if returns a list of RoomAreaDto")
    void getAreaRooms() {
        Property newProperty = TestUtilsGenerator.generateNewProperty();
        List<RoomAreaDto> listRoom = TestUtilsGenerator.generateListRoom();

        List<RoomAreaDto> result = service.getAreaRooms(newProperty.getId());

        assertThat(result).isEqualTo(listRoom);
        verify(propertyRepository, atLeastOnce()).getProperty(newProperty.getId());
    }

    @Test
    @DisplayName("Tests if returns a Property through the id")
    void getProperty() {
        Property newProperty = TestUtilsGenerator.generateNewProperty();

        PropertyDto result = service.getProperty(newProperty.getId());

        assertThat(result.getId()).isEqualTo(newProperty.getId());
        assertThat(result.getPropName()).isEqualTo(newProperty.getPropName());
        verify(propertyRepository, atLeastOnce()).getProperty(newProperty.getId());
    }

    @Test
    @DisplayName("Tests if deletes and returns true when the property exists")
    void deleteProperty() {
        Property newProperty = TestUtilsGenerator.generateNewProperty();

        DefaultServerResponseDto result = service.deleteProperty(newProperty.getId());

        assertThat(result.getMessage()).isEqualTo("Property successfully deleted");
        assertThat(result.getStatus()).isEqualTo("OK");
        verify(propertyRepository, atLeastOnce()).getAllProperty();
        verify(propertyRepository, atLeastOnce()).deleteProperty(0);
    }

    @Test
    @DisplayName("Tests if returns false when the property does not exist")
    void deletePropertyWhenPropertyNotExist() {

        PropertyNotFoundException exception = Assertions.assertThrows(PropertyNotFoundException.class, () -> {
            DefaultServerResponseDto result = service.deleteProperty(2L);
        });

        assertThat(exception.getMessage()).contains("Could not find property for id");
        verify(propertyRepository, atLeastOnce()).getAllProperty();
        verify(propertyRepository, never()).deleteProperty(ArgumentMatchers.anyInt());
    }

    @Test
    @DisplayName("Tests if returns PropertyDTO on successful save")
    void savePropertyWithSuccess() {
        Property propertyWithoutId = TestUtilsGenerator.generateNewPropertyWithoutId();

        PropertyDto result = service.saveProperty(propertyWithoutId);

        assertThat(result.getId()).isEqualTo(2L);
        verify(propertyRepository, atLeastOnce()).getAllProperty();
        verify(districtRepository, atLeastOnce()).getAllDistrict();
        verify(districtRepository, never()).saveDistrict(ArgumentMatchers.any(District.class));
    }

    @Test
    @DisplayName("Tests if it returns PropertyDTO when saving successfully and with different District")
    void savePropertyWithSuccessAndNewDistrict() {
        Property propertyWithNewDistrict = TestUtilsGenerator.generateNewPropertyWithoutIdAndNewDistrict();

        PropertyDto result = service.saveProperty(propertyWithNewDistrict);

        verify(propertyRepository, atLeastOnce()).getAllProperty();
        verify(districtRepository, atLeastOnce()).getAllDistrict();
        verify(districtRepository, atLeastOnce()).saveDistrict(ArgumentMatchers.any(District.class));
    }

}